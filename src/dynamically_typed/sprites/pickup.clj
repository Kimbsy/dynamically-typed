(ns dynamically-typed.sprites.pickup
  (:require [dynamically-typed.sound :as sound]
            [dynamically-typed.sprites.particle :as particle]
            [dynamically-typed.utils :as u]
            [quip.collision :as qpcollision]
            [quip.sprite :as qpsprite]))

(defn draw-pickup
  [p]
  )

(defn ->pickup
  [pos command]
  (-> (qpsprite/animated-sprite :pickups
                                pos
                                48 48
                                "img/pickup/pickup.png"
                                :animations {:mutate    {:frames      52
                                                         :y-offset    0
                                                         :frame-delay 6}
                                             :activated {:frames      10
                                                         :y-offset    1
                                                         :frame-delay 2}}
                                :current-animation :mutate)
      (assoc :command command)))

(defn handle-death
  [{:keys [life] :as p}]
  (if (pos? life)
    p
    (assoc p :sprite-group :finished-pickups)))

(defn update-activated-pickup
  [p]
  (-> p
      qpsprite/update-animated-sprite
      (update :life dec)
      (handle-death)))

(defn activate-pickup
  [p _]
  (prn "GOTCHA")
  (sound/pickup)
  (-> p
      (assoc :sprite-group :activated-pickups)
      (assoc :life 20)
      (assoc :update-fn update-activated-pickup)
      (qpsprite/set-animation :activated)))

(defn pickup-collider
  []
  (qpcollision/collider :player :pickups
                        qpcollision/identity-collide-fn
                        activate-pickup))

(defn handle-pickup
  [p]
  (assoc p :sprite-group :finished-pickups))

(defn generate-letters
  [{:keys [pos] :as p}]
  ;; for now just do a particle animation
  (particle/->homing-particle-group pos [0 0] [20 110]))

(defn remove-finished-pickups
  [{:keys [current-scene] :as state}]
  (let [[finished-pickups others] (u/extract-sprite-group state :finished-pickups)]
    (-> state
        (update-in [:scenes current-scene :commands]
                   (fn [commands]
                     (into {}
                           (concat commands
                                   (map :command finished-pickups)))))
        (assoc-in [:scenes current-scene :sprites]
                  (concat others
                          (mapcat generate-letters finished-pickups))))))
