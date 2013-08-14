(ns papadom.example.protocols
  "Protocols on JS objects."
  (:require [goog.object :as gobject]))

(defn strkey
  "Helper fn that converts keywords into strings"
  [x]
  (if (keyword? x)
    (name x)
    x))

;;; Extend array with basic clojure protocols
;;; See https://github.com/pangloss/clang/blob/master/client/clang/js_types.cljs
;;;     http://keminglabs.com/blog/angular-cljs-weather-app/
(extend-type array
  IAssociative
  (-assoc [o k v]
    (conj o [k v]))

  ICollection
  (-conj [parent [k v]]
    (let [a (js-obj)]
      (assoc! a k v)
      (goog.object.extend a parent)
      a))

  IEmptyableCollection
  (-empty [a]
    (array))

  ILookup
  (-lookup
    ([a k] (aget a (strkey k)))
    ([a k not-found]
      (let [s (strkey k)]
        (if (goog.object.containsKey a s)
          (aget a s)
          not-found))))

  ISeqable
  (-seq [a]
    (map (fn [k] [k (get a k)]) (js-keys a)))

  ITransientCollection
  (-conj! [a x]
    (.push a x)
    a)
  (-persistent! [a]
    (into [] a))

  ITransientAssociative
  (-assoc! [a k v]
    (aset a (strkey k) v)
    a))
