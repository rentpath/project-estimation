(ns planning-poker.client.core
  (:require-macros [cljs.core.async.macros :refer (go go-loop)])
  (:require [cljs.core.async :refer (>! chan)]
            [taoensso.sente :as sente]
            [reagent.core :as r :refer [render]]
            [planning-poker.client.card-state :refer [change-active-card]]
            [planning-poker.client.message-handler :as message-handler]
            [planning-poker.client.form-parser :refer [value]]))

(extend-type js/HTMLCollection
  ISeqable
  (-seq [array] (array-seq array 0)))

(extend-type js/NodeList
  ISeqable
  (-seq [array] (array-seq array 0)))

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

(defn notify-estimate
  [event]
  (let [estimate (-> event .-target .-textContent)]
    (go (>! events-to-send [::player-estimated estimate]))))

(defn start-new-round
  [event]
  (go (>! events-to-send [::new-round-requested])))

(defonce login-name (r/atom ""))
(defonce players (r/atom {}))

(defn estimated-players
  [players]
  (reduce-kv
    (fn [acc k v]
      (assoc acc k (assoc v :show-estimate? true)))
    {}
    players))

(defn login
  [event]
  (.preventDefault event)
  (let [form (.closest (.-currentTarget event) "form")]
    (aset form "style" "display" "none")
    (go (>! events-to-send [::player-joined @login-name]))))

(defn select-card
  [event]
  (change-active-card event)
  (notify-estimate event))

(defn login-component
  []
  (let [update-login-name (fn [evt] (reset! login-name (value evt)))]
    [:form.login
     [:fieldset
      [:p "Planning Poker"]
      [:input {:name "player-name"
               :placeholder "Your Name"
               :on-change update-login-name}]
      [:button {:on-click login} "Start Playing"]]]))

(defn cards-component
  []
  [:ol.cards
   (for [x ["?" 0 1 2 3 5 8 13 20]]
     ^{:key x} [:li
                [:button.card {:on-click select-card} x]])])

(defn player-component
  [player]
  [:li.player
   [:span.name (:name player)]
   [:span.estimate (cond
                     (:show-estimate? player) (:estimate player)
                     (:estimate player) "Done"
                     :else "Waiting")]])

(defn players-component
  [players]
  (when (all-players-estimated? @players)
    (swap! players estimated-players))
  [:div.players
   [:h2 "Players"]
   [:div.names
    [:ul
     (for [[player-id player] @players]
       ^{:key player-id} [player-component player])]]])

(defn root-component
  [players]
  [:div
   [login-component]
   [:div.game-room
    [:h1 "Planning Poker"]
    [cards-component]
    [players-component players]
    [:button.reset {:on-click start-new-round} "Play a New Round"]]])

(defn main
  []
  (render [root-component players] (first (.getElementsByClassName js/document "app"))))

(main)

(comment
  (all-players-estimated? {"abc-def" {:name "El Guapo" :estimate 3}})
  (all-players-estimated? {"abc-def" {:name "El Guapo"}})
  )
