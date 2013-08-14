(ns papadom.async-dom
  "Provide mappings from DOM elements to channels."
  (:require
   [cljs.core.async :as async :refer [<! >! chan put! alts!]]
   [clojure.string :refer [split]]
   [jayq.core :as jq :refer [$ attr prevent prop on]])
  (:require-macros
   [cljs.core.async.macros :as m :refer [go alt!]]))

;;; Basic conversion of js events into channel messages
(defn event-data
  "Return a data map for the javascript `event`, including attribute values for
  `attrs` from the event's `currentTarget`."
  [event attrs]
  (let [el (-> event .-currentTarget $)]
    (reduce
     (fn [m attrib]
       (assoc m (keyword attrib) (attr el attrib)))
     (-> el .data (js->clj :keywordize-keys true))
     attrs)))

(defn events
  "Send `events` from the given `selector` to the specified `channel`.
  Events are sent as a vector of event-name and the data from the event as a
  clojure map.  `events` is a keyword, or a sequence of keywords."
  [channel selector events event-name attrs]
  (on ($ "body") events selector {}
      (fn [e]
        (prevent e)
        (put! channel [event-name (event-data e attrs)]))))

(defn clicks
  "Send click from the given `selector` to the specified `channel`.
  Events are sent as a vector of event-name and the data from the event as a
  clojure map."
  [channel selector event-name attrs]
  (events channel selector :click event-name attrs))

(defn form-values
  "Return a sequence of the `form` element's input and select values.
  `field-key-fn` is a function of a single argument, the name of the input
  field, and should return a key to be used for the field, or nil to filter
  the field."
  [form field-key-fn]
  (filterv
   identity
   (map
    (fn [field]
      (let [field ($ field)]
        (if-let [n (attr field "name")]
          (let [p (field-key-fn n)]
            ;; (.log js/console (str "n: " n " fields-key-fn: " (pr-str p)))
            (if p
              [p (if (= "checkbox" (attr field "type"))
                   (boolean (prop field "checked"))
                   (jq/val field))])))))
    (js->clj ($ "input,select" ($ form))))))

(defn form-submit-events
  "Send submit events from the given `form-selector` to the specified `channel`.
  Events are sent as a two element vector containing the event-name and a
  sequence of vectors, each with a field key the input or select value for that
  field."
  [channel form-selector event-name field-key-fn]
  (on ($ "body") :submit form-selector {}
      (fn [e]
        (prevent e)
        (put! channel [event-name (form-values
                                   form-selector
                                   (or field-key-fn identity))]))))

;;; General channel functions
(defn merge-channels
  "Put messages from any of the `input-channels`, a sequence of channels, onto
  the output `channel`."
  [channel & input-channels]
  (go
   (loop []
     (put! channel (first (alts! input-channels)))
     (recur))))

(defn filter-channel
  "Put messages from the `input-channel` for which the `predicate` function
  returns a truthy value onto the output `channel`."
  [channel predicate input-channel]
  (go
   (loop []
     (let [value (<! input-channel)]
       (when (predicate value)
         (put! channel value))
       (recur)))))

;;; Event channel functions
(defn filter-events
  "Filter events with an event-name in the `event-names` set, onto the output
  `channel`"
  [channel event-names input-channel]
  (filter-channel channel (comp event-names first) input-channel))
