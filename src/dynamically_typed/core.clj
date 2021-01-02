(ns dynamically-typed.core
  (:gen-class)
  (:require [dynamically-typed.scenes.credits :as credits]
            [dynamically-typed.scenes.intro :as intro]
            [dynamically-typed.scenes.level-01 :as level-01]
            [dynamically-typed.scenes.level-02 :as level-02]
            [dynamically-typed.scenes.level-02-b :as level-02-b]
            [dynamically-typed.scenes.level-03 :as level-03]
            [dynamically-typed.scenes.level-04 :as level-04]
            [dynamically-typed.scenes.level-05 :as level-05]
            [dynamically-typed.scenes.level-06 :as level-06]
            [dynamically-typed.scenes.level-07 :as level-07]
            [dynamically-typed.scenes.level-08 :as level-08]
            [dynamically-typed.scenes.level-09 :as level-09]
            [dynamically-typed.scenes.menu :as menu]
            [dynamically-typed.sound :as sound]
            [quip.core :as quip]
            [quip.utils :as qpu]
            [quil.core :as q]))

(defn setup
  []
  (sound/loop-track :mellow)
  {:default-font (q/create-font "font/UbuntuMono-Regular.ttf" qpu/default-text-size)
   :giant-font   (q/create-font "font/UbuntuMono-Regular.ttf" 250)})

(defn init-scenes
  []
  {:menu       (menu/init)
   :intro      (intro/init)
   :level-01   (level-01/init)
   :level-02   (level-02/init)
   :level-02-b (level-02-b/init)
   :level-03   (level-03/init)
   :level-04   (level-04/init)
   :level-05   (level-05/init)
   :level-06   (level-06/init)
   :level-07   (level-07/init)
   :level-08   (level-08/init)
   :level-09   (level-09/init)
   :credits    (credits/init)})

(defn cleanup
  [state]
  (sound/stop-music)
  (System/exit 0))

(def game
  (quip/game
   {:title          "Dynamically Typed"
    :size           [1200 800]
    :setup          setup
    :init-scenes-fn init-scenes
    :current-scene  :menu
    :on-close       cleanup}))

(defn -main
  "Run the game."
  [& _]
  (quip/run game))
