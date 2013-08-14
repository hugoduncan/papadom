# papadom

Use html pages as templates for your clojurescript applications.

The library is based on html being the defining source for both the visual
appearance of you application, and the events that drive it.  Events are
delivered via `core.async` channels.

**EXPERIMENTAL**

*NOTE: the tests don't run yet due to :foreign-libs issues!*

*NOTE: the js dependencies aren't working via :foreign-libs yet!*

## Usage

Papadom uses custom attributes to recognise templates and elements that should
generate events when the user interacts with them.  See the example
[todo app page](examples/todo/resources/public/index.html).

## Recognised Attributes

`t-content`
: declares a template scope that will be passed a map of values that may be used
in mustache type template expressions.  The value of the`t-content` is the name
of the template.

`t-template`
: declares a template scope that will be passed a sequence of values.  For each
element in the sequnce, the contents of the element with the `t-template`
attribute will be rendered.  The element of the sequence becomes the current
expression for mustache type template expressions.  The value of the
`t-template` is both the name of the expression to iterate over, and the name
of the template.

`t-scope`
: within a `t-content` or `t-template` element, this can be used to further
narrow the current expression for mustache type template expressions.

`t-event`
: used to indicate that the element should generate an event.  The attribute
value becomes the name of the event.

`t-id`
: used with elements having a `t-event` attribute.  Specifies a comma
separated list of attribute names to be passed in the event data.

## Clojurescript functions

The `papadom.template` namespace provides functions for interacting with the
templates from within your application.  See the example
[todo app code](examples/todo/src/papadom/example/todo.cljs).

The `compile-templates` function must be called first, in order to compile the
templates in the current page.

The `template-events` is called with a `core.async` channel which is configured
to receive events from `t-event` annotated elements.  You're application reads
events from the event channel, modifies it's internal state, calling `render`
to re-display the templates based on that state.

## JavaScript Libraries

**NOTE: the tests don't run yet due to :foreign-libs issues!**
**NOTE: the js dependencies aren't working via :foreign-libs yet!**

Papadom uses `handlebars.js` and `jquery.js`.  You can provide these libs in
several ways.

See [Luke Vanderhart's post][lukespost] for a general introduction to using
Javascript libraries in Clojurescript.

### <script> Links in the HTML Page

You can add `<script>` links in the applications web page to provide the
JavaScript libs.  In this case you should not provide them via `:foreign-libs`
configuration in your cljsbuild definition, and you should not require the
`:foreign-libs` namespaces in your application code.

To use `:optimizations` other than `:whitespace` with this approach, you will
need to add `:externs` to the `:compiler` map in you cljsbuild definitions.

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


[lukespost]: http://lukevanderhart.com/2011/09/30/using-javascript-and-clojurescript.html "Luke Vanderhart's post on JavaScript libs"
