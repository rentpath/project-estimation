(ns planning-poker.client.login
  (:require-macros
   [cljs.core.async.macros :refer [go]])
  (:require
   [cljs.core.async :refer [>!]]
   [reagent.core :as r]
   [planning-poker.client.form-parser :refer [value]]
   [planning-poker.client.utils :refer [path]]))

(defonce player-name (r/atom ""))

(defn- login
  [channel]
  (fn [event]
    (.preventDefault event)
    (go (>! channel [:table/player-joined {:name @player-name
                                           :table-id (path)}]))))

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
          [:p "Remote Planning Poker"]
          [:input {:name "player-name"
                   :placeholder "Your Name"
                   :on-change update-name
                   :auto-focus true}]
          [:button
           {:on-click #(do (reset! logged-in true)
                           ((login channel) %))}
           "Start Playing"]]]))))
