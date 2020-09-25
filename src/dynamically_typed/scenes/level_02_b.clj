(ns dynamically-typed.scenes.level-02-b
  (:require [dynamically-typed.command :as command]
            [dynamically-typed.sprites.goal :as goal]
            [dynamically-typed.sprites.particle :as particle]
            [dynamically-typed.sprites.platform :as platform]
            [dynamically-typed.sprites.player :as player]
            [dynamically-typed.utils :as u]
            [quil.core :as q]
            [quip.collision :as qpcollision]
            [quip.scene :as qpscene]
            [quip.utils :as qpu]))

(defn update-level
  [state]
  (-> state
      player/reset-player-flags
      qpcollision/update-collisions
      qpscene/update-scene-sprites
      particle/clear-particles
      command/decay-display-delays
      ((u/check-victory-fn :level-03))))

(defn draw-level
  [state]
  (qpu/background u/dark-grey)
  (qpscene/draw-scene-sprites state)
  (command/draw-commands state)

  ;; hide platform seams
  (qpu/fill qpu/grey)
  (q/rect 602 738 1000 100)
  (q/rect 802 638 1000 100)
  (q/rect 1002 538 1000 100))

(defn init-platforms
  []
  [(platform/->platform [1200 490] 400 120)
   (platform/->platform [1200 600] 800 120)
   (platform/->platform [1200 710] 1200 120)
   (platform/floor)])

(defn sprites
  []
  (concat [(player/init-player)
           (goal/->goal [1125 398])]
          (init-platforms)
          (platform/world-bounds)))

(defn commands
  []
  {:jump (command/->command ["jump"] player/jump :green-delay 40)
   :dash (command/->command ["dash"] player/dash :green-delay 20)})

(defn colliders
  []
  [(platform/platform-collider :player)
   (platform/platform-collider :particles)
   (goal/goal-collider)])

(defn reset-level
  [{:keys [current-scene] :as state}]
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
