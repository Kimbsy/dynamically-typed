(ns dynamically-typed.core
  (:gen-class)
  (:require [dynamically-typed.scenes.credits :as credits]
            [dynamically-typed.scenes.level-1 :as level-1]
            [dynamically-typed.scenes.menu :as menu]
            [dynamically-typed.sound :as sound]
            [quip.core :as quip]))

(defn setup
  []
#_  (sound/loop-track :default)
  {})

(defn init-scenes
  []
  {:menu    (menu/init)
   :level-1 (level-1/init)
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
    :current-scene  :level-1
    :on-close       cleanup}))

(defn -main
  "Run the game."
  [& _]
  (quip/run game))
