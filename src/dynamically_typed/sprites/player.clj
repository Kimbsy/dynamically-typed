(ns dynamically-typed.sprites.player
  (:require [clunk.collision :as collision]
            [clunk.sprite :as sprite]
            [dynamically-typed.common :as common]
            [dynamically-typed.sprites.particle :as particle]
            [clunk.audio :as audio]
            [clunk.palette :as p]))

(defn decay-animation-timer
  [{:keys [animation-timer] :as p}]
  (if (some? animation-timer)
    (if (zero? animation-timer)
      (-> p
          (assoc :animation-timer nil)
          (sprite/set-animation :idle))
      (update p :animation-timer dec))
    p))

(defn init-player
  ([]
   (init-player [100 70]))
  ([pos]
   (-> (sprite/animated-sprite :player
                               pos
                               [32 32]
                               :player
                               [192 128]
                               :update-fn (comp sprite/update-animated-sprite
                                                common/apply-gravity
                                                common/apply-friction
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
       (merge {:landed?          false
               :grabbing?        false
               :direction       [1 1]
               :animation-timer nil
               :debug-color p/red}))))

(defn reset-player-flags
  [{:keys [current-scene] :as state}]
  (let [sprites     (get-in state [:scenes current-scene :sprites])
        non-players (remove #(#{:player} (:sprite-group %)) sprites)
        player      (first (filter #(#{:player} (:sprite-group %)) sprites))]
    (assoc-in state
              [:scenes current-scene :sprites]
              (conj non-players
                    (-> player
                        (assoc :landed? false))))))

(defn jump
  [{:keys [current-scene] :as state}]
  (let [sprites     (get-in state [:scenes current-scene :sprites])
        non-players (remove #(#{:player} (:sprite-group %)) sprites)
        player      (first (filter #(#{:player} (:sprite-group %)) sprites))]
    (if (or (:landed? player) (:grabbing? player))
      (do (audio/play! :jump)
          (assoc-in state
                    [:scenes current-scene :sprites]
                    (concat non-players
                            [(-> player
                                 (update :vel (fn [[vx vy]] [vx (- vy 5)]))
                                 (update :pos (fn [[x y]] [x (- y 10)]))
                                 (assoc :landed? false)
                                 (assoc :grabbing? false)
                                 (sprite/set-animation :jump)
                                 (assoc :animation-timer 30))]
                            (particle/->particle-group (:pos player)
                                                       (:vel player)
                                                       :color common/player-pink
                                                       :count 15
                                                       :life 150))))
      state)))

(defn dash
  [{:keys [current-scene] :as state}]
  (let [sprites     (get-in state [:scenes current-scene :sprites])
        non-players (remove #(#{:player} (:sprite-group %)) sprites)
        player      (first (filter #(#{:player} (:sprite-group %)) sprites))
        direction   (:direction player)]
    (do (audio/play! :dash)
        (assoc-in state
                  [:scenes current-scene :sprites]
                  (concat non-players
                          [(-> player
                               (update :vel (fn [vel]
                                              (common/add vel
                                                          (common/multiply direction
                                                                           [10 0]))))
                               (assoc :grabbing? false)
                               (sprite/set-animation :dash)
                               (assoc :animation-timer 30))]
                          (particle/->particle-group (:pos player)
                                                     (:vel player)
                                                     :color common/player-pink
                                                     :count 15
                                                     :life 150))))))

(defn turn
  [{:keys [current-scene] :as state}]
  (let [sprites     (get-in state [:scenes current-scene :sprites])
        non-players (remove #(#{:player} (:sprite-group %)) sprites)
        player      (first (filter #(#{:player} (:sprite-group %)) sprites))
        direction   (:direction player)]
    (do (audio/play! :turn)
        (assoc-in state
                  [:scenes current-scene :sprites]
                  (conj non-players
                        (-> player
                            (update :direction common/flip-x)
                            (update :vel common/flip-x)
                            (sprite/set-animation :turn)
                            (assoc :animation-timer 12)))))))

(defn dive
  [{:keys [current-scene] :as state}]
  (let [sprites     (get-in state [:scenes current-scene :sprites])
        non-players (remove #(#{:player} (:sprite-group %)) sprites)
        player      (first (filter #(#{:player} (:sprite-group %)) sprites))]
    (if-not (:landed? player)
      (do (audio/play! :dive)
          (assoc-in state
                    [:scenes current-scene :sprites]
                    (concat non-players
                            [(-> player
                                 (update :vel (fn [[vx vy]] [0 (+ vy 5)]))
                                 (sprite/set-animation :jump)
                                 (assoc :animation-timer 30))]
                            (particle/->particle-group (:pos player)
                                                       (:vel player)
                                                       :color common/player-pink
                                                       :count 15
                                                       :life 150))))
      state)))

(defn grab
  [{:keys [current-scene] :as state}]
  (let [sprites     (get-in state [:scenes current-scene :sprites])
        holds       (filter #(#{:holds} (:sprite-group %)) sprites)
        non-players (remove #(#{:player} (:sprite-group %)) sprites)
        player      (first (filter #(#{:player} (:sprite-group %)) sprites))]
    (if (some (partial collision/w-h-rects-collide? player) holds)
      (assoc-in state
                [:scenes current-scene :sprites]
                (concat non-players
                        [(-> player
                             (assoc :grabbing? true)
                             (assoc :vel [0 0]))]
                        (particle/->particle-group (:pos player)
                                                   (:vel player)
                                                   :color common/player-pink
                                                   :count 15
                                                   :life 150)))
      state)))
