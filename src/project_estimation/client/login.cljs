(ns project-estimation.client.login
  (:require-macros
   [cljs.core.async.macros :refer [go]])
  (:require
   [cljs.core.async :refer [>!]]
   [reagent.core :as r]
   [goog.dom.forms :as forms]
   [project-estimation.client.form-parser :refer [value]]
   [project-estimation.client.utils :refer [path]]))

(defn- field-value
  [form name]
  (first (.get form name)))

(defn- login
  [channel]
  (fn [event]
    (.preventDefault event)
    (let [form (forms/getFormDataMap (.-currentTarget event))]
      (go (>! channel [:board/participant-joined {:name (field-value form "participant-name")
                                                  :board-id (path)
                                                  :observer (field-value form "observer")}])))))

(defn component
  [channel]
  (let [logged-in (r/atom false)]
    (fn []
      (when-not @logged-in
        [:form.login {:on-submit #(do (reset! logged-in true)
                                      ((login channel) %))}
         [:fieldset
          [:p "Remote Project Estimation"]
          [:input.login-name {:name :participant-name
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
