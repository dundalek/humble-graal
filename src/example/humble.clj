(set! *warn-on-reflection* true)
(ns example.humble
  (:gen-class)
  (:require
   [io.github.humbleui.app :as app]
   [io.github.humbleui.ui :as ui]))

;; Replacement for `ui/start-app!` that does not start a separate thread.
;; Workaround for a window not showing when compiled with Graal native image on macOS.
(defmacro start-app! [& body]
  `(app/start
    (fn []
      ~@body)))

(defn -main [& _]
  (start-app!
   (ui/window
    {:title "HumbleUI"
     :bg-color 0xFFFFFFFF}
    (ui/default-theme
     {}
     (ui/center
      (ui/label "Hello, World!"))))))
