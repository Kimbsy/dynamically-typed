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
            [clunk.core :as c]
            [clunk.audio :as audio]
            [clunk.util :as u]))

(defn setup
  [state]
  (assoc state :music-source
         (audio/play! :mellow :loop? true)))

(defn init-scenes
  [state]
  {:menu (menu/init state)
   :intro (intro/init state)
   :level-01 (level-01/init state)
   :level-02 (level-02/init state)
   :level-02-b (level-02-b/init state)
   :level-03 (level-03/init state)
   :level-04 (level-04/init state)
   :level-05 (level-05/init state)
   :level-06 (level-06/init state)
   :level-07 (level-07/init state)
   :level-08 (level-08/init state)
   :level-09 (level-09/init state)
   :credits (credits/init state)})

(def game
  (c/game
   {:title "Dynamically Typed"
    :size [1200 800]
    :on-start-fn setup
    :init-scenes-fn init-scenes
    :current-scene :menu
    :assets {:image {:finish "resources/img/finish/finish.png"
                     :player "resources/img/player/player.png"
                     :pickup "resources/img/pickup/pickup.png"
                     :firework "resources/img/firework/firework.png"}
             :audio {:mellow "resources/sound/music/Blippy Trance.ogg"
                     :driving "resources/sound/music/8bit Romance Loopable.ogg"
                     :glitter "resources/sound/music/Glitter Blast.ogg"
                     :jump "resources/sound/jump2.ogg"
                     :dash "resources/sound/dash.ogg"
                     :turn "resources/sound/turn.ogg"
                     :dive "resources/sound/dive.ogg"
                     :pickup "resources/sound/pickup.ogg"
                     :finish "resources/sound/finish.ogg"
                     :new-command "resources/sound/new-command.ogg"}}}))

(defn -main
  "Run the game."
  [& _]
  (c/start! game))
