(ns papadom.dev.repl-server
  "Simple server for testing"
  (:require
   [com.ebaxt.ring-rewrite :refer [wrap-rewrite]]
   [net.cgrand.moustache :refer [app]]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.file :as file]
   [ring.middleware.file-info :as file-info]
   [ring.middleware.resource :as resource]
   [ring.util.response :refer [not-found response]]))


(def static-files
     ["/index.html" "/css/" "/js/" "/images/" "/static/" "/scripts/"])

(def the-app
  (app
   (wrap-rewrite [:rewrite "/" "/index.html"])
   (file-info/wrap-file-info {"xhtml" "text/html" "js" "text/javascript"})
   (resource/wrap-resource "public")
   (file/wrap-file "target")
   (not-found "not-found")))


;; Keep track of the server, so we can stop it if required
(defonce server (atom nil))


(defn start
  "Start the app, keeping track of the server"
  [& {:keys [port join?] :or {port 8080 join? false}}]
  (reset!
   server {:stop (run-jetty #'the-app {:port port :join? join?})}))

(defn stop
  "Stop the app"
  []
  (swap! server (fn [server] ((:stop server)) nil)))
