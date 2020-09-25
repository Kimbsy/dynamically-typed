(ns dynamically-typed.sprites.player
  (:require [quip.sprite :as qpsprite]
            [dynamically-typed.utils :as u]
            [dynamically-typed.sound :as sound]
            [quip.utils :as qpu]
            [dynamically-typed.sprites.particle :as particle]))

(defn decay-animation-timer
  [{:keys [animation-timer] :as p}]
  (if (some? animation-timer)
    (if (zero? animation-timer)
      (-> p
          (assoc :animation-timer nil)
          (qpsprite/set-animation :idle))
      (update p :animation-timer dec))
    p))

(defn init-player
  ([]
   (init-player [100 70]))
  ([pos]
   (-> (qpsprite/animated-sprite :player
                                 pos
                                 32
                                 32
                                 "img/player/player.png"
                                 :update-fn (comp qpsprite/update-animated-sprite
                                                  u/apply-gravity
                                                  u/apply-friction
                                                  decay-animation-timer)
                                 :animations {:idle {:frames      4
                                                     :y-offset    0
                                                     :frame-delay 10}
                                              :jump {:frames      6
                                                     :y-offset    1
                                                     :frame-delay 5}
                                              :dash {:frames      6
                                                     :y-offset    2
                                                     :frame-delay 5}
                                              :turn {:frames      6
                                                     :y-offset    3
                                                     :frame-delay 2}}
                                 :current-animation :idle)
       (merge {:landed          false
               :direction       [1 1]
               :animation-timer nil}))))

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
      (do (sound/jump)
          (assoc-in state
                    [:scenes current-scene :sprites]
                    (concat non-players
                            [(-> player
                                 (update :vel (fn [[vx vy]] [vx (- vy 5)]))
                                 (update :pos (fn [[x y]] [x (- y 10)]))
                                 (assoc :landed false)
                                 (qpsprite/set-animation :jump)
                                 (assoc :animation-timer 30))]
                            (particle/->particle-group (:pos player)
                                                       (:vel player)
                                                       :color u/player-pink
                                                       :count 15
                                                       :life 150))))
      state)))

(defn dash
  [{:keys [current-scene] :as state}]
  (let [sprites     (get-in state [:scenes current-scene :sprites])
        non-players (remove #(#{:player} (:sprite-group %)) sprites)
        player      (first (filter #(#{:player} (:sprite-group %)) sprites))
        direction   (:direction player)]
    (do (sound/dash)
        (assoc-in state
                  [:scenes current-scene :sprites]
                  (concat non-players
                          [(-> player
                               (update :vel (fn [vel]
                                              (u/add vel
                                                     (u/multiply direction
                                                                 [10 0]))))
                               (qpsprite/set-animation :dash)
                               (assoc :animation-timer 30))]
                          (particle/->particle-group (:pos player)
                                                     (:vel player)
                                                     :color u/player-pink
                                                     :count 15
                                                     :life 150))))))

(defn turn
  [{:keys [current-scene] :as state}]
  (let [sprites     (get-in state [:scenes current-scene :sprites])
        non-players (remove #(#{:player} (:sprite-group %)) sprites)
        player      (first (filter #(#{:player} (:sprite-group %)) sprites))
        direction   (:direction player)]
    (do (sound/turn)
        (assoc-in state
                  [:scenes current-scene :sprites]
                  (conj non-players
                        (-> player
                            (update :direction u/flip-x)
                            (update :vel u/flip-x)
                            (qpsprite/set-animation :turn)
                            (assoc :animation-timer 12)))))))

(defn dive
  [{:keys [current-scene] :as state}]
  (let [sprites     (get-in state [:scenes current-scene :sprites])
        non-players (remove #(#{:player} (:sprite-group %)) sprites)
        player      (first (filter #(#{:player} (:sprite-group %)) sprites))]
    (if-not (:landed player)
      (do (sound/dive)
          (assoc-in state
                    [:scenes current-scene :sprites]
                    (concat non-players
                            [(-> player
                                 (update :vel (fn [[vx vy]] [0 (+ vy 5)]))
                                 (qpsprite/set-animation :jump)
                                 (assoc :animation-timer 30))]
                            (particle/->particle-group (:pos player)
                                                       (:vel player)
                                                       :color u/player-pink
                                                       :count 15
                                                       :life 150))))
      state)))
