(ns planning-poker.routes
  (:use planning-poker.views
        [hiccup.middleware :only (wrap-base-url)])
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defroutes app-routes
  (GET "/" [] (index-page))
  (GET "/:game-number" [] (voting-page))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
