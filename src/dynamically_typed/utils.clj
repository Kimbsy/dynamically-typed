(ns dynamically-typed.utils)

(def dark-grey [30 30 30])

(def alpha? #{\A \a \B \b \C \c \D \d \E \e \F \f \G \g \H \h \I \i \J \j \K \k
              \L \l \M \m \N \n \O \o \P \p \Q \q \R \r \S \s \T \t \U \u \V \v
              \W \w \X \x \Y \y \Z \z})

(defn zero-ish
  [i]
  (if (< i 0.01)
    0
    i))

(defn apply-gravity
  [{:keys [landed] :as s}]
  (if-not landed
    (update s :vel (fn [[vx vy]]
                     [vx (+ vy 0.1)]))
    s))

(defn apply-friction
  [{:keys [landed] :as s}]
  (if landed
    (update s :vel (fn [[vx vy]]
                     [(zero-ish (* vx 0.95)) vy]))
    s))
