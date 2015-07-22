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
      [:title "Estimation"]
      [:script {:src "javascript/main.js"}]]
    [:body
      [:h1 "Estimate"]
      [:ol
        (for [x [0 1 2 3 5 8 13 20]]
          [:li
            [:button x]])]]))
