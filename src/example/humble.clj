(set! *warn-on-reflection* true)
(ns example.humble
  (:gen-class)
  (:require
   [io.github.humbleui.ui :as ui]))

(defn -main [& _]
  (ui/start-app!
   (ui/window
    {:title "HumbleUI"
     :bg-color 0xFFFFFFFF}
    (ui/default-theme
     {}
     (ui/center
      (ui/label "Hello, World!"))))))
