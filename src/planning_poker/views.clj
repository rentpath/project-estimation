(ns planning-poker.views
  (:use [hiccup core page]))

(defn index-page []
  (html5
   [:head
    [:title "Hello World"]
    #_(include-css "/css/style.css")]
   [:body
    [:h1 "Hello World"]]))

(defn voting-page []
  (html5
   [:head
    [:title "Remote Planning Poker"]
    (include-css "/stylesheets/styles.css")]
   [:body
    [:div.app
     [:script {:src "javascript/main.js"}]]]))
