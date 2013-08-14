(ns papadom.template
  "HTML template based on recognising attributes in a standard html file.
  Supports mustache content templates."
  (:require
   [clojure.string :as string]
   [clojure.set :refer [difference]]
   [jayq.core :as jq
    :refer [$ after append attr before clone html prepend prop]]
   [papadom.async-dom :refer [clicks events form-submit-events]]
   [papadom.handlebars :refer [compile]]))

;;; TODO - and a t-value for form controls, so that checkboxes can be made
;;; simpler and so that the value templates can be dropped and not rendered in
;;; the unprocessed template.

;;; similarly it would be good to have nested templates, to avoid mustache
;;; section markup in the unprocessed templates.
(defn- mustache-node
  "Create a node that will be converted into a mustache expression."
  [t]
  ($ (str "<div class=\"t-mustache\">" t "</div>")))

(def ^:private mustache-node-regex
  ;; [\s\S] is used as a proxy for dotall
  #"<div class=\"t-mustache\">([\s\S]*?)</div>")

(def nested-ctrl-selector
  "[t-template] input, [t-template] select, [t-scope] input, [t-scope] select")

(defn input-name
  "Return an input name for the specified inputs."
  [index? scope prop]
  (if prop
    (str "t-"
         (if scope (str "." scope "."))
         (if index? "_{{@index}}_")
         prop)))

(defn input-path
  "Return a path vector for the given input-name."
  [input-name]
  (if-not (string/blank? input-name)
    (let [[_ scope index prop] (re-matches
                                #"t-(?:\.(.*)\.)?(?:_(.*)_)?(.*)" input-name)
          _ (assert _ "input-path called with invalid input-name")
          v (if scope (mapv keyword (string/split scope #"\.")) [])
          prop (keyword prop)]
      (if index
        (conj (conj v (js/parseInt index)) prop)
        (conj v prop)))))

(defn- scope-control
  "Wrap a the `ctrl` node in a scope block for `t-prop`."
  ([ctrl t-prop inverted?]
     (before ctrl (mustache-node (str (if inverted? "{{^" "{{#") t-prop "}}")))
     (after ctrl (mustache-node (str "{{/" t-prop "}}"))))
  ([ctrl t-prop]
     (scope-control ctrl t-prop nil)))

(defn- scope-children
  "Wrap a the `ctrl` node's children in a scope block for `t-prop`."
  [ctrl t-prop]
  (prepend ctrl (mustache-node (str "{{#" t-prop "}}")))
  (append ctrl (mustache-node (str "{{/" t-prop "}}"))))

(defn- wrap-control
  "Wrap a the `ctrl` node in conditional blocks for `t-prop`.  Returns
  a clone of `ctrl` that is in the block produced when `t-prop` is false."
  [ctrl t-prop]
  (let [ctrl1 (clone ctrl)]
    (after ctrl ctrl1)
    (scope-control ctrl t-prop)
    (scope-control ctrl1 t-prop :inverted)))

(defn value->select-options
  "Convert a value into a map that can be used to display options for the value.
  `options` is a sequence of maps, each with `:value` and `:label` keys.
  Returns a sequence of maps with `:value`, `:label` and `:selected` keys."
  [selected-value options]
  (mapv
   (fn [{:keys [value] :as m}]
     (if (= selected-value value)
       (assoc m :selected true)
       m))
   options))

(defn process-template
  "Process a t-template or t-contents node `el`.  A t-template will have index?
  true. `scope` is a string that passes any parent scope, and block is a the
  local scope for the current element `el`."
  [el index? scope block]
  (let [original-el el
        [el parent] (if (attr el "t-elide-node")
                      [el el]
                      (let [c (clone el)]
                        [c (append ($ "<div/>") c)]))]
    (when block
      (scope-children parent block))

    ;; Process control names, etc
    (let [all-ctrls (js->clj ($ "input,select" el))
          nested-ctrls (js->clj ($ nested-ctrl-selector el))
          unnested-ctrls (difference (set all-ctrls) (set nested-ctrls))]
      (doseq [c unnested-ctrls
              :let [ctrl ($ c)
                    t-prop (attr ctrl "t-prop")
                    t-name (attr ctrl "name")
                    tag (prop ctrl "tagName")]]
        (when (not t-name)
          (when-let [n (input-name index? scope t-prop)]
            (attr ctrl "name" n)))
        (condp = tag
          "INPUT" (let [ctrl-type (attr ctrl "type")]
                    (when (and t-prop (not= "checkbox" ctrl-type))
                      (attr ctrl "value" (str "{{" t-prop "}}")))
                    (when (= "checkbox" ctrl-type)
                      (wrap-control ctrl t-prop)
                      (attr ctrl "checked" "")))
          "SELECT" (let [option ($ "option" ctrl)]
                     (doseq [e extra]
                       (jq/remove ($ e)))
                     (html option "{{label}}")
                     (attr option "value" "{{value}}")
                     (scope-control option t-prop)
                     (wrap-control option "selected")
                     (attr option "selected" "")))))

    ;; Process nested scopes and templates
    (doseq [c ($ "[t-scope]" el)
            :let [ctrl ($ c)
                  t-scope (attr ctrl "t-scope")
                  m (process-template
                     ctrl false (if scope (str scope "." t-scope) t-scope)
                     t-scope)]]
      (after ctrl (mustache-node (:template m)))
      (.remove ctrl))

    (doseq [c ($ "[t-template]" el)
            :let [ctrl ($ c)
                  t-scope (attr ctrl "t-template")
                  m (process-template
                     ctrl true (if scope (str scope "." t-scope) t-scope)
                     t-scope)]]
      (before ctrl (mustache-node (:template m)))
      (.remove ctrl))

    (let [template (string/replace (html parent) mustache-node-regex "$1")]
      ;; (.log js/console (str "template: " template))
      {:node original-el
       :template template
       :html (compile template)
       :type :t-template})))

;;; It would be great if the templates could be pre-compiled
;;; instead of compiled at runtime.
(defn cache-templates []
  (let [contents ($ "[t-content]")
        templates ($ "[t-template]")]
    (reduce merge
            (concat
             (for [e contents
                   :let [el ($ e)
                         t-name (attr el "t-content")
                         m (process-template el false nil nil)]]
               (do
                 ;; (.log js/console (str "t-content: " t-name))
                 {t-name m}))
             (for [e templates
                   :let [el ($ e)
                         t-name (attr el "t-template")
                         m (process-template el true nil t-name)]]
               (do
                 ;; (.log js/console (str "t-template: " t-name))
                 {t-name m}))))))

(def templates nil)

(defn compile-templates
  "Compiles templates for the current page."
  []
  (set! templates (cache-templates)))

(defn template-for-name [t-name]
  (if-let [t (get templates t-name)]
    t
    (throw (js/Error. (str "Unknown template: " t-name
                           " known templates:" (vec (keys templates)))))))

(defn render
  "Set the content for a single-valued template."
  [t-name vals]
  (let [t (template-for-name t-name)
        s ((:html t) (clj->js vals))]
    ;; (.log js/console (str "t-name: " t-name
    ;;                       "  template: " (:template t)
    ;;                       "  vals: " vals
    ;;                       "  html: " s))
    (html (:node t) s)))

;;; Template based events
(defn template-node-events
  "Put events from the template html node `el,` onto `channel`."
  [channel el]
  (let [tag (prop el "tagName")
        event-name (attr el "t-event")
        event-id (attr el "t-id")]
    (condp = tag
      "A" (clicks channel (str "[t-event=" event-name "]") (keyword event-name)
                  (string/split event-id #","))
      "INPUT" (let [t (attr el "type")]
                (events channel (str "[t-event=" event-name "]") :change
                        (keyword event-name)
                        (conj (string/split event-id #",")
                              (if (= t "checkbox")
                                "checked"
                                "value"))))
      "FORM" (form-submit-events channel (str "[t-event=" event-name "]")
                                 (keyword event-name) input-path))))

(defn template-events
  "Put events from template nodes with t-event attributes onto the output
  `channel`."
  [channel]
  (doseq [el (js->clj ($ "[t-event]"))]
    (template-node-events channel ($ el))))

(defn input-seq->map
  "Convert the input-seq (a sequence of path, value vectors) to a map."
  [input-seq]
  (reduce
   (fn [m [p v]] (assoc-in m p v))
   {}
   input-seq))
