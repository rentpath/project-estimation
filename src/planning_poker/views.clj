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
    [:form.login
     [:fieldset
      [:p "Planning Poker"]
      [:input {:name "player-name" :placeholder "Your Name" :autofocus true}]
      [:button "Start Playing"]]]
    [:div.game-room
     [:h1 "Planning Poker"]
     [:ol.cards
      (for [x ["?" 0 1 2 3 5 8 13 20]]
        [:li
         [:button.card x]])]
     [:div.players
      [:h2 "Players"]
      [:div.names]
      [:button.reset "Play a New Round"]]]
    [:script {:src "javascript/main.js"}]]))
