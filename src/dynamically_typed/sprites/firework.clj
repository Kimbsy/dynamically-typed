(ns dynamically-typed.sprites.firework
  (:require [clunk.sprite :as qpsprite]
            [dynamically-typed.common :as common]
            [dynamically-typed.sprites.particle :as particle]
            [clunk.palette :as p]))

(defn random-pos
  []
  [(* 1200 (rand)) 800])

(defn ->firework
  []
  (-> (qpsprite/animated-sprite :fireworks
                                (random-pos)
                                [16 32]
                                :firework
                                [64 32]
                                :animations {:spin {:frames      4
                                                    :y-offset    0
                                                    :frame-delay 2}}
                                :current-animation :spin
                                :vel [0  (- -8 (rand 5))]
                                :update-fn (comp qpsprite/update-animated-sprite
                                                 common/apply-gravity
                                                 common/decay-life-timer))
      (assoc :life 100)
      (assoc :debug-color p/red)))

(defn pop-firework
  [{:keys [pos vel life] :as firework}]
  (if (pos? life)
    [firework]
    (particle/->particle-group pos vel)))

(defn pop-fireworks
  [{:keys [current-scene] :as state}]
  (let [sprites       (get-in state [:scenes current-scene :sprites])
        non-fireworks (remove #(#{:fireworks} (:sprite-group %)) sprites)
        fireworks     (filter #(#{:fireworks} (:sprite-group %)) sprites)]
    (assoc-in state
              [:scenes current-scene :sprites]
              (concat non-fireworks
                      (mapcat pop-firework fireworks)))))
