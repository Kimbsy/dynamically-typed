(ns dynamically-typed.scenes.credits
  (:require [clunk.audio :as audio]
            [clunk.core :as c]
            [clunk.palette :as p]
            [clunk.scene :as scene]
            [clunk.shape :as shape]
            [clunk.sprite :as sprite]
            [clunk.util :as u]
            [dynamically-typed.command :as command]
            [dynamically-typed.common :as common]
            [dynamically-typed.scenes.intro :as intro]
            [dynamically-typed.scenes.level-01 :as level-01]
            [dynamically-typed.scenes.level-02 :as level-02]
            [dynamically-typed.scenes.level-02-b :as level-02-b]
            [dynamically-typed.scenes.level-03 :as level-03]
            [dynamically-typed.scenes.level-04 :as level-04]
            [dynamically-typed.scenes.level-05 :as level-05]
            [dynamically-typed.scenes.level-06 :as level-06]
            [dynamically-typed.scenes.level-07 :as level-07]
            [dynamically-typed.sprites.button :as button]
            [dynamically-typed.sprites.firework :as firework]
            [dynamically-typed.sprites.particle :as particle]
            [clunk.input :as i]))

;; @TODO: sometime playing two musics?

(defn update-credits
  [state]
  (-> state
      sprite/update-state
      firework/pop-fireworks
      particle/clear-particles))

(defn draw-header
  []
  (let [points [[-30  485]
                [535  485]
                [635  385]
                [1200 385]
                [1200 445]
                [665  445]
                [565  545]
                [-30  545]]
        poly-1 [[-30  485]
                [535  485]
                [565  545]
                [-30  545]]
        poly-2 [[535  485]
                [635  385]
                [665  445]
                [565  545]]
        poly-3 [[635  385]
                [1200 385]
                [1200 445]
                [665  445]]]
    (shape/fill-poly! [0 100] poly-1 common/player-pink)
    (shape/fill-poly! [0 100] poly-2 common/player-pink)
    (shape/fill-poly! [0 100] poly-3 common/player-pink)
    (shape/fill-poly! [30 20] poly-1 common/platform-blue)
    (shape/fill-poly! [30 20] poly-2 common/platform-blue)
    (shape/fill-poly! [30 20] poly-3 common/platform-blue)))

(defn draw-credits
  [{:keys [window] :as state}]
  (c/draw-background! common/dark-grey)
  (draw-header)

  (let [[w h] (u/window-size window)]
    (shape/fill-rect!
     [(* w 1/4) (* h 3/20)]
     [(* w 1/2) (* h 14/20)]
     p/black)

    (shape/fill-rect!
     [(+ (* w 1/4) 5) (+ (* h 3/20) 5)]
     [(- (* w 1/2) 10) (- (* h 14/20) 10)]
     p/white))
  
  (sprite/draw-scene-sprites! state)
  (command/draw-commands state))

(defn on-click-back
  [state e]
  (scene/transition state :menu
                    :transition-length 30
                    :init-fn (fn [{:keys [music-source] :as state}]
                               (audio/stop! music-source)
                               (-> state
                                   (assoc :music-source (audio/play! :mellow :loop? true))
                                   (assoc-in [:scenes :intro] (intro/init state))
                                   (assoc-in [:scenes :level-01] (level-01/init state))
                                   (assoc-in [:scenes :level-02] (level-02/init state))
                                   (assoc-in [:scenes :level-02-b] (level-02-b/init state))
                                   (assoc-in [:scenes :level-03] (level-03/init state))
                                   (assoc-in [:scenes :level-04] (level-04/init state))
                                   (assoc-in [:scenes :level-05] (level-05/init state))
                                   (assoc-in [:scenes :level-06] (level-06/init state))
                                   (assoc-in [:scenes :level-07] (level-07/init state))
                                   common/unclick-all-buttons))))

(defn text-sprites
  [{:keys [window] :as state}]
  (let [[w h] (u/window-size window)]
    [(sprite/text-sprite :credits
                         [(* w 1/2)
                          (* h 11/40)]
                         "A game by Kimbsy"
                         :color p/black)
     (sprite/text-sprite :credits
                         [(* w 1/2)
                          (* h 17/40)]
                         "Music by PJ Kimber"
                         :color p/black)
     (sprite/text-sprite :credits
                         [(* w 1/2)
                          (* h 19/40)]
                         "and"
                         :color p/black)
     (sprite/text-sprite :credits
                         [(* w 1/2)
                          (* h 21/40)]
                         "Kevin MacLeod (incompetech.com)"
                         :color p/black)
     (sprite/text-sprite :credits
                         [(* w 1/2)
                          (* h 27/40)]
                         "Played by You!"
                         :color p/black)]))

(defn button-sprites
  [{:keys [window] :as state}]
  (let [[w h] (u/window-size window)]
    [(i/add-on-click
      (button/button-sprite [(* w 1/2) (* h 5/6)] "Back")
      on-click-back)]))

(defn sprites
  [state]
  (concat (text-sprites state)
          (button-sprites state)))

(defn celebrate
  [{:keys [current-scene] :as state}]
  (-> state
      (update-in [:scenes current-scene :sprites]
                 (fn [sprites]
                   (conj sprites (firework/->firework))))))

(defn celebrate-commands
  [commands]
  (into {}
        (map
         (fn [c]
           [(keyword c) (command/->command [c] celebrate)])
         commands)))

(defn commands
  []
  (celebrate-commands
   ["fun" "amazing" "nice" "wow" "great" "celebrate"
    "fantastic" "excellent" "cool" "hooray"]))

(defn key-pressed-fns
  []
  [command/handle-keypress])

(defn init
  [state]
  {:update-fn update-credits
   :draw-fn   draw-credits
   :sprites   (sprites state)
   :commands  []
   :key-fns   (key-pressed-fns)})
