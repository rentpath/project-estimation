(defproject planning-poker "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.3.1"]
                 [hiccup "1.0.5"]
                 [ring "1.4.0"]
                 [com.taoensso/sente "1.5.0"]
                 [ring/ring-defaults "0.1.5"]
                 [http-kit "2.0.0"]]

  :profiles

  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}}

  :main planning-poker.routes)
