(ns dynamically-typed.platform
  (:require [quil.core :as q]
            [quip.sprite :as qpsprite]
            [quip.utils :as qpu]))

(defn ->platform
  [pos w h]
  (qpsprite/static-sprite :platforms
                          pos
                          w
                          h
                          "img/player.png" ; hack, we're not using an
                                        ; image yet
                          :draw-fn (fn [{[x y] :pos w :w h :h}]
                                     (qpu/fill qpu/grey)
                                     (q/no-stroke)
                                     (q/rect (- x (/ w 2))
                                             (- y (/ h 2))
                                             w
                                             h))))
