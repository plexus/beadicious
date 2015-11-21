(ns beadicious.core
  (:require
   [beadicious.midi :as midi]
   [clojure.core.async :refer [go-loop <! close!]])
  (:import
   [javax.sound.midi MidiDevice MidiSystem Receiver MidiMessage ShortMessage]
   [net.beadsproject.beads.core AudioContext UGen]
   [net.beadsproject.beads.data Buffer]
   [net.beadsproject.beads.ugens WavePlayer Gain BiquadFilter BiquadFilter$Type]))


(comment
  (def dev (midi/find-device "MidiIn"))
  (.open dev)

  (def transmitter (midi/transmitter dev))

  (def ch  (midi/listen transmitter (filter :noteOn)))

  (go-loop []
    (println (<! ch))
    (recur))


  (.close dev)
  (.close transmitter)
  (close! ch))


Buffer/SINE
Buffer/SQUARE

;; AudioContext ac = new AudioContext();
;; WavePlayer wp = new WavePlayer(ac, 440.0f, Buffer.SINE);
;; ac.out.addInput(wp);
;; ac.start();

(def ac
  (AudioContext.))

(def out (.out ac))

(defn ->WavePlayer [^AudioContext ac ^double freq ^Buffer buffer]
  (WavePlayer. ac freq buffer))

(defn ->UWavePlayer [^AudioContext ac ^UGen ugen ^Buffer buffer]
  (WavePlayer. ac ugen buffer))

(defn ->BiquadFilter [^AudioContext ac ^BiquadFilter$Type type ^double freq ^double q]
  (BiquadFilter. ac type freq q))

(defn connect-> [ugen & ugens]
  (when (seq ugens)
    (.addInput (first ugens) ugen)
    (apply connect-> ugens)))

(defn frequency! [ugen freq]
  (.setFrequency ugen (float freq)))


(comment
  (.addInput (.out ac)
             (->WavePlayer ac 440 Buffer/SINE))
  (.start ac)
  (.stop ac))


;; ;; In this line of code, a Gain object is instantiated with one input at a starting volume of 0.2, or 20%.
;; Gain g = new Gain(ac, 1, 0.2f);
;; g.addInput(wp);
;; ac.out.addInput(g);


;; kickFilter = new BiquadFilter(ac, BiquadFilter.BESSEL_LP, 500.0f, 1.0f);

(comment

  (do
    (def dev (midi/find-device "MidiIn"))
    (.open dev)

    (def transmitter (midi/transmitter dev))
    (def ch (midi/listen transmitter (filter :controlChange)))
    (def ac (AudioContext.)))

  (let [
        out (.out ac)
        player (->WavePlayer ac 440 Buffer/SQUARE)
        filter (->BiquadFilter ac BiquadFilter$Type/BESSEL_LP 450 1)]
    (connect-> player filter out)
    (go-loop []
      (let [msg (<! ch)]
        (println msg)
        (if (= 26 (:control msg))
          (frequency! filter (* 5 (:value msg)))
          (frequency! player (* 5 (:value msg)))))
      (recur))
    (.start ac))

  (do
    (.stop ac)
    (.close transmitter)
    (.close dev))
  )
