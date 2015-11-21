(ns beadicious.core
  (:import
   [javax.sound.midi MidiDevice MidiSystem Receiver MidiMessage ShortMessage]
   [net.beadsproject.beads.core AudioContext UGen]
   [net.beadsproject.beads.data Buffer]
   [net.beadsproject.beads.ugens WavePlayer Gain]))


(defmethod clojure.core/print-method MidiDevice
  [device writer]
  (.write writer (str (.getName (class device)) "<" (.getDeviceInfo device) ">")))

;; AudioContext ac = new AudioContext();
;; WavePlayer wp = new WavePlayer(ac, 440.0f, Buffer.SINE);
;; ac.out.addInput(wp);
;; ac.start();

(defn query-midi-devices []
  (map #(MidiSystem/getMidiDevice %)
       (MidiSystem/getMidiDeviceInfo)))

(defn find-midi-device [name]
  (some #(and (.contains (str %) name) %) (query-midi-devices)))

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

(defn parse-midi-message [message]
  (if (instance? ShortMessage message)
    (let [command (get midi-commands (.getCommand message))
          channel (.getChannel message)
          data1   (.getData1 message)
          data2   (.getData2 message)
          result  {:command command :channel channel :data1 data1 :data2 data2}]
      (merge result
             (case command
               (:noteOn :noteOff) {:note data1 :velocity data2}
               (:controlChange)   {:control data1 :value data2}
               )))))


(defn midi-listen [device-name fn]
  (let [device (find-midi-device device-name)
        transmitter (.getTransmitter device)
        receiver (proxy [Receiver] []
                       (send [^MidiMessage message ^double timestamp]
                         (if-let [msg (parse-midi-message message)]
                           (fn msg timestamp))))]
    (.open device)
    (.setReceiver transmitter receiver)
    [device transmitter]))

(def device (midi-listen "MidiInDevice" #(println %1 %2)))

(map #(.close %) device)


;; (com.sun.media.sound.SoftSynthesizer<Gervill>
;; com.sun.media.sound.MidiInDevice<Mini [hw:1,0,0]>
;; com.sun.media.sound.MidiInDevice<Mini [hw:1,0,1]>
;; com.sun.media.sound.MidiOutDevice<Mini [hw:1,0,0]>
;; com.sun.media.sound.MidiOutDevice<Mini [hw:1,0,1]>
;; com.sun.media.sound.RealTimeSequencer<Real Time Sequencer>)






Buffer/SINE
Buffer/SQUARE

(def ac
  (AudioContext.))

(defn ->WavePlayer [^AudioContext ac ^double freq ^Buffer buffer]
  (WavePlayer. ac freq buffer))

(defn ->UgenWavePlayer [^AudioContext ac ^UGen ugen ^Buffer buffer]
  (WavePlayer. ac ugen buffer))

(.addInput (.out ac)
           (->WavePlayer ac 440 Buffer/SINE))

(.start ac)

(.stop ac)

;;  In this line of code, a Gain object is instantiated with one input at a starting volume of 0.2, or 20%.

;; Gain g = new Gain(ac, 1, 0.2f);
;; g.addInput(wp);
;; ac.out.addInput(g);




;; // set up the keyboard input
;;     MidiKeyboard keys = new MidiKeyboard();
;;     keys.addActionListener(new ActionListener(){
;;       @Override
;;       public void actionPerformed(ActionEvent e)
;;       {
;;         // if the event is not null
;;         if( e != null )
;;         {
;;           // if the event is a MIDI event
;;           if( e.getSource() instanceof ShortMessage )
;;           {
;;             // get the MIDI event
;;             ShortMessage sm = (ShortMessage)e.getSource();

;;             // if the event is a key down
;;             if( sm.getCommand() == MidiKeyboard.NOTE_ON && sm.getData2() > 1 )
;;             {
;;               keyDown(sm.getData1());
;;             }
;;             // if the event is a key up
;;             else if( sm.getCommand() == MidiKeyboard.NOTE_OFF )
;;             {
;;               keyUp(sm.getData1());
;;             }
;;           }
;;         }
;;       }
;;     });
