(ns dynamically-typed.sprites.firework
  (:require [dynamically-typed.sprites.particle :as particle]
            [dynamically-typed.utils :as u]
            [quip.sprite :as qpsprite]
            [quip.utils :as qpu]))

(defn random-pos
  []
  [(* 1200 (rand)) 800])

(defn ->firework
  []
  (-> (qpsprite/animated-sprite :fireworks
                                (random-pos)
                                16 32
                                "img/firework/firework.png"
                                :animations {:spin {:frames      4
                                                    :y-offset    0
                                                    :frame-delay 2}}
                                :current-animation :spin
                                :vel [0  (- -8 (rand 5))]
                                :update-fn (comp qpsprite/update-animated-sprite
                                                 u/apply-gravity
                                                 u/decay-life-timer))
      (assoc :life 100)))

(defn pop-firework
  [{:keys [pos vel life] :as firework}]
  (if (pos? life)
    [firework]
    (do (prn "BANG")
        (particle/->particle-group pos vel))))

(defn pop-fireworks
  [{:keys [current-scene] :as state}]
  (let [sprites       (get-in state [:scenes current-scene :sprites])
        non-fireworks (remove #(#{:fireworks} (:sprite-group %)) sprites)
        fireworks     (filter #(#{:fireworks} (:sprite-group %)) sprites)]
    (assoc-in state
              [:scenes current-scene :sprites]
              (concat non-fireworks
                      (mapcat pop-firework fireworks)))))
