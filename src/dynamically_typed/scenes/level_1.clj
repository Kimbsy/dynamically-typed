(ns dynamically-typed.scenes.level-1
  (:require [dynamically-typed.command :as command]
            [dynamically-typed.utils :as u]
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
  state)

(defn run
  [state]
  (prn "**RUNNING**")
  state)

(defn init
  []
  {:update-fn       update-level
   :draw-fn         draw-level
   :sprites         [(init-player)]
   :commands        {:jump (command/->command ["jump"  "hop" "leap"]
                                            jump)
                     :run  (command/->command ["run" "move" "go"]
                                            run)
                     }
   :key-pressed-fns (key-pressed-fns)})
