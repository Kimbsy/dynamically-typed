(ns dynamically-typed.platform
  (:require [quil.core :as q]
            [quip.sprite :as qpsprite]
            [quip.utils :as qpu]))

(defn draw-platform
  [{[x y] :pos w :w h :h}]
  (qpu/fill qpu/grey)
  (q/no-stroke)
  (q/rect (- x (/ w 2))
          (- y (/ h 2))
          w
          h))

(defn ->platform
  [pos w h]
  {:sprite-group :platforms
   :uuid         (java.util.UUID/randomUUID)
   :pos          pos
   :rotation     0
   :w            w
   :h            h
   :animated?    false
   :static?      true
   :update-fn    identity
   :draw-fn      draw-platform
   :bounds-fn    qpsprite/default-bounding-poly})

(defn floor [] (->platform [600 775] 1200 50))
(defn world-top [] (->platform [600 0] 1200 2))
(defn world-bottom [] (->platform [600 800] 1200 2))
(defn world-left [] (->platform [0 400] 2 800))
(defn world-right [] (->platform [1200 400] 2 800))

(defn world-bounds
  []
  [(world-top)
   (world-bottom)
   (world-left)
   (world-right)])
