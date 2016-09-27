(ns planning-poker.client.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [chan]]
            [taoensso.sente :as sente]
            [reagent.core :as r]
            [planning-poker.client.game-table :as game-table]
            [planning-poker.client.message-handler :as message-handler]
            [planning-poker.client.extensions]))

(defonce players (r/atom {}))
(def channel (chan))

(let [{:keys [ch-recv send-fn state]}
      (sente/make-channel-socket! "/chsk"
                                  {:type :auto})]
  (def ch-chsk ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn)) ; ChannelSocket's send API fn

(defn handle-message
  [{event :event, :as event-message}]
  (message-handler/process! event
                            players
                            {:event event
                             :events-to-send channel
                             :send-fn chsk-send!}))

(defn main
  []
  (enable-console-print!)
  (sente/start-chsk-router! ch-chsk handle-message)
  (r/render [game-table/component players channel]
            (first (.getElementsByClassName js/document "app"))))

(main)
