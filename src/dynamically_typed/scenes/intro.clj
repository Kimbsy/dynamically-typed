(ns dynamically-typed.scenes.intro
  (:require [dynamically-typed.command :as command]
            [dynamically-typed.sprites.goal :as goal]
            [dynamically-typed.sprites.platform :as platform]
            [dynamically-typed.sprites.player :as player]
            [dynamically-typed.utils :as u]
            [quil.core :as q]
            [quip.collision :as qpcollision]
            [quip.scene :as qpscene]
            [quip.sprite :as qpsprite]
            [quip.utils :as qpu]))

(defn update-intro
  [state]
  (-> state
      player/reset-player-flags
      qpcollision/update-collisions
      qpscene/update-scene-sprites
      (command/decay-display-delays :sound? false)
      ((u/check-victory-fn :level-01))))

(defn draw-big-letter-command
  [[command-key {:keys [display-delay green-delay] :as command}] font]
  (when (neg? display-delay)
    (let [complete  (apply str (:complete (first (:progression command))))
          remaining (apply str (:remaining (first (:progression command))))]
      (q/text-font font)
      (qpu/fill qpu/green)
      (q/text complete (/ (q/width) 2) (/ (q/height) 2))
      (when (neg? green-delay)
        (qpu/fill qpu/white))
      (q/text remaining (/ (q/width) 2) (- (/ (q/height) 2) 90)))))

(defn draw-big-letter-commands
  [{:keys [current-scene giant-font] :as state}]
  (let [commands (filter #(#{:huge} (:size (second %)))
                         (get-in state [:scenes current-scene :commands]))]
    (->> commands
         (mapv #(draw-big-letter-command % giant-font)))))

(defn draw-intro
  [{:keys [default-font] :as state}]
  (qpu/background u/dark-grey)

  ;; This should really be a text sprite, but something goes wrong
  ;; with displaying regular commands afterwards in windows, probably
  ;; due to the text offsets that get applied.
  (q/text-font default-font)
  (qpu/fill qpu/white)
  (q/text "(press)" (- (* (q/width) 1/2) 120) (- (* (q/height) 1/2) 100))

  (qpscene/draw-scene-sprites state)
  (draw-big-letter-commands state))

(defn draw-completed-intro
  [state]
  (qpu/background u/dark-grey)
  (qpscene/draw-scene-sprites state)
  (command/draw-commands state))

(defn fading-square
  [pos w h]
  {:sprite-group :fading-squares
   :uuid         (java.util.UUID/randomUUID)
   :pos          pos
   :w            w
   :h            h
   :update-fn    (fn [{:keys [fade-delay] :as s}]
                   (if (neg? fade-delay)
                     (update s :alpha #(max 0 (dec %)))
                     (update s :fade-delay dec)))
   :draw-fn      (fn [{[x y] :pos w :w h :h alpha :alpha}]
                   (qpu/fill (conj u/dark-grey alpha))
                   (q/rect (- x (/ w 2))
                           (- y (/ h 2))
                           w h))
   :fade-delay 150
   :alpha 255})

(defn sprites
  []
  [(fading-square [(- (* (q/width) 1/2) 120)
                   (- (* (q/height) 1/2) 100)]
                  400
                  100)
   (goal/->goal [100 950])
   (player/init-player [100 1050])])

(defn pressed-p
  [{:keys [current-scene] :as state}]
  (-> state
      (assoc-in [:scenes current-scene :commands :jump]
                (command/->command ["jump"]
                                   player/jump
                                   :green-delay 50))
      (assoc-in [:scenes current-scene :sprites]
                [(-> (player/init-player [100 550])
                     (assoc :landed? true))
                 (goal/->goal [100 718])
                 (platform/floor)])
      (assoc-in [:scenes current-scene :draw-fn] draw-completed-intro)
      (command/particle-burst :jump)
      player/jump))

(defn pressed-m
  [{:keys [current-scene] :as state}]
  (-> state
      (assoc-in [:scenes current-scene :commands]
                {:p (-> (command/->command ["p"] pressed-p
                                           :display-delay 100
                                           :particle-burst? false
                                           :resetting? false)
                        (assoc :size :huge))})))

(defn pressed-u
  [{:keys [current-scene] :as state}]
  (-> state
      (assoc-in [:scenes current-scene :commands]
                {:m (-> (command/->command ["m"] pressed-m
                                           :display-delay 100
                                           :particle-burst? false
                                           :resetting? false)
                        (assoc :size :huge))})))

(defn pressed-j
  [{:keys [current-scene] :as state}]
  (-> state
      (assoc-in [:scenes current-scene :commands]
                {:u (-> (command/->command ["u"] pressed-u
                                           :display-delay 100
                                           :particle-burst? false
                                           :resetting? false)
                        (assoc :size :huge))})))

(defn commands
  []
  {:j (-> (command/->command ["j"] pressed-j
                             :display-delay 100
                             :particle-burst? false
                             :resetting? false)
          (assoc :size :huge))})

(defn key-pressed-fns
  []
  [command/handle-keypress])

(defn colliders
  []
  [(platform/platform-collider :player)
   (platform/platform-collider :particles)
   (goal/goal-collider)])


(defn init
  []
  {:update-fn       update-intro
   :draw-fn         draw-intro
   :sprites         (sprites)
   :commands        (commands)
   :key-pressed-fns (key-pressed-fns)
   :colliders       (colliders)})
