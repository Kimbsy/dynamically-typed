(ns dynamically-typed.scenes.level-03
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

(declare reset-level)

(defn update-level
  [state]
  (-> state
      player/reset-player-flags
      collision/update-state
      pickup/remove-finished-pickups
      sprite/update-state
      particle/clear-particles
      command/decay-display-delays
      ((common/check-victory-fn :level-04))))

(defn draw-level
  [state]
  (c/draw-background! common/dark-grey)
  (sprite/draw-scene-sprites! state)
  (command/draw-commands state))

(defn init-platforms
  []
  [(platform/world-top)
   (platform/world-left)
   (platform/world-right)
   (platform/->platform [0 400] 800 50)
   (platform/->platform [0 500] 1000 50)
   (platform/->platform [1200 500] 1000 50)])

(defn sprites
  []
  (concat [(player/init-player [100 450])
           (goal/->goal [1125 443])
           (pickup/->pickup [800 850]
                            {:reset (command/->command ["reset"]
                                                       reset-level
                                                       :display-delay 65
                                                       :green-delay 100)})]
          (init-platforms)))

(defn commands
  [green-delay?]
  {:jump (command/->command ["jump"] player/jump :green-delay (if green-delay? 40 0))
   :dash (command/->command ["dash"] player/dash :green-delay (if green-delay? 20 0))})

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
  [state]
  {:update-fn update-level
   :draw-fn   draw-level
   :sprites   (sprites)
   :commands  (commands true)
   :key-fns   (key-pressed-fns)
   :colliders (colliders)})
