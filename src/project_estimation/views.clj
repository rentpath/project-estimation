(ns project-estimation.views
  (:use [hiccup core page]))

(defn index-page []
  (html5
   [:head
    [:title "Remote Project Estimation"]
    (include-css "/stylesheets/styles.css")]
   [:body
    [:div.app
     [:h1.home-heading "Remote Project Estimation"]
     [:a.new-session {:href (str "/" (java.util.UUID/randomUUID))} "Start a Session"]

     [:h2 "Join Existing Session"]
     [:p "Ask someone on your team for the URL of their project estimation session."]
     [:footer.copyright
      [:p "PLANNING POKER ® is a registered trademark of Mountain Goat Software, LLC"]
      [:p "Sequence of values is (C) Mountain Goat Software, LLC"]]]]))

(defn voting-page []
  (html5
   [:head
    [:title "Remote Project Estimation"]
    (include-css "/stylesheets/styles.css")]
   [:body
    [:div.app
     [:script {:src "javascript/main.js"}]]]))
