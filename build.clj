(ns build
  (:require
   [clojure.tools.build.api :as b]
   [babashka.process :refer [shell]])
  (:import [io.github.humbleui.jwm Platform]))

(def target-path "target")
(def class-dir (str target-path "/classes"))

(defn ->main [{:keys [main]}]
  (or main "humble"))

(defn ->uber-file [params]
  (str target-path "/" (->main params) ".jar"))

(defn ->executable-file [params]
  (str target-path "/" (->main params)))

(def include-resources
  (str
   ;; uber jar contains native libs for all platforms, let's pickup only those for target platform
   ;; this can be further improved by also picking only those for target architecture like x86_64 and arm64
   (condp = Platform/CURRENT
     Platform/MACOS ".*\\.dylib"
     Platform/WINDOWS ".*\\.dll"
     Platform/X11 ".*\\.so")
   "|.*jwm.version"
   "|.*skija.version"
   ;; default humble theme bundles a ttf font
   "|.*\\.ttf"))

(def graalvm-home (System/getenv "GRAALVM_HOME"))
(def native-image-bin (str graalvm-home "/bin/native-image"))

(def basis (b/create-basis {:project "deps.edn"}))

;; == Build API ==

(defn clean [_]
  (b/delete {:path "target"}))

(defn uber [params]
  (clean params)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file (->uber-file params)
           :basis basis
           :main (symbol (str "example." (->main params)))}))

(defn native [params]
  (uber params)
  (shell
   native-image-bin
   "--initialize-at-build-time"
   "-J-Dclojure.compiler.direct-linking=true"

   "--initialize-at-run-time=io.github.humbleui.jwm.impl.RefCounted$_FinalizerHolder"
   "--initialize-at-run-time=io.github.humbleui.jwm.impl.Managed"

   "--initialize-at-run-time=io.github.humbleui.skija.impl.Cleanable"
   "--initialize-at-run-time=io.github.humbleui.skija.impl.RefCnt$_FinalizerHolder"

   "-Dskija.staticLoad=false"
   "--initialize-at-run-time=io.github.humbleui.skija"

   ; "--trace-object-instantiation=java.lang.ref.Cleaner"
   ; "--trace-class-initialization=io.github.humbleui.skija.shaper.Shaper"

   ; "-H:ConfigurationFileDirectories=traced-config"
   ; "-H:ReflectionConfigurationFiles=reflect-config.json"
   "-H:JNIConfigurationFiles=traced-config/jni-config.json"
   "-H:+JNI"
   (str "-H:IncludeResources=" include-resources)

   ;; Some extra reporting for debugging
   "-H:+ReportExceptionStackTraces"
   "--report-unsupported-elements-at-runtime"
   "--native-image-info"
   "--verbose"
   "-Dskija.logLevel=DEBUG"

   "--no-fallback"
   "-jar"
   (->uber-file params)
   (->executable-file params)))
