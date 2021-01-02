(ns dynamically-typed.scenes.level-08
  (:require [dynamically-typed.command :as command]
            [dynamically-typed.sprites.goal :as goal]
            [dynamically-typed.sprites.hold :as hold]
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
      ((u/check-victory-fn :level-09))))

(defn draw-level
  [state]
  (qpu/background u/dark-grey)
  (qpscene/draw-scene-sprites state)
  (command/draw-commands state))

(defn init-platforms
  []
  [(platform/floor)])

(defn pickups
  []
  [(pickup/->pickup [100 600]
                    {:turn (command/->command ["grab"]
                                              player/grab
                                              :display-delay 65
                                              :green-delay 100)})
   (pickup/->pickup [100 300]
                    {:dive (command/->command ["dash"]
                                              player/dash
                                              :display-delay 65
                                              :green-delay 100)})])

(defn init-holds
  []
  [(hold/->hold [110 600] 200 800)])

(defn sprites
  []
  (concat [(player/init-player [100 700])
           (goal/->goal [1125 718])]
          (init-platforms)
          (init-holds)
          (platform/world-bounds)))

(defn commands
  []
  {:jump (command/->command ["jump"] player/jump :green-delay 20)})

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
   :sprites         (concat (sprites)
                            (pickups))
   :commands        (commands)
   :key-pressed-fns (key-pressed-fns)
   :colliders       (colliders)})
