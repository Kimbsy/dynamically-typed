(ns dynamically-typed.scenes.credits
  (:require [dynamically-typed.command :as command]
            [dynamically-typed.sound :as sound]
            [dynamically-typed.utils :as u]
            [quil.core :as q]
            [quip.scene :as qpscene]
            [quip.sprite :as qpsprite]
            [quip.sprites.button :as qpbutton]
            [quip.utils :as qpu]))

(defn update-credits
  [state]
  (-> state
      qpscene/update-scene-sprites))

(defn draw-credits
  [state]
  (qpu/background u/dark-grey)
  (qpu/fill qpu/black)
  (q/rect (* (q/width) 1/4) (* (q/height) 3/20)
          (* (q/width) 1/2) (* (q/height) 14/20))
  (qpu/fill qpu/white)
  (q/rect (+ (* (q/width) 1/4) 5) (+ (* (q/height) 3/20) 5)
          (- (* (q/width) 1/2) 10) (- (* (q/height) 14/20) 10))
  (qpscene/draw-scene-sprites state)
  (command/draw-commands state))

(defn on-click-back
  [state e]
  (qpscene/transition state :menu
                      :transition-length 30
                      :init-fn (fn [state]
                                 (sound/stop-music)
                                 (sound/loop-track :mellow)
                                 (prn "MENU")
                                 (u/unclick-all-buttons state))))

(defn text-sprites
  []
  [(qpsprite/text-sprite "A game by Kimbsy"
                         [(* (q/width) 1/2)
                          (* (q/height) 6/20)]
                         :color qpu/black)
   (qpsprite/text-sprite "Music by Kevin MacLeod (incompetech.com)"
                         [(* (q/width) 1/2)
                          (* (q/height) 19/40)]
                         :color qpu/black)
   (qpsprite/text-sprite "Played by You!"
                         [(* (q/width) 1/2)
                          (* (q/height) 13/20)]
                         :color qpu/black)])

(defn button-sprites
  []
  [(qpbutton/button-sprite "Back"
                           [(* (q/width) 1/2) (* (q/height) 5/6)]
                           :on-click on-click-back)])

(defn sprites
  []
  (concat (text-sprites)
          (button-sprites)))

(defn celebrate
  [state]
  state)

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
   ["yay" "woohoo" "terrific" "celebrate" "yes" "nice" "super" "wow"
    "cool" "party" "amazing" "fantastic" "awesome" "fun" "enjoy" "great"]))

(defn key-pressed-fns
  []
  [command/handle-keypress
   command/clear])

(defn init
  []
  {:update-fn          update-credits
   :draw-fn            draw-credits
   :sprites            (sprites)
   :commands           (commands)
   :key-pressed-fns    (key-pressed-fns)
   :mouse-pressed-fns  [qpbutton/handle-buttons-pressed]
   :mouse-released-fns [qpbutton/handle-buttons-released]})
