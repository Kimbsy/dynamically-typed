(ns dynamically-typed.sprites.particle
  (:require [dynamically-typed.utils :as u]
            [quip.sprite :as qpsprite]
            [quil.core :as q]
            [quip.utils :as qpu]))

(defn draw-particle
  [{[x y] :pos
    w     :w
    h     :h
    color :color
    :as   p}]
  (qpu/fill color)
  (q/rect x y w h))

(defn ->particle
  [pos vel color
   & {:keys [life] :or {life 50}}]
  {:sprite-group :particles
   :uuid         (java.util.UUID/randomUUID)
   :pos          pos
   :vel          vel
   :color        color
   :rotation     0
   :w            3
   :h            3
   :animated?    false
   :static?      false
   :update-fn    (comp qpsprite/update-image-sprite
                       u/apply-gravity
                       u/apply-friction
                       u/decay-life-timer)
   :draw-fn      draw-particle
   :bounds-fn    qpsprite/default-bounding-poly
   :life         life})

(defn randomize
  [v]
  (u/add [(- (rand 6) 3)
          (- (rand 6) 3)]
         v))

(defn randomize-color
  [color]
  (case (rand-int 3)
    0 color
    1 (-> color qpu/lighten qpu/lighten qpu/lighten qpu/lighten)
    2 (-> color qpu/darken qpu/darken)))

(defn random-color
  []
  (let [colors [u/light-blue u/light-red u/light-green]]
    (get colors
         (rand-int (count colors)))))

(defn ->particle-group
  [pos vel & {:keys [color count life]
              :or   {color (random-color)
                     count 30
                     life  50}}]
  (take count (repeatedly #(->particle (randomize pos)
                                       (randomize vel)
                                       (randomize-color color)
                                       :life life))))

(defn clear-particles
  [{:keys [current-scene] :as state}]
  (let [sprites       (get-in state [:scenes current-scene :sprites])
        non-particles (remove #(#{:particles} (:sprite-group %)) sprites)
        particles     (filter #(#{:particles} (:sprite-group %)) sprites)]
    (assoc-in state
              [:scenes current-scene :sprites]
              (concat non-particles
                      (filter #(pos? (:life %))
                              particles)))))
