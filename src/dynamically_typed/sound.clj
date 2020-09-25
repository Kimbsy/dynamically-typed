(ns dynamically-typed.sound
  (:require [clojure.java.io :as io])
  (:import javax.sound.sampled.AudioSystem
           javax.sound.sampled.Clip
           javax.sound.sampled.DataLine$Info))

(defonce ^:dynamic *music* (atom nil))

(def tracks {:mellow "music/Blippy Trance.wav"
             :driving "music/Getting it Done.wav"
             :glitter "music/Glitter Blast.wav"})

(def sound-effects {:jump "jump2.wav"
                    :dash "dash.wav"
                    :turn "turn.wav"
                    :dive "dive.wav"
                    :pickup "pickup.wav"
                    :finish "finish.wav"
                    :new-command "new-command.wav"})

(defn play
  [sound]
  (let [input-stream (io/input-stream (io/resource (str "sound/" sound)))
        audio-stream (AudioSystem/getAudioInputStream input-stream)
        audio-format (.getFormat audio-stream)
        audio-info (DataLine$Info. Clip audio-format)
        audio-clip (cast Clip (AudioSystem/getLine audio-info))]
    (.open audio-clip audio-stream)
    (.start audio-clip)
    audio-clip))

(defn stop
  [clip]
  (.stop clip))

(defn stop-music
  []
  (stop @*music*))

(defn loop-track
  [track-key]
  (when @*music*
    (stop-music))
  (reset! *music* (play (track-key tracks))))

(defn play-sound-effect
  [sound-effect-key]
  (play (sound-effect-key sound-effects)))

(defn init
  []
  (loop-track :default))

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
