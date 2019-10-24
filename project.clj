(defproject project-estimation "0.1.0-SNAPSHOT"
  :description "Project estimation for remote agile teams"
  :url "https://remoteplanning.herokuapp.com"
  :min-lein-version "2.0.0"
  :dependencies [[com.taoensso/sente "1.6.0"]
                 [compojure "1.4.0"]
                 [hiccup "1.0.5"]
                 [http-kit "2.0.0"]
                 [org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.145"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.clojure/tools.logging "0.3.1"]
                 [environ "1.0.2"]
                 [reagent "0.5.0"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [org.clojars.mikejs/ring-gzip-middleware "0.1.0-SNAPSHOT"]]
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring-mock "0.1.5"]
                                  [doo "0.1.7"]]}
             :uberjar {:prep-tasks ["compile" ["cljsbuild" "once" "production"]]
                       :aot [project-estimation.routes]
                       :uberjar-name "project-estimation.jar"}}
  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-doo "0.1.7"]]
  :source-paths ["src"]
  :clean-targets ^{:protect false} ["resources/public/javascript"]
  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src"]
                        :compiler {:main "project-estimation.client.core"
                                   :asset-path "javascript"
                                   :output-dir "resources/public/javascript"
                                   :output-to "resources/public/javascript/main.js"
                                   :optimizations :none
                                   :verbose true}}
                       {:id "test"
                        :source-paths ["src" "test"]
                        :compiler {:main 'project-estimation.client.runner
                                   :output-to "out/test.js"
                                   :optimizations :none}}
                       {:id "production"
                        :source-paths ["src"]
                        :compiler {:main "project-estimation.client.core"
                                   :asset-path "javascript"
                                   :output-to "resources/public/javascript/main.js"
                                   :optimizations :advanced
                                   :verbose true}}]}
  :main project-estimation.routes)
