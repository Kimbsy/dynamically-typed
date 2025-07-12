(ns dynamically-typed.scenes.level-04
  (:require [clunk.collision :as collision]
            [clunk.core :as c]
            [clunk.sprite :as sprite]
            [dynamically-typed.command :as command]
            [dynamically-typed.common :as common]
            [dynamically-typed.sprites.goal :as goal]
            [dynamically-typed.sprites.particle :as particle]
            [dynamically-typed.sprites.pickup :as pickup]
            [dynamically-typed.sprites.platform :as platform]
            [dynamically-typed.sprites.player :as player]))

(defn update-level
  [state]
  (-> state
      player/reset-player-flags
      collision/update-state
      pickup/remove-finished-pickups
      sprite/update-state
      particle/clear-particles
      command/decay-display-delays
      ((common/check-victory-fn :level-05))))

(defn draw-level
  [state]
  (c/draw-background! common/dark-grey)
  (sprite/draw-scene-sprites! state)
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
  [state]
  {:update-fn update-level
   :draw-fn   draw-level
   :sprites   (sprites)
   :commands  (commands)
   :key-fns   (key-pressed-fns)
   :colliders (colliders)})
