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
  [pos vel
   & {:keys [color count life]
      :or   {color (random-color)
             count 30
             life  50}}]
  (take count (repeatedly #(->particle (randomize pos)
                                       (randomize vel)
                                       (randomize-color color)
                                       :life life))))

(defn retarget
  [[tx ty]]
  (fn [{[px py]         :pos
        current-vel     :vel
        targeting-delay :targeting-delay
        :as             p}]
    (if (pos? targeting-delay)
      (update p :targeting-delay dec)
      (let [tv   [(- tx px) (- ty py)]
            utv  (u/unit-vector tv)
            tvel (u/multiply utv [2 3])]
        (assoc p :vel (u/add current-vel tvel))))))

(defn remove-arrived
  [[tx ty]]
  (fn [{[px py] :pos :as p}]
    (if (and (< (Math/abs (int (- tx px))) 100)
             (< (Math/abs (int (- ty py))) 100))
      (assoc p :life -1)
      p)))

(defn ->homing-particle-group
  [pos vel target-pos
   & {:keys [color]
      :or {color u/light-blue}}]
  (let [basic-group (->particle-group pos vel :color color)]
    (map (fn [p]
           (-> p
               (assoc :targeting-delay 30)
               (assoc :update-fn (comp qpsprite/update-image-sprite
                                       (retarget target-pos)
                                       (remove-arrived target-pos)))))
         basic-group)))

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
