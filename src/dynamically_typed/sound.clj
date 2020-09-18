(ns dynamically-typed.sound
  (:require [quip.sound :as qpsound]))

(defn jump
  []
  (prn "SOUNDS")
  (qpsound/play-sound "sound/jump.mp3"))

(defn dash
  []
  (prn "SOUNDS")
  (qpsound/play-sound "sound/dash.mp3"))
