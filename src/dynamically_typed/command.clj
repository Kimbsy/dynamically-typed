(ns dynamically-typed.command
  (:require [clojure.string :as s]
            [dynamically-typed.utils :as u]))

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
