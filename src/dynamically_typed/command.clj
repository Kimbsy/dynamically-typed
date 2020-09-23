(ns dynamically-typed.command
  (:require [clojure.string :as s]
            [dynamically-typed.utils :as u]
            [quil.core :as q]
            [quip.utils :as qpu]))

(defn ->progress
  [command-alias]
  {:complete []
   :remaining (mapv s/lower-case command-alias)})

(defn ->command
  [aliases on-complete]
  {:aliases aliases
   :on-complete on-complete
   :progression (map ->progress aliases)})

(defn complete?
  [{:keys [progression]}]
  (some zero? (map (comp count :remaining) progression)))

(defn update-progress
  [{:keys [complete remaining] :as progress} k]
  (if (= (s/lower-case k) (first remaining))
    (-> progress
        (update :complete #(conj % (first remaining)))
        (update :remaining (comp vec rest)))
    progress))

(defn update-progression
  [{:keys [progression] :as command} k]
  (assoc command :progression
         (mapv #(update-progress % k) progression)))

(defn reset-command
  [{:keys [current-scene] :as state}
   command-key
   {:keys [aliases on-complete] :as command}]
  (assoc-in state [:scenes current-scene :commands command-key]
            (->command aliases on-complete)))

(defn apply-on-completes
  [{:keys [current-scene] :as state}]
  (reduce (fn [acc [command-key {:keys [on-complete] :as command}]]
            (if (complete? command)
              (-> acc
                  on-complete
                  (reset-command command-key command))
              acc))
          state
          (get-in state [:scenes current-scene :commands])))

(defn reduce-commands
  [commands k]
  (reduce (fn [acc [command-key command]]
            (assoc acc command-key (update-progression command k)))
          {}
          commands))

(defn handle-keypress
  [{:keys [current-scene] :as state} {raw :raw-key :as e}]
  (if (u/alpha? raw)
    (-> state
        (update-in [:scenes current-scene :commands]
                   #(reduce-commands % raw))
        apply-on-completes)
    state))

(defn clear
  [{:keys [current-scene] :as state} {k :key :as e}]
  (if (= :space k)
    (do (prn "------CLEAR------")
        (update-in state [:scenes current-scene :commands]
                   (fn [commands]
                     (reduce (fn [acc [command-key {:keys [aliases on-complete]}]]
                               (assoc acc command-key (->command aliases on-complete)))
                             {}
                             commands))))
    state))

(defn draw-character
  [i c [x-offset y-offset]]
  (q/text (str c) (+ x-offset (* i 12.5)) y-offset))

(defn draw-command
  [i [command-key command] font]
  (let [complete (apply str (:complete (first (:progression command))))
        remaining (apply str (:remaining (first (:progression command))))]
    (q/text-font font)
    (qpu/fill qpu/green)
    (mapv #(draw-character %1 %2 [20 (+ 40 (* i 35))])
          (range)
          complete)
    (qpu/fill qpu/white)
    (mapv #(draw-character %1 %2 [(+ 20 (* 12.5 (count complete))) (+ 40 (* i 35))])
          (range)
          remaining)))

(defn draw-commands
  [{:keys [current-scene default-font] :as state}]
  (let [commands (get-in state [:scenes current-scene :commands])]
    (->> commands
         (sort-by first)
         (mapv #(draw-command %1 %2 default-font) (range)))))
