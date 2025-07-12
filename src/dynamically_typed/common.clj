(ns dynamically-typed.common
  (:require [clunk.scene :as scene]))

(def dark-grey [0.11764706 0.11764706 0.11764706 1])
(def light-blue [0.39215687 0.39215687 1.0 1])
(def light-red [0.98039216 0.29411766 0.29411766 1])
(def light-yellow [1.0 1.0 0.29411766 1])
(def light-green [0.29411766 0.98039216 0.29411766 1])
(def player-pink [0.9137255 0.11764706 0.38431373 1])
(def platform-blue [0.09411765 1.0 1.0 1])
(def button-teal [0.0 0.5882353 0.654902 1])

(def key->alpha
  {65 \a
   66 \b
   67 \c
   69 \e
   68 \d
   70 \f
   71 \g
   72 \h
   73 \i
   74 \j
   75 \k
   76 \l
   77 \m
   78 \n
   79 \o
   80 \p
   81 \q
   82 \r
   83 \s
   84 \t
   85 \u
   86 \v
   87 \w
   88 \x
   89 \y
   90 \z})

(def alpha? #{\A \a \B \b \C \c \D \d \E \e \F \f \G \g \H \h \I \i \J \j \K \k
              \L \l \M \m \N \n \O \o \P \p \Q \q \R \r \S \s \T \t \U \u \V \v
              \W \w \X \x \Y \y \Z \z})

(defn zero-vector?
  "Predicate to check if a vector has length 0."
  [v]
  (every? zero? v))

(defn magnitude
  "Calculate the length of a vector."
  [v]
  (Math/sqrt (reduce + (map #(Math/pow % 2)
                            v))))

(defn unit-vector
  "Calculate the unit vector of a given 2D vector."
  [v]
  (when-not (zero-vector? v)
    (map #(/ % (magnitude v)) v)))

(defn add
  [v1 v2]
  (map + v1 v2))

(defn multiply
  [v1 v2]
  (map * v1 v2))

(defn flip-x
  [v]
  (multiply v [-1 1]))

(defn flip-y
  [v]
  (multiply v [1 -1]))

(defn zero-ish
  [i]
  (let [delta 0.01]
    (if (< (- delta) i delta)
      0
      i)))

(defn apply-gravity
  [{:keys [landed? grabbing?] :as s}]
  (if-not (or landed? grabbing?)
    (update s :vel (fn [[vx vy]]
                     [vx (+ vy 0.1)]))
    s))

(defn apply-friction
  [{:keys [landed?] :as s}]
  (if landed?
    (update s :vel (fn [[vx vy]]
                     [(zero-ish (* vx 0.95)) vy]))
    s))

(defn decay-life-timer
  [s]
  (update s :life dec))

(defn reset-handler
  "Create a key-pressed handler which will invoke a level-reset
  function."
  [reset-fn]
  (fn [state {:keys [key-code modifiers] :as e}]
    (if (and (= 82 key-code)
             (modifiers :control))
      (reset-fn state)
      state)))

(defn check-victory-fn
  ([target-scene]
   (check-victory-fn target-scene identity))
  ([target-scene init-fn]
   (fn [{:keys [current-scene end-level-timeout] :as state}]
     (let [sprites (get-in state [:scenes current-scene :sprites])
           goals   (filter #(#{:goal} (:sprite-group %)) sprites)
           goal    (first goals)]
       (if (#{:complete} (:current-animation goal))
         (if (nil? end-level-timeout)
           (assoc state :end-level-timeout 100)
           (if (<= end-level-timeout 0)
             (-> state
                 (dissoc :end-level-timeout)
                 (scene/transition target-scene
                                   :transition-length 50
                                   :init-fn init-fn))
             (update state :end-level-timeout dec)))
         state)))))

(defn unclick-all-buttons
  [{:keys [current-scene] :as state}]
  (let [sprites     (get-in state [:scenes current-scene :sprites])
        buttons     (filter #(#{:button} (:sprite-group %)) sprites)
        non-buttons (remove #(#{:button} (:sprite-group %)) sprites)]
    (-> state
        (assoc-in [:scenes current-scene :sprites]
                  (concat non-buttons
                          (map #(assoc % :held? false)
                               buttons))))))

(defn extract-sprite-group
  [{:keys [current-scene] :as state} group]
  (let [sprites           (get-in state [:scenes current-scene :sprites])
        group-sprites     (filter #(#{group} (:sprite-group %)) sprites)
        non-group-sprites (remove #(#{group} (:sprite-group %)) sprites)]
    [group-sprites
     non-group-sprites]))
