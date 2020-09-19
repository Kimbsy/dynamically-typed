(ns dynamically-typed.scenes.level-05
  (:require [dynamically-typed.command :as command]
            [dynamically-typed.sound :as sound]
            [dynamically-typed.utils :as u]
            [quil.core :as q]
            [quip.collision :as qpcollision]
            [quip.scene :as qpscene]
            [quip.sprite :as qpsprite]
            [quip.utils :as qpu]
            [dynamically-typed.player :as p]))

(defn update-level
  [state]
  (-> state
      qpcollision/update-collisions
      qpscene/update-scene-sprites))

(defn draw-level
  [state]
  (qpu/background u/dark-grey)
  (qpscene/draw-scene-sprites state)
  (command/draw-commands state))

(defn init-platforms
  []
  [(qpsprite/static-sprite :platforms
                           [600 750]
                           1200
                           50
                           "img/player.png" ; hack, we're not using an
                                            ; image yet
                           :draw-fn (fn [{[x y] :pos w :w h :h}]
                                      (qpu/fill qpu/grey)
                                      (q/rect (- x (/ w 2))
                                              (- y (/ h 2))
                                              w
                                              h)))])

(defn sprites
  []
  (concat [(p/init-player)]
          (init-platforms)))

(defn commands
  []
  {:jump        (command/->command ["jump"  "hop" "leap"] p/jump)
   :dash        (command/->command ["dash" "run" "move" "go"] p/dash)
   :skip        (command/->command ["skip"] identity)
   :corroborate (command/->command ["corroborate"] identity)
   :interfere   (command/->command ["interfere"] identity)})

(defn colliders
  []
  [(qpcollision/collider
    :player
    :platforms
    p/player-landed
    identity
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
