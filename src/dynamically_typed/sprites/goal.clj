(ns dynamically-typed.sprites.goal
  (:require [clunk.collision :as collision]
            [clunk.sprite :as sprite]
            [clunk.audio :as audio]
            [clunk.palette :as p]))

(defn ->goal
  [pos]
  (merge
   (sprite/animated-sprite :goal
                           pos
                           [96 64]
                           :finish
                           [192 128]
                           :current-animation :incomplete
                           :animations {:incomplete {:frames      1
                                                     :y-offset    0
                                                     :frame-delay 100}
                                        :complete   {:frames      2
                                                     :y-offset    1
                                                     :frame-delay 20}})
   {:debug-color p/red}))

(defn goal-collider
  []
  (collision/collider
   :player
   :goal
   collision/identity-collide-fn
   (fn [{:keys [current-animation] :as g} _]
     (if (#{:incomplete} current-animation)
       (do
         (audio/play! :finish)
         (sprite/set-animation g :complete))
       g))))
