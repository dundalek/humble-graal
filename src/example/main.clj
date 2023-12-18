;; Based on https://github.com/HumbleUI/JWM/blob/main/examples/empty/java/Example.java
(set! *warn-on-reflection* true)
(ns example.main
  (:gen-class)
  (:import
   [io.github.humbleui.jwm App EventWindowClose EventWindowCloseRequest]
   [java.util.function Consumer]))

(defn make-window []
  (let [window (App/makeWindow)
        screen (App/getPrimaryScreen)
        scale (.getScale screen)
        listener (reify Consumer
                   (accept [_ e]
                     (cond
                       (instance? EventWindowClose e)
                       (if (zero? (count (App/_windows)))
                         (App/terminate)
                         nil)

                       (instance? EventWindowCloseRequest e)
                       (.close window))))]
    (doto window
      (.setTitle "Empty JWM")
      (.setEventListener listener)
      (.setWindowSize (int (* 300 scale)) (int (* 600 scale)))
      (.setWindowPosition (int (* 300 scale)) (int (* 200 scale)))
      (.setVisible true))))

(defn -main []
  (App/start make-window))

(comment
  (-main))
