(ns planning-poker.views
  (:use [hiccup core page]))

(defn index-page []
  (html5
   [:head
    [:title "Remote Planning Poker"]
    (include-css "/stylesheets/styles.css")]
   [:body
    [:div.app
     [:h1.home-heading "Remote Planning Poker"]
     [:a.new-game {:href (str "/" (java.util.UUID/randomUUID))} "Start a Game"]

     [:h2 "Join Existing Game"]
     [:p "Ask someone on your team for the URL of their planning poker session."]
     [:footer.copyright
      [:p "PLANNING POKER ® is a registered trademark of Mountain Goat Software, LLC"]
      [:p "Sequence of values is (C) Mountain Goat Software, LLC"]]]]))

(defn voting-page []
  (html5
   [:head
    [:title "Remote Planning Poker"]
    (include-css "/stylesheets/styles.css")]
   [:body
    [:div.app
     [:script {:src "javascript/main.js"}]]]))
