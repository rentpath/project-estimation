(ns planning-poker.client.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [>! chan]]
            [taoensso.sente :as sente]
            [reagent.core :as r]
            [planning-poker.client.cards :as cards]
            [planning-poker.client.login :as login]
            [planning-poker.client.player :as player]
            [planning-poker.client.message-handler :as message-handler]
            planning-poker.client.extensions))

(defonce players (r/atom {}))
(def events-to-send (chan))

(let [{:keys [ch-recv send-fn state]}
      (sente/make-channel-socket! "/chsk"
                                  {:type :auto})]
  (def ch-chsk ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn)) ; ChannelSocket's send API fn

(enable-console-print!)

(defn handle-message [{:as ev-message :keys [event]}]
  (message-handler/process! event
                            players
                            {:event event
                             :events-to-send events-to-send
                             :send-fn chsk-send!}))

(sente/start-chsk-router! ch-chsk handle-message)

(defn all-players-estimated?
  [players]
  (every? :estimate (vals players)))

(defn start-new-round
  [event]
  (go (>! events-to-send [::new-round-requested])))

(defn estimated-players
  [players]
  (reduce-kv
    (fn [acc k v]
      (assoc acc k (assoc v :show-estimate? true)))
    {}
    players))

(defn players-component
  [players]
  (when (all-players-estimated? @players)
    (swap! players estimated-players))
  [:div.players
   [:h2 "Players"]
   [:div.names
    [:ul
     (for [[player-id player] @players]
       ^{:key player-id} [player/component player])]]])

(defn root-component
  [players]
  [:div
   [login/component events-to-send]
   [:div.game-room
    [:h1 "Planning Poker"]
    [cards/component events-to-send]
    [players-component players]
    [:button.reset {:on-click start-new-round} "Play a New Round"]]])

(defn main
  []
  (r/render [root-component players] (first (.getElementsByClassName js/document "app"))))

(main)

(comment
  (all-players-estimated? {"abc-def" {:name "El Guapo" :estimate 3}})
  (all-players-estimated? {"abc-def" {:name "El Guapo"}})
  )
