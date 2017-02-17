(ns planning-poker.client.login
  (:require-macros
   [cljs.core.async.macros :refer [go]])
  (:require
   [cljs.core.async :refer [>!]]
   [reagent.core :as r]
   [goog.dom.forms :as forms]
   [planning-poker.client.form-parser :refer [value]]
   [planning-poker.client.utils :refer [path]]))

(defn- field-value
  [form name]
  (first (.get form name)))

(defn- login
  [channel]
  (fn [event]
    (.preventDefault event)
    (let [form (forms/getFormDataMap (.-currentTarget event))]
      (go (>! channel [:table/player-joined {:name (field-value form "player-name")
                                             :table-id (path)
                                             :observer (field-value form "observer")}])))))

(defn component
  [channel]
  (let [logged-in (r/atom false)]
    (fn []
      (when-not @logged-in
        [:form.login {:on-submit #(do (reset! logged-in true)
                                      ((login channel) %))}
         [:fieldset
          [:p "Remote Planning Poker"]
          [:input.login-name {:name :player-name
                              :placeholder "Your Name"
                              :auto-focus true}]
          [:button "Start Playing"]
          [:div.login-observer-controls
           [:input.login-observer
            {:type :checkbox
             :id :observer
             :name :observer}]
           [:label
            {:for :observer}
            "I'm just observing."]]]]))))
