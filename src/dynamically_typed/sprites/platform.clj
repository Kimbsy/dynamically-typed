(ns dynamically-typed.sprites.platform
  (:require [dynamically-typed.utils :as u]
            [quil.core :as q]
            [quip.collision :as qpcollision]
            [quip.sprite :as qpsprite]
            [quip.utils :as qpu]))

(defn draw-platform
  [{[x y] :pos w :w h :h edge? :edge?}]
  (qpu/fill qpu/grey)
  (when edge?
    (q/stroke-weight 2)
    (qpu/stroke u/platform-blue))
  (q/rect (- x (/ w 2))
          (- y (/ h 2))
          w
          h
          10)
  (q/stroke-weight 1)
  (q/no-stroke))

(defn ->platform
  [pos w h
   & {:keys [edge?]
      :or   {edge? true}}]
  {:sprite-group :platforms
   :uuid         (java.util.UUID/randomUUID)
   :pos          pos
   :rotation     0
   :w            w
   :h            h
   :edge?        edge?
   :animated?    false
   :static?      true
   :update-fn    identity
   :draw-fn      draw-platform
   :bounds-fn    qpsprite/default-bounding-poly})

(defn floor [] (->platform [600 775] 1300 50))
(defn world-top [] (->platform [600 0] 1200 2 :edge? false))
(defn world-bottom [] (->platform [600 800] 1200 2 :edge? false))
(defn world-left [] (->platform [0 400] 2 800 :edge? false))
(defn world-right [] (->platform [1200 400] 2 800 :edge? false))

(defn world-bounds
  []
  [(world-top)
   (world-bottom)
   (world-left)
   (world-right)])

(defn top-hit?
  [vel-offset-y s-y p-y1 p-x1 p-x2 s-x1 s-x2]
  (and (< s-y (+ p-y1 vel-offset-y))
       (<= p-x1 s-x2)
       (<= s-x1 p-x2)))

(defn bottom-hit?
  [vel-offset-y s-y p-y2 p-x1 p-x2 s-x1 s-x2]
  (and (< (+ p-y2 vel-offset-y) s-y)
       (<= p-x1 s-x2)
       (<= s-x1 p-x2)))

(defn left-hit?
  [vel-offset-x s-x p-x1 p-y1 p-y2 s-y1 s-y2]
  (and (< s-x (+ p-x1 vel-offset-x))
       (<= p-y1 s-y2)
       (<= s-y1 p-y2)))

(defn right-hit?
  [vel-offset-x s-x p-x2 p-y1 p-y2 s-y1 s-y2]
  (and (< (+ p-x2 vel-offset-x) s-x)
       (<= p-y1 s-y2)
       (<= s-y1 p-y2)))

(defn sprite-hit-platform
  [{[vx vy]   :vel
    [s-x s-y] :pos
    s-w       :w
    s-h       :h
    :as       sprite}
   {[p-x p-y] :pos
    p-w       :w
    p-h       :h
    :as       platform}]
  (let [s-x1         (- s-x (/ s-w 2))
        s-x2         (+ s-x (/ s-w 2))
        s-y1         (- s-y (/ s-h 2))
        s-y2         (+ s-y (/ s-h 2))
        p-x1         (- p-x (/ p-w 2))
        p-x2         (+ p-x (/ p-w 2))
        p-y1         (- p-y (/ p-h 2))
        p-y2         (+ p-y (/ p-h 2))
        vel-offset-x (if (< (/ s-w 2) (Math/abs vx))
                       (- vx (/ s-w 2))
                       0)
        vel-offset-y (if (< (/ s-h 2) (Math/abs vy))
                       (- vy (/ s-h 2))
                       0)]
    (cond
      (top-hit? vel-offset-y s-y p-y1 p-x1 p-x2 s-x1 s-x2)
      (-> sprite
          (update :vel (fn [[vx vy]] [vx 0]))
          (update :pos (fn [[x y]] [x (- p-y1 (/ s-h 2))]))
          (assoc :landed? true))

      (bottom-hit? vel-offset-y s-y p-y2 p-x1 p-x2 s-x1 s-x2)
      (-> sprite
          (update :vel (fn [[vx vy]] [vx 0]))
          (update :pos (fn [[x y]] [x (inc (+ p-y2 (/ s-h 2)))]))
          (assoc :landed? false))

      (left-hit? vel-offset-x s-x p-x1 p-y1 p-y2 s-y1 s-y2)
      (-> sprite
          (update :vel (fn [[vx vy]] [0 vy]))
          (update :pos (fn [[x y]] [(dec (- p-x1 (/ s-w 2))) y]))
          (assoc :landed? false))

      (right-hit? vel-offset-x s-x p-x2 p-y1 p-y2 s-y1 s-y2)
      (-> sprite
          (update :vel (fn [[vx vy]] [0 vy]))
          (update :pos (fn [[x y]] [(inc (+ p-x2 (/ s-w 2))) y]))
          (assoc :landed? false))

      :else
      sprite)))

(defn platform-collider
  [sprite-group]
  (qpcollision/collider
    sprite-group
    :platforms
    sprite-hit-platform
    qpcollision/identity-collide-fn
    :collision-detection-fn
    (fn [{:keys [phasing?] :as s} platform]
      (if-not phasing?
        (qpcollision/w-h-rects-collide? s platform)))))
