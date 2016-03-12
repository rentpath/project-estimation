(ns planning-poker.views
  (:use [hiccup core page]))

(defn index-page []
  (html5
   [:head
    [:title "Remote Planning Poker"]]
   [:body
    [:h1 "Remote Planning Poker"]
    [:button "Start a Game"]
    [:button "Join a Game"]]))

(defn voting-page []
  (html5
   [:head
    [:title "Remote Planning Poker"]
    (include-css "/stylesheets/styles.css")]
   [:body
    [:div.app
     [:script {:src "javascript/main.js"}]]]))
