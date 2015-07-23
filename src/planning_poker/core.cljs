(ns planning-poker.core
  (:require-macros
    [cljs.core.async.macros :as asyncm :refer (go go-loop)])
  (:require
    [cljs.core.async :as async :refer (<! >! put! chan)]
    [taoensso.sente  :as sente :refer (cb-success?)]))

(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket! "/chsk"
                                  {:type :auto})]
  (def chsk chsk)
  (def ch-chsk ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def chsk-state state)) ; Watchable, read-only atom

(enable-console-print!)
(println "Hello world!")

;; Payload handler is for our custom event ids

(defmulti payload-handler (comp first second))

(defmethod payload-handler :planning-poker.routes/something
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
  (println "Handshake" msg))

(defmethod msg-handler :chsk/state
  [msg]
  (println "State" msg))

(defmethod msg-handler :chsk/recv
  [msg]
  (println "Push event from server:" msg)
  (payload-handler msg))

(sente/start-chsk-router! ch-chsk msg-handler*)
