(ns papadom.template-test
  (:require
   papadom.js.jquery
   papadom.js.handlebars
   [cemerick.cljs.test :as t]
   [papadom.template :refer [input-name input-path]])
  (:require-macros
   [cemerick.cljs.test :refer [is deftest with-test run-tests testing]]))

(deftest input-name-test
  (is (= "t-prop" (input-name false nil "prop")))
  (is (= "t-_{{@index}}_prop" (input-name true nil "prop")))
  (is (= "t-.a.b.prop" (input-name false "a.b" "prop")))
  (is (= "t-.a.b._{{@index}}_prop" (input-name true "a.b" "prop"))))

(deftest input-path-test
  (is (nil? (input-path ""))
      "blank")
  (is (= [:prop] (input-path "t-prop"))
      "simple property")
  (is (= [0 :prop] (input-path "t-_0_prop"))
      "property and index")
  (is (= [:a :b :prop] (input-path "t-.a.b.prop"))
      "scoped property")
  (is (= [:a :b 0 :prop] (input-path "t-.a.b._0_prop"))
      "scoped property and index"))
