(ns dynamically-typed.sprites.hold
  (:require [clunk.palette :as p]
            [clunk.shape :as shape]
            [clunk.sprite :as qpsprite]
            [dynamically-typed.common :as common]))

(defn draw-hold
  [{[x y] :pos [w h] :size}]
  ;; @TODO: corner radius 10px
  (shape/fill-rect! [(- x (/ w 2)) (- y (/ h 2))] [w h] (assoc common/platform-blue 3 0.275)))

(defn ->hold
  ([pos]
   (->hold pos 80 80))
  ([pos w h]
   {:sprite-group :holds
    :uuid         (java.util.UUID/randomUUID)
    :pos          pos
    :rotation     0
    :size         [w h]
    :animated?    false
    :static?      true
    :update-fn    identity
    :draw-fn      draw-hold
    :bounds-fn    qpsprite/default-bounding-poly
    :debug-color  p/red}))
