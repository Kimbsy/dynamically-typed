(ns dynamically-typed.core
  (:gen-class)
  (:require [dynamically-typed.scenes.credits :as credits]
            [dynamically-typed.scenes.level-1 :as level-1]
            [dynamically-typed.scenes.menu :as menu]
            [quip.core :as quip]))

(defn setup
  []
  {})

(defn init-scenes
  []
  {:menu    (menu/init)
   :level-1 (level-1/init)
   :credits (credits/init)})

(def game
  (quip/game
   {:title          "Dynamically Typed"
    :size           [1200 800]
    :setup          setup
    :init-scenes-fn init-scenes
    :current-scene  :level-1}))

(defn -main
  "Run the game."
  [& _]
  (quip/run game))
