(ns dynamically-typed.scenes.level-05
  (:require [dynamically-typed.command :as command]
            [dynamically-typed.platform :as platform]
            [dynamically-typed.player :as player]
            [dynamically-typed.utils :as u]
            [quip.collision :as qpcollision]
            [quip.scene :as qpscene]
            [quip.utils :as qpu]))

(defn update-level
  [state]
  (-> state
      player/reset-player-flags
      qpcollision/update-collisions
      qpscene/update-scene-sprites))

(defn draw-level
  [state]
  (qpu/background u/dark-grey)
  (qpscene/draw-scene-sprites state)
  (command/draw-commands state))

(defn init-platforms
  []
  [(platform/world-top)
   (platform/world-left)
   (platform/world-right)
   (platform/->platform [0 800] 1000 1300)
   (platform/->platform [1200 0] 1200 600)
   (platform/->platform [650 800] 100 800)
   (platform/->platform [1200 800] 1000 500)])

(defn sprites
  []
  (concat [(player/init-player)]
          (init-platforms)))

(defn commands
  []
  {:jump (command/->command ["jump"] player/jump)
   :dash (command/->command ["dash"] player/dash)})

(defn colliders
  []
  [(qpcollision/collider
    :player
    :platforms
    player/player-hit-platform
    qpcollision/identity-collide-fn
    :collision-detection-fn qpcollision/w-h-rects-collide?)])

(defn reset-level
  [{:keys [current-scene] :as state}]
  (prn "resetting level 5")
  (-> state
      (assoc-in [:scenes current-scene :sprites] (sprites))
      (assoc-in [:scenes current-scene :commands] (commands))
      (assoc-in [:scenes current-scene :colliders] (colliders))))

(defn key-pressed-fns
  []
  [command/handle-keypress
   command/clear
   (u/reset-handler reset-level)])

(defn init
  []
  {:update-fn       update-level
   :draw-fn         draw-level
   :sprites         (sprites)
   :commands        (commands)
   :key-pressed-fns (key-pressed-fns)
   :colliders       (colliders)})
