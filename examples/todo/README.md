# todos

A simple TODO app written with papadom.

## Usage

To try this, run `lein ring server`.

## JavaScript Libraries

Papadom uses handlebars.js and jQuery.  Since we use jQuery via jayq, it has to
be specified as a `&lt;script&gt;` tag in the browser.

You can provide `handlebars.js` in several ways.

See [Luke Vanderhart's post][lukespost] for a general introduction to using
Javascript libraries in Clojurescript.

### SCRIPT Link in the HTML Page

You can add a `<script>` links in the applications web page to provide
handlebars.js.  In this case you should create an empty `papadom.js.jquery`
namespace.

To use `:optimizations` other than `:whitespace` with this approach, you will
need to add `:externs` to the `:compiler` map in you cljsbuild definitions.

### Use :foreign-libs with Papadom Provided Versions

To use the versions of the handlebars.js included with Papadom, add the
following to your lein-cljsbuild `:compiler` configuration maps.

```clj
:foreign-libs
  [{:file "papadom/js/handlebars.js"
    :provides ["papadom.js.handlebars"]}]
```

### Use :foreign-libs with Custom Versions

Add your own versions of handlebars.js as a resource, and add
`:foreign-libs` definitions as above, but with the `:file` value pointing to the
classpath relative path for your versions of the libraries.

## License

Copyright © 2013 Hugo Duncan

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
