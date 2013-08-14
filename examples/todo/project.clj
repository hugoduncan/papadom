(defproject todos "0.1.0-SNAPSHOT"
  :description "Example TODO app using papadom"
  :url "https://github.com/hugoduncan/papadom"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1844"]
                 [papadom "0.1.0-SNAPSHOT"]]
  :profiles {:dev {:dependencies [[ring/ring-core "1.2.0"]
                                  [ring/ring-devel "1.2.0"]
                                  [ring/ring-jetty-adapter "1.2.0"]
                                  [ring-rewrite "0.1.0"]
                                  [net.cgrand/moustache "1.2.0-alpha2"]]}}
  :cljsbuild {:builds
              [{:source-paths ["src" "test"]
                :compiler {:output-to "target/todos.js"
                           :optimizations :whitespace
                           :pretty-print true
                           ;; If you want to use jquery and handlebars
                           ;; via a CDN, remove the :foreign-libs
                           ;; definitions.
                           :libs [""]
                           }}]
              :test-commands {"unit-tests"
                              ["runners/phantomjs.js"
                               "target/todos.js"]}}
  :plugins [[com.cemerick/austin "0.1.0"]
            [lein-cljsbuild "0.3.2"]
            [lein-ring "0.8.6"]]
  :ring {:handler papadom.dev.repl-server/the-app}
  :hooks [leiningen.cljsbuild])

                           ;; :foreign-libs
                           ;; [{:file "papadom/js/jquery.js"
                           ;;   :provides ["papadom.js.jquery"]}
                           ;;  {:file "papadom/js/handlebars.js"
                           ;;   :provides ["papadom.js.handlebars"]}]
