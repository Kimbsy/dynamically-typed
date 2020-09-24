(ns dynamically-typed.scenes.intro
  (:require [dynamically-typed.command :as command]
            [dynamically-typed.sprites.goal :as goal]
            [dynamically-typed.sprites.player :as player]
            [dynamically-typed.utils :as u]
            [quil.core :as q]
            [quip.collision :as qpcollision]
            [quip.scene :as qpscene]
            [quip.sprite :as qpsprite]
            [quip.utils :as qpu]
            [dynamically-typed.sprites.platform :as platform]))

(defn update-intro
  [state]
  (-> state
      player/reset-player-flags
      qpcollision/update-collisions
      qpscene/update-scene-sprites
      (command/decay-display-delays :sound? false)
      ((u/check-victory-fn :level-01))))

(defn draw-big-letter-command
  [i [command-key {:keys [display-delay green-delay] :as command}] font]
  (when (neg? display-delay)
    (let [complete  (apply str (:complete (first (:progression command))))
          remaining (apply str (:remaining (first (:progression command))))]
      (q/text-font font)
      (qpu/fill qpu/green)
      (q/text complete (/ (q/width) 2) (/ (q/height) 2))
      (when (neg? green-delay)
        (qpu/fill qpu/white))
      (q/text remaining (/ (q/width) 2) (/ (q/height) 2)))))

(defn draw-big-letter-commands
  [{:keys [current-scene giant-font] :as state}]
  (let [commands (get-in state [:scenes current-scene :commands])]
    (->> commands
         (mapv #(draw-big-letter-command %1 %2 giant-font) (range)))))

(defn draw-intro
  [state]
  (qpu/background u/dark-grey)
  (qpscene/draw-scene-sprites state)
  (draw-big-letter-commands state))

(defn sprites
  []
  [(qpsprite/text-sprite "(press)"
                         [(- (* (q/width) 1/2) 100) (- (* (q/height) 1/2) 100)]
                         :color qpu/white)
   (goal/->goal [100 950])
   (player/init-player [100 1050])])

(defn pressed-p
  [{:keys [current-scene] :as state}]
  (-> state
      (assoc-in [:scenes current-scene :sprites]
                [(-> (player/init-player [100 150])
                     (assoc :landed true))
                 (goal/->goal [100 718])
                 (platform/floor)])
      player/jump))

(defn pressed-m
  [{:keys [current-scene] :as state}]
  (-> state
      (assoc-in [:scenes current-scene :commands]
                {:p (command/->command ["p"] pressed-p
                                       :display-delay 100
                                       :particle-burst? false
                                       :resetting? false)})))

(defn pressed-u
  [{:keys [current-scene] :as state}]
  (-> state
      (assoc-in [:scenes current-scene :commands]
                {:m (command/->command ["m"] pressed-m
                                       :display-delay 100
                                       :particle-burst? false
                                       :resetting? false)})))

(defn pressed-j
  [{:keys [current-scene] :as state}]
  (-> state
      (assoc-in [:scenes current-scene :commands]
                {:u (command/->command ["u"] pressed-u
                                       :display-delay 100
                                       :particle-burst? false
                                       :resetting? false)})))

(defn commands
  []
  {:j (command/->command ["j"] pressed-j
                         :display-delay 100
                         :particle-burst? false
                         :resetting? false)})

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
