(ns dynamically-typed.sprites.hold
  (:require [dynamically-typed.utils :as u]
            [quil.core :as q]
            [quip.sprite :as qpsprite]
            [quip.utils :as qpu]))

(defn draw-hold
  [{[x y] :pos w :w h :h}]
  (apply q/fill (conj u/platform-blue 70))
  (q/rect (- x (/ w 2))
          (- y (/ h 2))
          w
          h
          10))

(defn ->hold
  ([pos]
   (->hold pos 80 80))
  ([pos w h]
   {:sprite-group :holds
    :uuid         (java.util.UUID/randomUUID)
    :pos          pos
    :rotation     0
    :w            w
    :h            h
    :animated?    false
    :static?      true
    :update-fn    identity
    :draw-fn      draw-hold
    :bounds-fn    qpsprite/default-bounding-poly}))
