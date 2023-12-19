(ns build
  (:require
   [clojure.tools.build.api :as b]
   [babashka.process :refer [shell]])
  (:import [io.github.humbleui.jwm Platform]))

(def class-dir "target/classes")
(def uber-file "target/example.jar")
(def executable-file "target/example")

(def include-resources
  (str (condp = Platform/CURRENT
         Platform/MACOS ".*\\.dylib"
         Platform/WINDOWS ".*\\.dll"
         Platform/X11 ".*\\.so")
       "|.*jwm.version"
       "|.*skija.version"
       "|.*\\.ttf")) ; default humble theme bundles a ttf font

(def graalvm-home (System/getenv "GRAALVM_HOME"))
(def native-image-bin (str graalvm-home "/bin/native-image"))

(def basis (b/create-basis {:project "deps.edn"}))

(defn clean [_]
  (b/delete {:path "target"}))

(defn uber [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main
           ; 'example.jwm
           ; 'example.skija
           'example.humble}))

(defn native [_]
  (uber nil)
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
   ; "--trace-class-initialization=io.github.humbleui.skija.Font"
   ; "--trace-class-initialization=io.github.humbleui.skija.Typeface"

   ; "-H:ConfigurationFileDirectories=native-image-config"
   ; "-H:ConfigurationFileDirectories=traced-config"
   ; "-H:ReflectionConfigurationFiles=reflect-config.json"
   "-H:+JNI"
   "-H:JNIConfigurationFiles=traced-config/jni-config.json"
   (str "-H:IncludeResources=" include-resources)

   "--no-fallback"
   "-H:+ReportExceptionStackTraces"
   "--report-unsupported-elements-at-runtime"

   "--native-image-info"
   "--verbose"
   "-Dskija.logLevel=DEBUG"

   "-jar"
   uber-file
   executable-file))
