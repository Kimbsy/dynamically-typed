(ns dynamically-typed.sprites.goal
  (:require [quip.collision :as qpcollision]
            [quip.sprite :as qpsprite]))

(defn ->goal
  [pos]
  (qpsprite/animated-sprite :goal pos 96 64 "img/finish/finish.png"
                            :current-animation :incomplete
                            :animations {:incomplete {:frames      1
                                                      :y-offset    0
                                                      :frame-delay 100}
                                         :complete   {:frames      2
                                                      :y-offset    1
                                                      :frame-delay 20}}))

(defn goal-collider
  []
  (qpcollision/collider
   :player
   :goal
   qpcollision/identity-collide-fn
   (fn [{:keys [current-animation] :as g} _]
     (if (#{:incomplete} current-animation)
       (qpsprite/set-animation g :complete)
       g))))
