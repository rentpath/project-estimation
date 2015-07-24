(defproject planning-poker "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"

  :dependencies [[com.taoensso/sente "1.5.0"]
                 [compojure "1.4.0"]
                 [hiccup "1.0.5"]
                 [http-kit "2.0.0"]
                 [org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "0.0-3308"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]]

  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring-mock "0.1.5"]]}}

  :plugins [[lein-cljsbuild "1.0.6"]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/javascript"]

  :cljsbuild {
    :builds [{:id "planning-poker"
              :source-paths ["src"]
              :compiler {:main planning-poker.core
                         :asset-path "javascript"
                         :output-to "resources/public/javascript/main.js"
                         :output-dir "resources/public/javascript"
                         :optimizations :none
                         :verbose true}}]}

  :main planning-poker.routes)
