(ns planning-poker.core
  (:require-macros
    [cljs.core.async.macros :as a :refer (go go-loop)])
  (:require
    [cljsjs.jquery]
    [cljs.core.async :as a :refer (<! >! put! chan)]
    [taoensso.sente :as sente :refer (cb-success?)]
    [goog.events :as gevents]
    [reagent.core :as reagent :refer [atom]])
  (:use [jayq.core :only [$ css html]])
  (:import
    [goog dom]
    [goog style]
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

;; Initial parameter is in this format:
;; [:chsk/recv [:planning-poker.routes/players-updated {"a13-18-434-a62-2f5df" {:name "Michael"}}]]
(defmethod payload-handler :planning-poker.message-handler/players-updated
  [[_ [_ data]]]
  (reset! players data)
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
  (payload-handler message))

(sente/start-chsk-router! ch-chsk message-handler*)

(defn estimate
  [event]
  (-> event .-target dom/getTextContent))

((defn set-up-event-handlers
  []
  (. ($ ".card") (on "click" (fn [event]
                               (. ($ ".card") (removeClass "active"))
                               (. ($ (.-currentTarget event)) (addClass "active"))
                               (go (>! events-to-send [::player-estimated (estimate event)])))))

  (. ($ ".login") (on "submit" (fn [event]
                                 (.preventDefault event)
                                 (let [form (.-currentTarget event)
                                       player-name (forms/getValueByName form "player-name")]
                                   (goog.style.showElement form false)
                                   (go (>! events-to-send [::player-joined player-name]))))))

  (. ($ ".reset") (on "click" (fn [event]
                                (. ($ ".card") (removeClass "active"))
                                (go (>! events-to-send [::new-round-requested])))))))

(def players (atom {}))

(defn players-component []
  [:ul
   (for [player @players]
     ^{:key (first player)} [:li.player {:class (when (:estimate (second player)) "estimated")}

                             [:span.name (:name (second player))]
                             [:span.estimate (:estimate (second player))]])])

(reagent/render-component [players-component] (dom/getElementByClass "names"))

(comment
  (println (dom/getChildren (dom/getElementByClass "players")))
  )
