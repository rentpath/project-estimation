(ns planning-poker.core
  (:require-macros
    [cljs.core.async.macros :as a :refer (go go-loop)])
  (:require
    [cljs.core.async :as a :refer (<! >! put! chan)]
    [taoensso.sente :as sente :refer (cb-success?)])
  (:import
    [goog dom]
    [goog window]
    [goog.dom forms]))

(def events-to-send (chan))

(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket! "/chsk"
                                  {:type :auto})]
  (def chsk chsk)
  (def ch-chsk ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def chsk-state state)) ; Watchable, read-only atom

(enable-console-print!)

(defmulti payload-handler (comp first second))

;; FIXME
;; Data is in this format:
;; [:chsk/recv [:planning-poker.routes/players-updated {"35b37a13-4318-4434-a762-2f79b37ef5df" "Michael"}}]]
(defmethod payload-handler :planning-poker.message-handler/players-updated
  [data]
  (println (vals (second (second data))))
  (dom/setTextContent (dom/getElementByClass "players") (str (vals (second (second data)))))
  (println "Custom event from server:" data))

(defmethod payload-handler :planning-poker.message-handler/user-estimated
  [data]
  (println "Custom event from server:" data))

;; Handler for events

;; Wrap for logging, catching, etc.:
(defn message-handler* [{:as ev-message :keys [event]}]
  (message-handler event))

(defmulti message-handler first)

(defmethod message-handler :default
  [message]
  (println "Unhandled event:" (first message)))

(defmethod message-handler :chsk/handshake
  [message]
  (println "Handshake" message)
  (go
    (loop []
      (let [event (<! events-to-send)]
        (chsk-send! event))
      (recur))))

(defmethod message-handler :chsk/state
  [message]
  (println "State" message))

(defmethod message-handler :chsk/recv
  [message]
  (println "Push event from server:" message)
  (payload-handler message))

(sente/start-chsk-router! ch-chsk message-handler*)

(defn name
  []
  (forms/getValue (dom/getElementByClass "name")))

(go (>! events-to-send [::user-joined-session (name)]))
