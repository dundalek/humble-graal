;; Based on https://github.com/HumbleUI/JWM/blob/main/examples/empty/java/Example.java
(set! *warn-on-reflection* true)
(ns example.skija
  (:gen-class)
  (:import
   [io.github.humbleui.jwm App EventWindowClose EventWindowCloseRequest Window]
   [io.github.humbleui.jwm.skija EventFrameSkija LayerGLSkija]
   [io.github.humbleui.skija Canvas Color Paint]
   [io.github.humbleui.types Rect]
   [java.util.function Consumer]))

(defn paint [^Window window ^Canvas canvas width height]
  (let [scale (.getScale (.getScreen window))]
    (.clear canvas (Color/makeRGB 0x26 0x46 0x53) #_0xFF264653)
    (with-open [paint (Paint.)]
      (.setColor paint (Color/makeRGB 0xE7 0x6F 0x51) #_0xFFe76f51)
      (.drawRect canvas (Rect/makeXYWH (* 10 scale) (* 10 scale) (* 10 scale) (* 10 scale)) paint)
      (.drawRect canvas (Rect/makeXYWH (- width (* 20 scale)) (* 10 scale) (* 10 scale) (* 10 scale)) paint)
      (.drawRect canvas (Rect/makeXYWH (* 10 scale) (- height (* 20 scale)) (* 10 scale) (* 10 scale)) paint)
      (.drawRect canvas (Rect/makeXYWH (- width (* 20 scale)) (- height (* 20 scale)) (* 10 scale) (* 10 scale)) paint)
      (.drawRect canvas (Rect/makeXYWH (- (/ width 2) (* 5 scale)) (- (/ height 2) (* 5 scale)) (* 10 scale) (* 10 scale)) paint))))

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
                       (.close window)

                       (instance? EventFrameSkija e)
                       (let [s (.getSurface ^EventFrameSkija e)]
                         (paint window (.getCanvas s) (.getWidth s) (.getHeight s))))))]
    (doto window
      (.setEventListener listener)
      (.setTitle "Empty")
      (.setLayer (LayerGLSkija.))
      (.setWindowSize (int (* 300 scale)) (int (* 600 scale)))
      (.setWindowPosition (int (* 300 scale)) (int (* 200 scale)))
      (.setVisible true))))

(defn -main []
  (App/start make-window))

(comment
  (-main))
