(ns papadom.handlebars
  "Handlebars based templating lib")

(defn ^:extern render
  "Render the handlebars template s, using the map m to provide values."
  [s m]
  ((.compile js/Handlebars s) m))

(defn compile
  "Compile the handlebars template s, into a function."
  [s]
  (.compile js/Handlebars s))
