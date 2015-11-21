(ns beadicious.midi
  (:require
   [clojure.core.async :refer [go chan >!]])
  (:import
   [javax.sound.midi MidiDevice MidiSystem Receiver MidiMessage ShortMessage]))

(def midi-commands
  {ShortMessage/ACTIVE_SENSING         :activeSensing
   ShortMessage/CHANNEL_PRESSURE       :channelPressure
   ShortMessage/CONTINUE               :continue
   ShortMessage/CONTROL_CHANGE         :controlChange
   ShortMessage/END_OF_EXCLUSIVE       :endOfExclusive
   ShortMessage/MIDI_TIME_CODE         :midiTimeCode
   ShortMessage/NOTE_OFF               :noteOff
   ShortMessage/NOTE_ON                :noteOn
   ShortMessage/PITCH_BEND             :pitchBend
   ShortMessage/POLY_PRESSURE          :polyPressure
   ShortMessage/PROGRAM_CHANGE         :programChange
   ShortMessage/SONG_POSITION_POINTER  :songPositionPointer
   ShortMessage/SONG_SELECT            :songSelect
   ShortMessage/START                  :start
   ShortMessage/STOP                   :stop
   ShortMessage/SYSTEM_RESET           :systemReset
   ShortMessage/TIMING_CLOCK           :timingClock
   ShortMessage/TUNE_REQUEST           :tuneRequest})

(defmethod clojure.core/print-method MidiDevice
  [device writer]
  (.write writer (str (.getName (class device)) "<" (.getDeviceInfo device) ">")))

(defn query-devices []
  (map #(MidiSystem/getMidiDevice %)
       (MidiSystem/getMidiDeviceInfo)))

(defn find-device
  "Finds the first device that matches the name. You are responsible for calling
  .open and .close on it."
  [name]
  (some #(and (.contains (str %) name) %) (query-devices)))


(defn parse-message
  "Turns a raw MidiMessage into a map, e.g.
  {:command :noteOn :channel 0 :note 60 :velocity 80}
  Ignores anything that isn't a plain ShortMessage."
  [^MidiMessage message]
  (if (instance? ShortMessage message)
    (let [command (get midi-commands (.getCommand message))
          channel (.getChannel message)
          data1   (.getData1 message)
          data2   (.getData2 message)
          result  {command  true
                   :command command
                   :channel channel
                   :data1   data1
                   :data2   data2}]
      (merge result
             (case command
               (:noteOn :noteOff) {:note data1 :velocity data2}
               (:controlChange)   {:control data1 :value data2})))))


(defn transmitter
  "You need to create a transmitter for a device to listen to its messages.
  .close the transmitter when you're done with it"
  [device]
  (.getTransmitter device))

(defn listen [transmitter transducer]
  (let [ch  (chan 1 transducer)
        rec (proxy [Receiver] []
              (send [^MidiMessage message ^double timestamp]
                (let [msg (parse-message message)]
                  (when msg
                    (go (>! ch (assoc msg :timestamp timestamp)))))))]
    (.setReceiver transmitter rec)
    ch))
