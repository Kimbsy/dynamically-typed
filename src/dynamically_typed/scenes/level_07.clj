(ns dynamically-typed.scenes.level-07
  (:require [clunk.collision :as collision]
            [clunk.core :as c]
            [clunk.palette :as p]
            [clunk.shape :as shape]
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
      ((common/check-victory-fn :level-08))))

(defn draw-level
  [state]
  (c/draw-background! common/dark-grey)
  (sprite/draw-scene-sprites! state)
  (command/draw-commands state)

  ;; hide platform seam
  (shape/fill-rect! [68 287] [31 15] p/grey)
  (shape/fill-rect! [450 752] [300 150] p/grey))

(defn init-platforms
  []
  [(platform/floor)
   (platform/->platform [0 300] 200 30)
   (platform/->platform [85 240] 30 120)
   (platform/->platform [530 625] 30 350)
   (platform/->platform [670 625] 30 350)])

(defn pickups
  []
  [(pickup/->pickup [1142 700]
                    {:turn (command/->command ["turn"]
                                              player/turn
                                              :display-delay 65
                                              :green-delay 100)})
   (pickup/->pickup [970 700]
                    {:dive (command/->command ["dive"]
                                              player/dive
                                              :display-delay 65
                                              :green-delay 100)})
   (pickup/->pickup [800 700]
                    {:reset (command/->command ["reset"]
                                               reset-level
                                               :display-delay 65
                                               :green-delay 100)})])

(defn sprites
  []
  (concat [(player/init-player [50 230])
           (goal/->goal [600 718])]
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
