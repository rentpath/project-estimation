(ns planning-poker.client.core
  (:require-macros [cljs.core.async.macros :as a :refer (go go-loop)])
  (:require [cljs.core.async :as a :refer (<! >! put! chan)]
            [taoensso.sente :as sente :refer (cb-success?)]
            [reagent.core :as r :refer [render]]
            [planning-poker.client.form-parser :refer [value]]))

(extend-type js/HTMLCollection
  ISeqable
  (-seq [array] (array-seq array 0)))

(extend-type js/NodeList
  ISeqable
  (-seq [array] (array-seq array 0)))

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
  (reset! players data))

(defmethod payload-handler :planning-poker.message-handler/new-round-started
  [[_ [_ data]]]
  (reset! players data)
  (deactivate-all-cards))

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

(defn all-players-estimated?
  [players]
  (every? :estimate (vals players)))

(defn notify-estimate
  [event]
  (let [estimate (-> event .-target .-textContent)]
    (go (>! events-to-send [::player-estimated estimate]))))

(defn deactivate-all-cards
  []
  (let [cards (.getElementsByClassName js/document "card")]
    (doseq [card cards]
      (-> card .-classList (.remove "active")))))

(defn change-active-card
  [event]
  (deactivate-all-cards)
  (let [card (.-currentTarget event)]
    (-> card .-classList (.add "active"))
    (notify-estimate event)))

(defn start-new-round
  [event]
  (go (>! events-to-send [::new-round-requested])))

(defonce players (r/atom {}))

(defn estimated-players
  [players]
  (reduce-kv
    (fn [acc k v]
      (assoc acc k (assoc v :show-estimate? true)))
    {}
    players))

(defn player-component
  [player]
  [:li.player
   [:span.name (:name player)]
   [:span.estimate (cond
                     (:show-estimate? player) (:estimate player)
                     (:estimate player) "done"
                     :else "waiting")]])

;; Do we need to pass in players here?
(defn players-component
  [players]
  (fn []
    (when (all-players-estimated? @players) (swap! players estimated-players))
    [:ul
     (for [[player-id player] @players]
       ^{:key player-id} [player-component player])]))

(def login-name (r/atom ""))

(defn login
  [event]
  (.preventDefault event)
  (let [form (.closest (.-currentTarget event) "form")]
    (aset form "style" "display" "none")
    (go (>! events-to-send [::player-joined @login-name]))))

(defn login-component
  []
  (let [update-login-name (fn [evt] (reset! login-name (value evt)))]
    [:form.login
     [:fieldset
      [:p "Planning Poker"]
      [:input {:name "player-name"
               :placeholder "Your Name"
               :autofocus true
               :on-change update-login-name}]
      [:button {:on-click login} "Start Playing"]]]))

(defn cards-component
  []
  [:ol.cards
   (for [x ["?" 0 1 2 3 5 8 13 20]]
     ^{:key x} [:li
                [:button.card {:on-click change-active-card} x]])])

(defn root-component
  [players]
  [:div
   [login-component]
   [:div.game-room
    [:h1 "Planning Poker"]
    [cards-component]
    [:div.players
     [:h2 "Players"]
     [:div.names]
     [:button.reset {:on-click start-new-round} "Play a New Round"]]]])

(defn main
  []
  (render [root-component players] (first (.getElementsByClassName js/document "app")))
  (render [(players-component players)] (first (.getElementsByClassName js/document "names")))
)

(main)

(comment
  (all-players-estimated? {"abc-def" {:name "El Guapo" :estimate 3}})
  (all-players-estimated? {"abc-def" {:name "El Guapo"}})
  )
