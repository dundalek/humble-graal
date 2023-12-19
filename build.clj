(ns build
  (:require
   [clojure.tools.build.api :as b]
   [babashka.process :refer [shell]])
  (:import [io.github.humbleui.jwm Platform]))

(def class-dir "target/classes")
(def uber-file "target/example.jar")
(def executable-file "target/example")

(def include-resources
  ; ".*"
  (str (condp = Platform/CURRENT
         Platform/MACOS ".*\\.dylib"
         Platform/WINDOWS ".*\\.dll"
         Platform/X11 ".*\\.so")
       "|.*\\.version"))

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
           'example.skija}))

(defn native [_]
  (uber nil)
  (shell
   native-image-bin
   "--initialize-at-build-time"
   "-J-Dclojure.compiler.direct-linking=true"
   ; "--trace-object-instantiation=java.lang.ref.Cleaner"
   "--initialize-at-run-time=io.github.humbleui.jwm.impl.RefCounted$_FinalizerHolder"
   "--initialize-at-run-time=io.github.humbleui.jwm.impl.Managed"

   "--initialize-at-run-time=io.github.humbleui.skija.impl.Cleanable"
   "--initialize-at-run-time=io.github.humbleui.skija.impl.RefCnt$_FinalizerHolder"

   "--initialize-at-run-time=io.github.humbleui.skija"

   ; "-H:ReflectionConfigurationFiles=reflect-config.json"
   ; "-H:ConfigurationFileDirectories=native-image-config"
   "-H:ConfigurationFileDirectories=traced-config"
   "-H:+JNI"
   (str "-H:IncludeResources=" include-resources)
   "-H:+ReportExceptionStackTraces"

   "-Dskija.staticLoad=false"
   "-Dskija.logLevel=DEBUG"

   "--no-fallback"
   "--report-unsupported-elements-at-runtime"
   "--native-image-info"
   "--verbose"

   "-jar"
   uber-file
   executable-file))
