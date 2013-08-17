(ns papadom.template-test
  (:require
   papadom.js.handlebars
   [cemerick.cljs.test :as t]
   [jayq.core :as jq
    :refer [$ after append attr before clone html prepend prop]]
   [papadom.template
    :refer [input-name input-path clear-templates compile-templates
            render template-for-name]])
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

(defn id-selector [id]
  (str "#" (name id)))

(defn add-element
  [el-id el-content]
  (let [el ($ "<div/>")]
    (attr el "id" (name el-id))
    (append el ($ el-content))
    (append ($ "body") el)))

(defn remove-element
  [el-id]
  (jq/remove ($ (id-selector el-id) "body")))

(defn element-html
  [el-id]
  (html ($ (id-selector el-id))))

(deftest compile-templates-test
  (add-element :t "<p t-template=\"t\">{{p}}</p>")
  (clear-templates)
  (compile-templates)
  (is (template-for-name "t"))
  (is (= "{{#t}}<p t-template=\"t\">{{p}}</p>{{/t}}"
         (:template (template-for-name "t"))))
  (is (= "<p t-template=\"t\">a</p><p t-template=\"t\">b</p><p t-template=\"t\">c</p>"
         ((:html (template-for-name "t"))
          (clj->js {:t [{:p "a"}{:p "b"}{:p "c"}]}))))
  (remove-element :t))

(deftest render-test
  (testing "no elide"
    (add-element :t "<p t-template=\"t\">{{p}}</p>")
    (clear-templates)
    (compile-templates)
    (is (template-for-name "t"))
    (render "t" {:t [{:p "a"}{:p "b"}{:p "c"}]})
    (is (= "<p t-template=\"t\">a</p><p t-template=\"t\">b</p><p t-template=\"t\">c</p>"
           (element-html :t)))
    (remove-element :t))
  (testing "no elide with prior element"
    (add-element :t "<div id=\"a\"/><p t-template=\"t\">{{p}}</p>")
    (clear-templates)
    (compile-templates)
    (is (template-for-name "t"))
    (render "t" {:t [{:p "a"}{:p "b"}{:p "c"}]})
    (is (= (str "<div id=\"a\"></div>"
                "<p t-template=\"t\">a</p>"
                "<p t-template=\"t\">b</p>"
                "<p t-template=\"t\">c</p>")
           (element-html :t)))
    (remove-element :t))
  (testing "elide"
    (add-element :t
                 "<div t-template=\"t\" t-elide-node=\"1\"><p>{{p}}</p></div>")
    (clear-templates)
    (compile-templates)
    (is (template-for-name "t"))
    (render "t" {:t [{:p "a"}{:p "b"}{:p "c"}]})
    (is (= (str "<p t-template=\"t\">a</p>"
                "<p t-template=\"t\">b</p>"
                "<p t-template=\"t\">c</p>")
           (element-html :t)))
    (testing "repeated render"
      (render "t" {:t [{:p "c"}{:p "b"}{:p "a"}]})
      (is (= (str "<p t-template=\"t\">c</p>"
                  "<p t-template=\"t\">b</p>"
                  "<p t-template=\"t\">a</p>")
             (element-html :t))))
    (remove-element :t))
  (testing "elide with prior element"
    (add-element
     :t (str "<div id=\"a\"/>"
             "<div t-template=\"t\" t-elide-node=\"1\"><p>{{p}}</p></div>"))
    (clear-templates)
    (compile-templates)
    (is (template-for-name "t"))
    (render "t" {:t [{:p "a"}{:p "b"}{:p "c"}]})
    (is (= (str "<div id=\"a\"></div>"
                "<p t-template=\"t\">a</p>"
                "<p t-template=\"t\">b</p>"
                "<p t-template=\"t\">c</p>")
           (element-html :t)))
    (remove-element :t)))
