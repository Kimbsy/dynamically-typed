(ns dynamically-typed.sprites.pickup
  (:require [dynamically-typed.utils :as u]
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
                                :animations {:mutate {:frames      52
                                                      :y-offset    0
                                                      :frame-delay 6}}
                                :current-animation :mutate)
      (assoc :command command)))
