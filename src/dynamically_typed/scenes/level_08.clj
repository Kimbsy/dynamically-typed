(ns dynamically-typed.scenes.level-08
  (:require [clunk.collision :as collision]
            [clunk.core :as c]
            [clunk.sprite :as sprite]
            [dynamically-typed.command :as command]
            [dynamically-typed.common :as common]
            [dynamically-typed.sprites.goal :as goal]
            [dynamically-typed.sprites.hold :as hold]
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
      ((common/check-victory-fn :level-09))))

(defn draw-level
  [state]
  (c/draw-background! common/dark-grey)
  (sprite/draw-scene-sprites! state)
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
  [state]
  {:update-fn update-level
   :draw-fn   draw-level
   :sprites   (concat (sprites)
                      (pickups))
   :commands  (commands)
   :key-fns   (key-pressed-fns)
   :colliders (colliders)})
