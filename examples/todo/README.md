# todos

A simple TODO app written with papadom.

## Usage

To try this, run `lein ring server`.

## JavaScript Libraries

**NOTE: the js dependencies aren't working yet via :foreign-libs!**

Papadom uses `handlebars.js` and `jquery.js`.  You can provide these libs in
several ways.

### <script> Links in the HTML Page

You can add `<script>` links in the applications web page to provide the
JavaScript libs.  In this case you should not provide them via `:foreign-libs`
configuration in your cljsbuild definition, and you should not require the
`:foreign-libs` namespaces in your application code.

### Use :foreign-libs with Papadom Provided Versions

To use the versions of the JavaScript libraries included with Papadom, add the
following to your lein-cljsbuild `:compiler` configuration maps, and
require the`papadom.js.handlebars` and `papadom.js.jquery` namespaces in your
application code.

```clj
:foreign-libs
  [{:file "papadom/js/jquery.js"
    :provides ["papadom.js.jquery"]}
   {:file "papadom/js/handlebars.js"
    :provides ["papadom.js.handlebars"]}]
```

### Use :foreign-libs with Custom Versions

Add your own versions of the JavaScript libraries as resources, and add
`:foreign-libs` definitions as above, but with the `:file` value pointing to the
classpath relative path for your versions of the libraries.  Require
the`papadom.js.handlebars` and `papadom.js.jquery` namespaces in your
application code.

## License

Copyright © 2013 Hugo Duncan

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
