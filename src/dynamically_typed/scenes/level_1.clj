(ns dynamically-typed.scenes.level-1
  (:require [dynamically-typed.command :as command]
            [dynamically-typed.utils :as u]
            [dynamically-typed.sound :as sound]
            [quip.scene :as qpscene]
            [quip.sprite :as qpsprite]
            [quip.utils :as qpu]))

(defn update-level
  [state]
  state)

(defn draw-level
  [state]
  (qpu/background u/dark-grey)
  (qpscene/draw-scene-sprites state))

(defn init-player
  []
  (qpsprite/animated-sprite :player
                            [200 200]
                            20
                            20
                            "img/player.png"))

(defn key-pressed-fns
  []
  [command/handle-keypress
   command/clear])

(defn jump
  [state]
  (prn "**JUMPING**")
  (sound/jump)
  state)

(defn dash
  [state]
  (prn "**DASHING**")
  (sound/dash)
  state)

(defn init
  []
  {:update-fn       update-level
   :draw-fn         draw-level
   :sprites         [(init-player)]
   :commands        {:jump (command/->command ["jump"  "hop" "leap"] jump)
                     :dash  (command/->command ["dash" "run" "move" "go"] dash)}
   :key-pressed-fns (key-pressed-fns)})
