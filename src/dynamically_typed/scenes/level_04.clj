(ns dynamically-typed.scenes.level-04
  (:require [dynamically-typed.command :as command]
            [dynamically-typed.sprites.goal :as goal]
            [dynamically-typed.sprites.particle :as particle]
            [dynamically-typed.sprites.pickup :as pickup]
            [dynamically-typed.sprites.platform :as platform]
            [dynamically-typed.sprites.player :as player]
            [dynamically-typed.utils :as u]
            [quip.collision :as qpcollision]
            [quip.scene :as qpscene]
            [quip.utils :as qpu]))

(defn update-level
  [state]
  (-> state
      player/reset-player-flags
      qpcollision/update-collisions
      pickup/remove-finished-pickups
      qpscene/update-scene-sprites
      particle/clear-particles
      command/decay-display-delays
      ((u/check-victory-fn :level-05))))

(defn draw-level
  [state]
  (qpu/background u/dark-grey)
  (qpscene/draw-scene-sprites state)
  (command/draw-commands state))

(defn init-platforms
  []
  [(platform/floor)
   (platform/->platform [700 200] 1200 25)
   (platform/->platform [500 450] 1200 25)])

(defn sprites
  []
  (concat [(player/init-player [150 70])
           (goal/->goal [200 718])
           (pickup/->pickup [1142 133]
                            {:turn (command/->command ["turn"]
                                                      player/turn
                                                      :display-delay 65
                                                      :green-delay 100)})]
          (init-platforms)
          (platform/world-bounds)))

(defn commands
  []
  {:jump  (command/->command ["jump"] player/jump :green-delay 40)
   :dash  (command/->command ["dash"] player/dash :green-delay 20)})

(defn colliders
  []
  [(platform/platform-collider :player)
   (platform/platform-collider :particles)
   (pickup/pickup-collider)
   (goal/goal-collider)])

(defn reset-level
  [{:keys [current-scene] :as state}]
  (-> state
      (assoc-in [:scenes current-scene :sprites] (sprites))
      (assoc-in [:scenes current-scene :commands] (commands))
      (assoc-in [:scenes current-scene :colliders] (colliders))))

(defn key-pressed-fns
  []
  [command/handle-keypress])

(defn init
  []
  {:update-fn       update-level
   :draw-fn         draw-level
   :sprites         (sprites)
   :commands        (commands)
   :key-pressed-fns (key-pressed-fns)
   :colliders       (colliders)})
