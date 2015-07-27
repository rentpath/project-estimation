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
    [:title "Estimation"]]
   [:body
    [:h1 "Estimates"]
    [:form.current-player
     [:label "Name: "
      [:input {:name "player-name"}]]
     [:input {:type "submit"}]]
    [:h2 "Cards"]
    [:ol.cards
     (for [x ["?" 0 1 2 3 5 8 13 20]]
       [:li
        [:button x]])]
    [:h2 "Players"]
    [:div.players]
    [:script {:src "javascript/main.js"}]]))
