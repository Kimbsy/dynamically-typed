(ns dynamically-typed.core
  (:gen-class)
  (:require [quip.core :as quip]))

(def game
  (quip/game
   {:title          "Dynamically Typed"
    :size           [1800 1200]
    :setup          (constantly {})
    :init-scenes-fn (constantly {})
    :current-scene  :menu
    :features       [:resizable]}))

(defn -main
  "Run the game."
  [& _]
  (quip/run game))
