(ns dynamically-typed.scenes.level-05
  (:require [dynamically-typed.command :as command]
            [dynamically-typed.sound :as sound]
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
      ((u/check-victory-fn :credits (fn [state]
                                      (sound/stop-music)
                                      (sound/loop-track :glitter)
                                      state)))))

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
   (platform/->platform [0 800] 1000 1200)
   (platform/->platform [1200 0] 1200 600)
   (platform/->platform [650 800] 100 800)
   (platform/->platform [1200 800] 1000 500)])

(defn sprites
  []
  (concat [(player/init-player)
           (goal/->goal [1000 518])
           (pickup/->pickup [800 850]
                            {:reset (command/->command ["reset"]
                                                       reset-level
                                                       :display-delay 65
                                                       :green-delay 100)})]
          (init-platforms)))

(defn commands
  []
  {:jump  (command/->command ["jump"] player/jump)
   :dash  (command/->command ["dash"] player/dash)
   :turn  (command/->command ["turn"] player/turn)})

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
  (prn "resetting level 5")
  (-> state
      (assoc-in [:scenes current-scene :sprites] (sprites))
      (assoc-in [:scenes current-scene :commands]
                (assoc (commands) :reset (command/->command ["reset"] reset-level)))
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
