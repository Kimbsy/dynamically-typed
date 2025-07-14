(ns dynamically-typed.scenes.intro
  (:require [dynamically-typed.command :as command]
            [dynamically-typed.sprites.goal :as goal]
            [dynamically-typed.sprites.platform :as platform]
            [dynamically-typed.sprites.player :as player]
            [dynamically-typed.common :as common]
            [clunk.collision :as collision]
            [clunk.scene :as scene]
            [clunk.sprite :as sprite]
            [clunk.util :as u]
            [clunk.palette :as p]
            [clunk.core :as c]
            [clunk.shape :as shape]))

(defn update-intro
  [state]
  (-> state
      player/reset-player-flags
      collision/update-state
      sprite/update-state
      (command/decay-display-delays :sound? false)
      ((common/check-victory-fn :level-01))))

(defn draw-big-letter-command
  [{:keys [window] :as state} [command-key {:keys [display-delay green-delay] :as command}] font]
  (when (neg? display-delay)
    (let [complete  (apply str (:complete (first (:progression command))))
          remaining (apply str (:remaining (first (:progression command))))
          [w h] (u/window-size window)]
      (sprite/draw-text-sprite!
       state
       (sprite/text-sprite :big-command
                           [(/ w 2) (/ h 2)]
                           complete
                           :color p/green
                           :font-size 250))
      (let [color (if (neg? green-delay)
                    p/white
                    p/green)]
        (sprite/draw-text-sprite!
         state
         (sprite/text-sprite :big-command
                             [(/ w 2) (- (/ h 2) 90)]
                             remaining
                             :color color
                             :font-size 250))))))

(defn draw-big-letter-commands
  [{:keys [current-scene giant-font] :as state}]
  (let [commands (filter #(#{:huge} (:size (second %)))
                         (get-in state [:scenes current-scene :commands]))]
    (->> commands
         (mapv #(draw-big-letter-command state % giant-font)))))

(defn draw-intro
  [{:keys [window default-font] :as state}]
  (c/draw-background! common/dark-grey)
  (let [[w h] (u/window-size window)]
    (sprite/draw-text-sprite!
     state
     (sprite/text-sprite :prompt
                         [(- (* w 1/2) 120) (- (* h 1/2) 100)]
                         "(press)"
                         :color p/white)))
  (sprite/draw-scene-sprites! state)
  (draw-big-letter-commands state))

(defn draw-completed-intro
  [state]
  (c/draw-background! common/dark-grey)
  (sprite/draw-scene-sprites! state)
  (command/draw-commands state))

(defn fading-square
  [pos w h]
  {:sprite-group :fading-squares
   :uuid         (java.util.UUID/randomUUID)
   :pos          pos
   :size         [w h]
   :update-fn    (fn [{:keys [fade-delay] :as s}]
                   (if (neg? fade-delay)
                     (update s :alpha #(max 0 (dec %)))
                     (update s :fade-delay dec)))
   :draw-fn      (fn [{[x y] :pos [w h] :size alpha :alpha}]
                   (shape/fill-rect! [(- x (/ w 2)) (- y (/ h 2))]
                                     [w h]
                                     (assoc common/dark-grey 3 (/ alpha 255))))
   :bounds-fn sprite/default-bounding-poly
   :debug-color p/red
   :fade-delay   150
   :alpha        255})

(defn sprites
  [{:keys [window]}]
  (let [[w h] (u/window-size window)]
    [(fading-square [(- (* w 1/2) 120)
                     (- (* h 1/2) 100)]
                    400
                    100)
     (goal/->goal [100 950])
     (player/init-player [100 1050])]))

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
  [state]
  {:update-fn update-intro
   :draw-fn   draw-intro
   :sprites   (sprites state)
   :commands  (commands)
   :key-fns   (key-pressed-fns)
   :colliders (colliders)})
