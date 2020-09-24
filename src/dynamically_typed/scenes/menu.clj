(ns dynamically-typed.scenes.menu
  (:require [dynamically-typed.scenes.credits :as credits]
            [dynamically-typed.sound :as sound]
            [dynamically-typed.utils :as u]
            [quil.core :as q]
            [quip.scene :as qpscene]
            [quip.sprite :as qpsprite]
            [quip.sprites.button :as qpbutton]
            [quip.utils :as qpu]))

(defn update-menu
  [state]
  (-> state
      qpscene/update-scene-sprites))

(defn draw-header
  []
  ;; (qpu/fill qpu/white)
  ;; (q/rect 270 90 660 140)
  (let [points [[-30 140]
                [535 140]
                [635 38]
                [1200 38]
                [1200 115]
                [665 115]
                [565 215]
                [-30 215]]]
    (qpu/fill u/player-pink)
    (q/begin-shape)
    (->> points
         (map (fn [p] (map + p [0 -15])))
         (mapv #(apply q/vertex %)))
    (q/end-shape)
    (qpu/fill u/platform-blue)
    (q/begin-shape)
    (->> points
         (map (fn [p] (map + [30 85] p)))
         (mapv #(apply q/vertex %)))
    (q/end-shape)))

(defn draw-menu
  [state]
  (qpu/background u/dark-grey)
  (draw-header)
  (qpscene/draw-scene-sprites state))

(defn on-click-play
  [state e]
  (qpscene/transition state :intro
                      :transition-length 30
                      :init-fn (fn [state]
                                 (sound/stop-music)
                                 (sound/loop-track :driving)
                                 (-> state
                                     (assoc-in [:scenes :credits :commands] (credits/commands))
                                     u/unclick-all-buttons))))

(defn on-click-quit
  [state e]
  (q/exit))

(defn on-click-credits
  [state e]
  (qpscene/transition state :credits
                      :transition-length 30
                      :init-fn (fn [state]
                                 (sound/stop-music)
                                 (sound/loop-track :glitter)
                                 (u/unclick-all-buttons state))))

(defn text-sprites
  []
  [(qpsprite/text-sprite "Dynamically Typed"
                         [(+ (* (q/width) 1/2) 4)
                          (+ (* (q/height) 1/4) 4)]
                         :color qpu/white
                         :font "font/UbuntuMono-Regular.ttf"
                         :size 100)
   (qpsprite/text-sprite "Dynamically Typed"
                         [(* (q/width) 1/2)
                          (* (q/height) 1/4)]
                         :color u/dark-grey
                         :font "font/UbuntuMono-Regular.ttf"
                         :size 100)])

(defn button-sprites
  []
  [(qpbutton/button-sprite "Play"
                           [(* (q/width) 1/2) (* (q/height) 1/2)]
                           :on-click on-click-play
                           :color u/button-teal
                           :content-color qpu/white
                           :font "font/UbuntuMono-Regular.ttf")
   (qpbutton/button-sprite "Quit"
                           [(* (q/width) 1/2) (* (q/height) 2/3)]
                           :on-click on-click-quit
                           :color u/button-teal
                           :content-color qpu/white
                           :font "font/UbuntuMono-Regular.ttf")
   (qpbutton/button-sprite "Credits"
                           [(* (q/width) 1/2) (* (q/height) 5/6)]
                           :on-click on-click-credits
                           :color u/button-teal
                           :content-color qpu/white
                           :font "font/UbuntuMono-Regular.ttf")])

(defn sprites
  []
  (concat (text-sprites)
          (button-sprites)))

(defn init
  []
  {:update-fn          update-menu
   :draw-fn            draw-menu
   :sprites            (sprites)
   :mouse-pressed-fns  [qpbutton/handle-buttons-pressed]
   :mouse-released-fns [qpbutton/handle-buttons-released]})
