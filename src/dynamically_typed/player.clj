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

(defn reset-player-flags
  [{:keys [current-scene] :as state}]
  (let [sprites     (get-in state [:scenes current-scene :sprites])
        non-players (remove #(#{:player} (:sprite-group %)) sprites)
        player      (first (filter #(#{:player} (:sprite-group %)) sprites))]
    (assoc-in state
              [:scenes current-scene :sprites]
              (conj non-players
                    (-> player
                        (assoc :landed false))))))

(defn jump
  [{:keys [current-scene] :as state}]
  (let [sprites     (get-in state [:scenes current-scene :sprites])
        non-players (remove #(#{:player} (:sprite-group %)) sprites)
        player      (first (filter #(#{:player} (:sprite-group %)) sprites))]
    (if (:landed player)
      (do (prn "**JUMPING**")
          (sound/jump)
          (assoc-in state
                    [:scenes current-scene :sprites]
                    (conj non-players
                          (-> player
                              (update :vel (fn [[vx vy]] [vx (- vy 5)]))
                              (update :pos (fn [[x y]] [x (- y 10)]))
                              (assoc :landed false)))))
      state)))

(defn dash
  [{:keys [current-scene] :as state}]
  (let [sprites     (get-in state [:scenes current-scene :sprites])
        non-players (remove #(#{:player} (:sprite-group %)) sprites)
        player      (first (filter #(#{:player} (:sprite-group %)) sprites))
        direction   (:direction player)]
    (do (prn "**DASHING**")
        (sound/dash)
        (assoc-in state
                  [:scenes current-scene :sprites]
                  (conj non-players
                        (-> player
                            (update :vel (fn [vel]
                                           (map + vel
                                                (map * direction
                                                     [10 0]))))))))))

(defn player-landed
  [p]
  (-> p
      (update :vel (fn [[vx vy]]
                     [vx 0]))
      (assoc :landed true)))
