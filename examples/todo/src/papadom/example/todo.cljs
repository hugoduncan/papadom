(ns papadom.example.todo
  "Simple todo example."
  (:require
   [cljs.core.async :refer [chan >! <!]]
   [papadom.template :refer [compile-templates input-seq->map render
                             template-events value->select-options]])
  (:require-macros
   [cljs.core.async.macros :refer [go]]))

(defn show-state
  "Takes the application state, and renders it on the page using the templates."
  [state]
  (render "state"
          (assoc state
            :total (count (:todos state))
            :remaining (count (filter :done (:todos state))))))

(defn app
  "Reads events and modifies the state."
  [state event-chan]
  (go
   (loop [[event event-data] (<! event-chan)]
     (.log js/console (str "event: " event " event-data: " event-data))
     (case event
       :add-todo
       (swap! state update-in [:todos]
              conj {:text (:text (input-seq->map event-data))
                    :done false})
       :done
       (let [v (get-in state [:todos (:index event-data) :done])
             nv (boolean (:checked event-data))]
         (when (not= v nv)
           (swap! state assoc-in [:todos (:index event-data) :done] nv)))
       :archive
       (swap! state update-in [:todos] #(vec (remove :done %))))
     (recur (<! event-chan)))))

(defn start
  "Read the templates, set up the events channel, and start the app with
  some initial state."
  ([initial-state]
     (let [event-chan (chan)
           state (atom nil)]
       (compile-templates)
       (template-events event-chan)
       (add-watch state :state (fn [key ref old new] (show-state new)))
       (reset! state initial-state)
       (app state event-chan)))
  ([] (start
       {:todos [{:text "Learn papadom" :done false}
                {:text "Build a papadom app" :done false}]})))
