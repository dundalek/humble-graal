(ns build
  (:require
   [clojure.tools.build.api :as b]
   [babashka.process :refer [shell]])
  (:import [io.github.humbleui.jwm Platform]))

(def class-dir "target/classes")
(def uber-file "target/example.jar")
(def executable-file "target/example")

(def include-resources
  (condp = Platform/CURRENT
    Platform/MACOS ".*\\.dylib"
    Platform/WINDOWS ".*\\.dll"
    Platform/X11 ".*\\.so"))

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
           :main 'example.main}))

(defn native [_]
  (uber nil)
  (shell
   native-image-bin
   "--initialize-at-build-time"
   "-J-Dclojure.compiler.direct-linking=true"
   "--initialize-at-run-time=io.github.humbleui.jwm.impl.RefCounted$_FinalizerHolder"
   "--initialize-at-run-time=io.github.humbleui.jwm.impl.Managed"
   "--no-fallback"
   "--report-unsupported-elements-at-runtime"
   (str "-H:IncludeResources=" include-resources)
   "-H:+JNI"
   "-H:+ReportExceptionStackTraces"
   "-jar"
   uber-file
   executable-file))
