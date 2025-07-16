(ns dynamically-typed.scenes.menu
  (:require [clunk.audio :as audio]
            [clunk.core :as c]
            [clunk.input :as i]
            [clunk.palette :as p]
            [clunk.scene :as scene]
            [clunk.shape :as shape]
            [clunk.sprite :as sprite]
            [clunk.util :as u]
            [dynamically-typed.common :as common]
            [dynamically-typed.scenes.credits :as credits]
            [dynamically-typed.sprites.button :as button]))

(defn update-menu
  [state]
  (-> state
      sprite/update-state))

(defn draw-header!
  []
  (let [points [[-30 215]
                [565 215]
                [665 115]
                [1200 115]
                [1200 38]
                [635 38]
                [535 140]
                [-30 140]]]
    (shape/fill-concave-poly! [0 -15] points common/player-pink)
    (shape/fill-concave-poly! [30 85] points common/platform-blue)))

(defn draw-menu
  [state]
  (c/draw-background! common/dark-grey)
  (draw-header!)
  (sprite/draw-scene-sprites! state))

(defn on-click-play
  [state e]
  (scene/transition state :intro
                    :transition-length 30
                    :init-fn (fn [{:keys [music-source] :as state}]
                               (audio/stop! music-source)
                               (-> state
                                   (assoc :music-source (audio/play! :driving :loop? true))
                                   (assoc-in [:scenes :credits :commands] (credits/commands))))))

(defn on-click-quit
  [state e]
  (c/quit! state))

(defn on-click-credits
  [state e]
  (scene/transition state :credits
                    :transition-length 30
                    :init-fn (fn [{:keys [music-source] :as state}]
                               (audio/stop! music-source)
                               (-> state
                                   (assoc :music-source (audio/play! :glitter :loop? true))
                                   common/unclick-all-buttons))))

(defn text-sprites
  [{:keys [window]}]
  (let [[w h] (u/window-size window)]
    [(sprite/text-sprite :title
                         [(+ (* w 1/2) 4) (+ (* h 1/4) 4)]
                         "Dynamically Typed"
                         :color p/white
                         :font-size 100)
     (sprite/text-sprite :title
                         [(* w 1/2) (* h 1/4)]
                         "Dynamically Typed"
                         :color common/dark-grey
                         :font-size 100)]))

(defn button-sprites
  [{:keys [window]}]
  (let [[w h] (u/window-size window)]
    [(i/add-on-click
      (button/button-sprite [(* w 1/2) (* h 1/2)] "Play")
      on-click-play)
     (i/add-on-click
      (button/button-sprite [(* w 1/2) (* h 2/3)] "Credits")
      on-click-credits)
     (i/add-on-click
      (button/button-sprite [(* w 1/2) (* h 5/6)] "Quit")
      on-click-quit)]))

(defn sprites
  [state]
  (concat (text-sprites state)
          (button-sprites state)))

(defn init
  [state]
  {:update-fn update-menu
   :draw-fn draw-menu
   :sprites (sprites state)})
