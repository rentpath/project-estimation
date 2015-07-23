(ns planning-poker.core
  (:require-macros
    [cljs.core.async.macros :as a :refer (go go-loop)])
  (:require
    [cljs.core.async :as a :refer (<! >! put! chan)]
    [taoensso.sente :as sente :refer (cb-success?)])
  (:import
   [goog dom]))

(def events-to-send (chan))

(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket! "/chsk"
                                  {:type :auto})]
  (def chsk chsk)
  (def ch-chsk ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def chsk-state state)) ; Watchable, read-only atom

(enable-console-print!)

;; Payload handler is for our custom event ids

(defmulti payload-handler (comp first second))

;; FIXME
;; Data is in this format:
;; [:chsk/recv [:planning-poker.routes/user-joined-session {:names #{35b37a13-4318-4434-a762-2f79b37ef5df}}]]
(defmethod payload-handler :planning-poker.routes/user-joined-session
  [data]
  (dom/setTextContent (dom/getElementByClass "players") (:names (second (second data))))
  (println "Custom event from server:" data))

(defmethod payload-handler :planning-poker.routes/user-estimated
  [data]
  (println "Custom event from server:" data))

;; Handler for events

;; Wrap for logging, catching, etc.:
(defn msg-handler* [{:as ev-msg :keys [event]}]
  (msg-handler event))

(defmulti msg-handler first)

(defmethod msg-handler :default ; Fallback
  [msg]
  (println "Unhandled event:" (first msg)))

(defmethod msg-handler :chsk/handshake
  [msg]
  (println "Handshake" msg)
  (go
    (loop []
      (let [evt (<! events-to-send)]
        (chsk-send! evt))
      (recur))))

(defmethod msg-handler :chsk/state
  [msg]
  (println "State" msg))

(defmethod msg-handler :chsk/recv
  [msg]
  (println "Push event from server:" msg)
  (payload-handler msg))

(sente/start-chsk-router! ch-chsk msg-handler*)

(go (>! events-to-send [::user-joined-session {:name "Michael"}]))
