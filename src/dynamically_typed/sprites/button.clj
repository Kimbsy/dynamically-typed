(ns dynamically-typed.sprites.button
  (:require [clunk.shape :as shape]
            [clunk.sprite :as sprite]
            [dynamically-typed.common :as common]
            [clunk.palette :as p]))

(def title-text-size 120)
(def large-text-size 50)

(defn draw-button
  [state {:keys [pos font-size button-size bg-color] :as button}]
  (shape/fill-rect! (mapv - pos (mapv #(/ % 2) button-size)) button-size bg-color)
  (sprite/draw-text-sprite! state button))

(defn button-sprite
  [pos content & {:keys [font-size] :or {font-size large-text-size}}]
  (sprite/text-sprite :button
                      pos
                      content
                      :font-size font-size
                      :draw-fn draw-button
                      :extra {:button-size [200 100] ; override default content-based size
                              :bg-color common/button-teal
                              :debug-color p/red}))
