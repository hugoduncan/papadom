(defproject papadom "0.1.0-SNAPSHOT"
  :description "A templating and event generation lib for clojurescript."
  :url "https://github.com/hugoduncan/papadom"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1844"]
                 [jayq "2.4.0"]
                 [org.clojure/core.async "0.1.0-SNAPSHOT"]
                 [com.cemerick/clojurescript.test "0.0.4"]] ; in dev
  :repositories {"sonatype-oss-public"
                 "https://oss.sonatype.org/content/groups/public"}
  :profiles {:dev {:source-paths ["src" "dev"]
                   :dependencies [[com.cemerick/piggieback "0.1.0"]
                                  [com.cemerick/clojurescript.test "0.0.4"]]}}
  :plugins [[lein-cljsbuild "0.3.2"]]
  ;;:hooks [leiningen.cljsbuild]
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  :cljsbuild {:builds
              [{:source-paths ["src" "test"]
                :compiler {:output-to "target/papadom.js"
                           :optimizations :whitespace
                           :pretty-print true
                           :libs
                           [{:file "papadom/js/jquery.js"
                             :provides ["papadom.js.jquery"]}
                            {:file "papadom/js/handlebars.js"
                             :provides ["papadom.js.handlebars"]}]}}]
              :test-commands {"unit-tests"
                              ["runners/phantomjs.js"
                               "target/papadom.js"]}})
