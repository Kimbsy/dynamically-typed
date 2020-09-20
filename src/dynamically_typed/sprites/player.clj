(ns dynamically-typed.sprites.player
  (:require [quip.sprite :as qpsprite]
            [dynamically-typed.utils :as u]
            [dynamically-typed.sound :as sound]
            [quip.utils :as qpu]))

(defn init-player
  ([]
   (init-player [100 70]))
  ([pos]
   (-> (qpsprite/animated-sprite :player
                                 pos
                                 32
                                 32
                                 "img/player.png"
                                 :update-fn (comp qpsprite/update-animated-sprite
                                                  u/apply-gravity
                                                  u/apply-friction))
       (merge {:landed    false
               :direction [1 1]}))))

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

(defn turn
  [{:keys [current-scene] :as state}]
  (let [sprites     (get-in state [:scenes current-scene :sprites])
        non-players (remove #(#{:player} (:sprite-group %)) sprites)
        player      (first (filter #(#{:player} (:sprite-group %)) sprites))
        direction   (:direction player)]
    (do (prn "**TURNING**")
        (sound/turn)
        (assoc-in state
                  [:scenes current-scene :sprites]
                  (conj non-players
                        (-> player
                            (update :direction (fn [d]
                                                 (map * d [-1 1])))))))))

(defn top-hit?
  [vel-offset-y ply-y plf-y1 plf-x1 plf-x2 ply-x1 ply-x2]
  (and (< ply-y (+ plf-y1 vel-offset-y))
       (<= plf-x1 ply-x2)
       (<= ply-x1 plf-x2)))

(defn bottom-hit?
  [vel-offset-y ply-y plf-y2 plf-x1 plf-x2 ply-x1 ply-x2]
  (and (< (+ plf-y2 vel-offset-y) ply-y)
       (<= plf-x1 ply-x2)
       (<= ply-x1 plf-x2)))

(defn left-hit?
  [vel-offset-x ply-x plf-x1 plf-y1 plf-y2 ply-y1 ply-y2]
  (and (< ply-x (+ plf-x1 vel-offset-x))
       (<= plf-y1 ply-y2)
       (<= ply-y1 plf-y2)))

(defn right-hit?
  [vel-offset-x ply-x plf-x2 plf-y1 plf-y2 ply-y1 ply-y2]
  (and (< (+ plf-x2 vel-offset-x) ply-x)
       (<= plf-y1 ply-y2)
       (<= ply-y1 plf-y2)))

(defn player-hit-platform
  [{[vx vy]       :vel
    [ply-x ply-y] :pos
    ply-w         :w
    ply-h         :h
    :as           player}
   {[plf-x plf-y] :pos
    plf-w         :w
    plf-h         :h
    :as           platform}]
  (let [ply-x1 (- ply-x (/ ply-w 2))
        ply-x2 (+ ply-x (/ ply-w 2))
        ply-y1 (- ply-y (/ ply-h 2))
        ply-y2 (+ ply-y (/ ply-h 2))
        plf-x1 (- plf-x (/ plf-w 2))
        plf-x2 (+ plf-x (/ plf-w 2))
        plf-y1 (- plf-y (/ plf-h 2))
        plf-y2 (+ plf-y (/ plf-h 2))
        vel-offset-x (if (< (/ ply-w 2) (Math/abs vx))
                       (- vx (/ ply-w 2))
                       0)
        vel-offset-y (if (< (/ ply-h 2) (Math/abs vy))
                       (- vy (/ ply-h 2))
                       0)]
    (cond
      (top-hit? vel-offset-y ply-y plf-y1 plf-x1 plf-x2 ply-x1 ply-x2)
      (-> player
          (update :vel (fn [[vx vy]] [vx 0]))
          (update :pos (fn [[x y]] [x (- plf-y1 (/ ply-h 2))]))
          (assoc :landed true))

      (bottom-hit? vel-offset-y ply-y plf-y2 plf-x1 plf-x2 ply-x1 ply-x2)
      (-> player
          (update :vel (fn [[vx vy]] [vx 0]))
          (update :pos (fn [[x y]] [x (inc (+ plf-y2 (/ ply-h 2)))]))
          (assoc :landed false))

      (left-hit? vel-offset-x ply-x plf-x1 plf-y1 plf-y2 ply-y1 ply-y2)
      (-> player
          (update :vel (fn [[vx vy]] [0 vy]))
          (update :pos (fn [[x y]] [(dec (- plf-x1 (/ ply-w 2))) y]))
          (assoc :landed false))

      (right-hit? vel-offset-x ply-x plf-x2 plf-y1 plf-y2 ply-y1 ply-y2)
      (-> player
          (update :vel (fn [[vx vy]] [0 vy]))
          (update :pos (fn [[x y]] [(inc (+ plf-x2 (/ ply-w 2))) y]))
          (assoc :landed false))

      :else
      player)))
