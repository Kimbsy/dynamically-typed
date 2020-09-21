(ns dynamically-typed.scenes.menu
  (:require [dynamically-typed.sound :as sound]
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

(defn draw-menu
  [state]
  (q/background 190)
  (qpscene/draw-scene-sprites state))

(defn on-click-play
  [state e]
  (qpscene/transition state :level-01
                      :transition-length 30
                      :init-fn (fn [state]
                                 (sound/stop-music)
                                 (sound/loop-track :driving)
                                 (prn "PLAY")
                                 (u/unclick-all-buttons state))))

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
                                 (prn "CREDITS")
                                 (u/unclick-all-buttons state))))

(defn text-sprites
  []
  [(qpsprite/text-sprite "Dynamically Typed"
                         [(* (q/width) 1/2)
                          (* (q/height) 1/5)]
                         :color qpu/white
                         :size qpu/title-text-size)
   (qpsprite/text-sprite "home row warm-ups reccomended!"
                         [(* (q/width) 1/2)
                          (* (q/height) 5/20)]
                         :font qpu/italic-font
                         :color qpu/white)])

(defn button-sprites
  []
  [(qpbutton/button-sprite "Play"
                           [(* (q/width) 1/2) (* (q/height) 1/2)]
                           :on-click on-click-play
                           :content-color qpu/white)
   (qpbutton/button-sprite "Quit"
                           [(* (q/width) 1/2) (* (q/height) 2/3)]
                           :on-click on-click-quit
                           :content-color qpu/white)
   (qpbutton/button-sprite "Credits"
                           [(* (q/width) 1/2) (* (q/height) 5/6)]
                           :on-click on-click-credits
                           :content-color qpu/white)])

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
