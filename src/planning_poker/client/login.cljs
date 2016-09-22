(ns planning-poker.client.login
  (:require-macros
   [cljs.core.async.macros :refer [go]])
  (:require
   [cljs.core.async :refer [>!]]
   [reagent.core :as r]
   [planning-poker.client.form-parser :refer [value]]))

(defonce player-name (r/atom ""))

(defn- login
  [channel]
  (fn [event]
    (.preventDefault event)
    (go (>! channel [:table/player-joined @player-name]))))

(defn- update-name
  [event]
  (reset! player-name (value event)))

(defn component
  [channel]
  (let [logged-in (r/atom false)]
    (fn []
      (when-not @logged-in
        [:form.login
         [:fieldset
          [:p "Planning Poker"]
          [:input {:name "player-name"
                   :placeholder "Your Name"
                   :on-change update-name}]
          [:button {:on-click #(do (reset! logged-in true) ((login channel) %))} "Start Playing"]]]))))