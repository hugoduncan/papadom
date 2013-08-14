(ns papadom.handlebars-test
  (:require
   papadom.js.jquery
   papadom.js.handlebars
   [cemerick.cljs.test :as t]
   [papadom.handlebars :refer [render]])
  (:require-macros
   [cemerick.cljs.test :refer [is deftest with-test run-tests testing]]))

(deftest render-test
  (is (= "1" (render "{{a}}" (clj->js {:a 1})))))
