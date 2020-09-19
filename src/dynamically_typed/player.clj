(ns dynamically-typed.player
  (:require [quip.sprite :as qpsprite]
            [dynamically-typed.utils :as u]
            [dynamically-typed.sound :as sound]))

(defn init-player
  []
  (-> (qpsprite/animated-sprite :player
                                [100 7]
                                32
                                32
                                "img/player.png"
                                :update-fn (comp qpsprite/update-animated-sprite
                                                 u/apply-gravity
                                                 u/apply-friction))
      (merge {:landed    false
              :direction [1 1]})))

(defn jump
  [{:keys [current-scene] :as state}]
  (prn "**JUMPING**")
  (sound/jump)
  (let [sprites     (get-in state [:scenes current-scene :sprites])
        non-players (remove #(#{:player} (:sprite-group %)) sprites)
        player      (first (filter #(#{:player} (:sprite-group %)) sprites))]
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
