(ns dynamically-typed.sprites.particle
  (:require [clunk.palette :as p]
            [clunk.shape :as shape]
            [clunk.sprite :as sprite]
            [clunk.util :as u]
            [dynamically-typed.common :as common]))

(defn draw-particle
  [{pos :pos
    [w h] :size
    color :color
    :as p}]
  (shape/fill-rect! pos [w h] color))

(defn ->particle
  [pos vel color
   & {:keys [life] :or {life 50}}]
  {:sprite-group :particles
   :uuid         (java.util.UUID/randomUUID)
   :pos          pos
   :vel          vel
   :color        color
   :rotation     0
   :size [3 3]
   :offsets [:center]
   :animated?    false
   :static?      false
   :update-fn    (comp sprite/update-pos
                       common/apply-gravity
                       common/apply-friction
                       common/decay-life-timer)
   :draw-fn      draw-particle
   :bounds-fn    sprite/default-bounding-poly
   :life         life
   :debug-color p/red})

(defn randomize
  [v]
  (common/add [(- (rand 6) 3)
               (- (rand 6) 3)]
              v))

(defn randomize-color
  [color]
  (case (rand-int 3)
    0 color
    1 (-> color p/lighten p/lighten p/lighten p/lighten)
    2 (-> color p/darken p/darken)))

(defn random-color
  []
  (let [colors [common/light-blue common/light-red common/light-green]]
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
            tvel (common/multiply utv [2 3])]
        (assoc p :vel (common/add current-vel tvel))))))

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
      :or {color common/light-yellow}}]
  (let [basic-group (->particle-group pos vel :color color :life 100)]
    (map (fn [p]
           (-> p
               (assoc :phasing? true)
               (assoc :targeting-delay 30)
               (assoc :update-fn (comp sprite/update-pos
                                       (retarget target-pos)
                                       (remove-arrived target-pos)
                                       common/decay-life-timer))))
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
