(ns dynamically-typed.sound
  (:require [clojure.java.io :as io]))

;; @TODO: This should be taken care of by quip, but there are issues,
;; doing it manually for now.

(defonce ^:dynamic *main-music-thread* (atom nil))

;; @TODO: make sound a toggleable setting
(def ^:dynamic *sound-enabled* true)

(def tracks {:mellow "sound/music/Blippy Trance.mp3"
             :driving "sound/music/Getting it Done.mp3"
             :glitter "sound/music/Glitter Blast.mp3"})

(def sound-effects {:jump ["sound/jump2.mp3"]
                    :dash ["sound/dash.mp3"]
                    :turn ["sound/turn.mp3"]
                    :dive ["sound/dive.mp3"]
                    :pickup ["sound/pickup.mp3"]
                    :finish ["sound/finish.mp3"]
                    :new-command ["sound/new-command.mp3"]})

(defn ->player
  [resource-name]
  (-> resource-name
      io/resource
      io/input-stream
      java.io.BufferedInputStream.
      javazoom.jl.player.Player.))

(defn loop-track
  [track-key]
  (when *sound-enabled*
    (reset! *main-music-thread* (Thread. #(while true (doto (->player (track-key tracks))
                                                        (.play)
                                                        (.close)))))
    (.start @*main-music-thread*)))

(defn stop-music
  []
  (when *sound-enabled*
    (.stop @*main-music-thread*)))

(defn play-sound-effect
  [sound-effect-key]
  (when *sound-enabled*
    (.start (Thread. #(doto (->player (rand-nth (sound-effect-key sound-effects)))
                        (.play)
                        (.close))))))

(defn init
  []
  (when *sound-enabled*
    (loop-track :default)))

(defn jump
  []
  (play-sound-effect :jump))

(defn dash
  []
  (play-sound-effect :dash))

(defn turn
  []
  (play-sound-effect :turn))

(defn dive
  []
  (play-sound-effect :dive))

(defn pickup
  []
  (play-sound-effect :pickup))

(defn finish
  []
  (play-sound-effect :finish))

(defn new-command
  []
  (play-sound-effect :new-command))
