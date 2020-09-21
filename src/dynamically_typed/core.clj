(ns dynamically-typed.core
  (:gen-class)
  (:require [dynamically-typed.scenes.credits :as credits]
            [dynamically-typed.scenes.level-01 :as level-01]
            [dynamically-typed.scenes.level-02 :as level-02]
            [dynamically-typed.scenes.level-03 :as level-03]
            [dynamically-typed.scenes.level-04 :as level-04]
            [dynamically-typed.scenes.level-05 :as level-05]
            [dynamically-typed.scenes.level-06 :as level-06]
            [dynamically-typed.scenes.menu :as menu]
            [dynamically-typed.sound :as sound]
            [quip.core :as quip]))

(defn setup
  []
  (sound/loop-track :driving)
  {})

(defn init-scenes
  []
  {:menu    (menu/init)
   :level-01 (level-01/init)
   :level-02 (level-02/init)
   :level-03 (level-03/init)
   :level-04 (level-04/init)
   :level-05 (level-05/init)
   :level-06 (level-06/init)
   :credits (credits/init)})

(defn cleanup
  [state]
  (sound/stop-music))

(def game
  (quip/game
   {:title          "Dynamically Typed"
    :size           [1200 800]
    :setup          setup
    :init-scenes-fn init-scenes
    :current-scene  :level-04
    :on-close       cleanup}))

(defn -main
  "Run the game."
  [& _]
  (quip/run game))
