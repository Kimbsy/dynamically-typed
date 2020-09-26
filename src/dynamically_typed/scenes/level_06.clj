(ns dynamically-typed.scenes.level-06
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

(declare reset-level)

(defn update-level
  [state]
  (-> state
      player/reset-player-flags
      qpcollision/update-collisions
      pickup/remove-finished-pickups
      qpscene/update-scene-sprites
      particle/clear-particles
      command/decay-display-delays
      ((u/check-victory-fn :level-07))))

(defn draw-level
  [state]
  (qpu/background u/dark-grey)
  (qpscene/draw-scene-sprites state)
  (command/draw-commands state))

(defn init-platforms
  []
  [(platform/world-top)
   (platform/world-left)
   (platform/->platform [0 300] 1200 50)
   (platform/->platform [0 775] 1200 50)])

(defn sprites
  []
  (concat [(player/init-player)
           (goal/->goal [75 718])
           (pickup/->pickup [800 850]
                            {:reset (command/->command ["reset"]
                                                       reset-level
                                                       :display-delay 65
                                                       :green-delay 100)})]
          (init-platforms)))

(defn commands
  [green-delay?]
  {:jump  (command/->command ["jump"] player/jump :green-delay (if green-delay? 40 0))
   :dash  (command/->command ["dash"] player/dash :green-delay (if green-delay? 20 0))
   :turn  (command/->command ["turn"] player/turn :green-delay (if green-delay? 60 0))})

(defn colliders
  []
  [(platform/platform-collider :player)
   (platform/platform-collider :particles)
   (goal/goal-collider)
   (assoc (pickup/pickup-collider)
          :collision-detection-fn
          (fn [{[x y] :pos :as player} _]
            (< 820 y)))])

(defn reset-level
  [{:keys [current-scene] :as state}]
  (-> state
      (assoc-in [:scenes current-scene :sprites] (sprites))
      (assoc-in [:scenes current-scene :commands]
                (assoc (commands false)
                       :reset
                       (command/->command ["reset"] reset-level)))
      (assoc-in [:scenes current-scene :colliders] (colliders))))

(defn key-pressed-fns
  []
  [command/handle-keypress])

(defn init
  []
  {:update-fn       update-level
   :draw-fn         draw-level
   :sprites         (sprites)
   :commands        (commands true)
   :key-pressed-fns (key-pressed-fns)
   :colliders       (colliders)})
