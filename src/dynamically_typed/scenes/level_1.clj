(ns dynamically-typed.scenes.level-1
  (:require [dynamically-typed.command :as command]
            [dynamically-typed.sound :as sound]
            [dynamically-typed.utils :as u]
            [quil.core :as q]
            [quip.collision :as qpcollision]
            [quip.scene :as qpscene]
            [quip.sprite :as qpsprite]
            [quip.utils :as qpu]))

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

(defn init-player
  []
  (-> (qpsprite/animated-sprite :player
                                [200 200]
                                32
                                32
                                "img/player.png"
                                :update-fn (comp qpsprite/update-animated-sprite
                                                 u/apply-gravity
                                                 u/apply-friction))
      (merge {:landed    false
              :direction [1 1]})))

(defn init-platforms
  []
  [(qpsprite/static-sprite :platforms
                           [600 650]
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

(defn key-pressed-fns
  []
  [command/handle-keypress
   command/clear])

(defn jump
  [{:keys [current-scene] :as state}]
  (prn "**JUMPING**")
  (sound/jump)
  (let [sprites     (get-in state [:scenes current-scene :sprites])
        non-players (remove #(#{:player} (:sprite-group %)) sprites)
        player      (first (filter #(#{:player} (:sprite-group %)) sprites))]
    (prn (:vel player))
    (assoc-in state
              [:scenes current-scene :sprites]
              (conj non-players
                    (-> player
                        (update :vel (fn [[vx vy]] [vx (- vy 5)]))
                        (update :pos (fn [[x y]] [x (- y 10)]))
                        (assoc :landed false))))))

(defn dash
  [{:keys [current-scene] :as state}]
  (prn "**DASHING**")
  (sound/dash)
  (let [sprites     (get-in state [:scenes current-scene :sprites])
        non-players (remove #(#{:player} (:sprite-group %)) sprites)
        player      (first (filter #(#{:player} (:sprite-group %)) sprites))
        direction   (:direction player)]
    (assoc-in state
              [:scenes current-scene :sprites]
              (conj non-players
                    (-> player
                        (update :vel (fn [vel]
                                       (map + vel
                                            (map * direction
                                                 [10 0])))))))))

(defn player-landed
  [p]
  (-> p
      (update :vel (fn [[vx vy]]
                     [vx 0]))
      (assoc :landed true)))

(defn init
  []
  {:update-fn       update-level
   :draw-fn         draw-level
   :sprites         (concat [(init-player)]
                            (init-platforms))
   :commands        {:jump (command/->command ["jump"  "hop" "leap"] jump)
                     :dash  (command/->command ["dash" "run" "move" "go"] dash)
                     :skip (command/->command ["skip"] identity)
                     :corroborate (command/->command ["corroborate"] identity)
                     :interfere (command/->command ["interfere"] identity)}
   :key-pressed-fns (key-pressed-fns)
   :colliders [(qpcollision/collider
                :player
                :platforms
                player-landed
                identity
                :collision-detection-fn qpcollision/w-h-rects-collide?)]})
