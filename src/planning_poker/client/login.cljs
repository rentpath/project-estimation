(ns planning-poker.client.login
  (:require-macros
   [cljs.core.async.macros :refer [go]])
  (:require
   [cljs.core.async :refer [>!]]
   [reagent.core :as r]
   [planning-poker.client.form-parser :refer [value]]
   [planning-poker.client.utils :refer [path]]))

(defonce player-name (r/atom ""))
(defonce observer? (r/atom false))

(defn- login
  [channel]
  (fn [event]
    (.preventDefault event)
    (go (>! channel [:table/player-joined {:name @player-name
                                           :table-id (path)
                                           :observer @observer?}]))))

(defn- update-name
  [event]
  (reset! player-name (value event)))

(defn- update-observer-status
  [_event]
  (reset! observer? (not @observer?)))

(defn component
  [channel]
  (let [logged-in (r/atom false)]
    (fn []
      (when-not @logged-in
        [:form.login
         [:fieldset
          [:p "Remote Planning Poker"]
          [:input.login-name {:name "player-name"
                              :placeholder "Your Name"
                              :on-change update-name
                              :auto-focus true}]
          [:button
           {:on-click #(do (reset! logged-in true)
                           ((login channel) %))}
           "Start Playing"]
          [:div.login-observer-controls
           [:input.login-observer
            {:type :checkbox
             :id :login-observer
             :on-change update-observer-status}]
           [:label
            {:for :login-observer}
            "I'm just observing."]]]]))))
