# plotly-pyclj

A Clojure library designed to wrap plotly and plotly express for clojure users.

# Usage

Installation using deps.edn for now.

``` clojure
{:deps {ch.dpham/plotly-pyclj {:git "" :sha1 ""}}

```

# Why the name?

The original goal of the project was to mimick
[plotly.express](https://plotly.com/python/plotly-express/) api. A first
version of the library used the libpython-clj to interop with the python api
for plotly.

# Rationale

I just needed a simple plotting library for Clojure and ClojureScript, and I
liked [plotly.js](https://plotly.com/). Since Clojure is a language of maker, I
decided to use it. (This is it for the rationale).

When I started to write the library, [oz](https://github.com/metasoarous/oz)
and [hanami](https://github.com/jsa-aerial/hanami) /
[saite](https://github.com/jsa-aerial/saite) were leveraging
[vega](https://vega.github.io/vega/) and
[vega-lite](https://vega.github.io/vega-lite/). Although these solution could
be strictly superior, I found the API a bit to verbose for my simple needs, and
plotly has well financially supported with a lot of human hours invested in it
to cover most of my use case.

What I like about plotly:

- Good enough documentation, so that I am rarely frustrated;
- The JavaScript backend is easy to read and understand so that I can dig into
  it (without any knowledge of JavaScript).
- Good enough default parameters and ease of customization, for most of my
  requirements;
- Good enough story about export;
- Data oriented (the most important point);

The last point makes quite amenable for our Clojure and ClojureScript community
since we have our core library to perform operations on trees of maps.

I hope you will try my library, and find that `plotly-pyclj` can also be
useful, practical and fun.


# Docs

# Quick start

TODO: create sample data set, launch server and plot.

# Quick intro to plotly

Plotly renders a plot from a configuration map containing with keys `[:data
:layout :config]`.  The `:data` key is a sequence of `traces` which can be
understood as a specific group of data in a plot. For example, if a line plot
contains two lines, it will have two traces defining each line. The shape of
the data (e.g dots, lines, bar) is defined in trace. An example value of data is

``` clojure
;; a line and dots in the plot
{:data [{:x [0 1] :y [10 20] :name "first trace"}
        {:x [0 1] :y [15 5] :name "second trace" :mode :markers}]}
```

The optional `:layout` key contains the arguments of how the overall plot should
resemble, e.g. legends position, margins, title shape. Example:

``` clojure
{:layout {:margins {:t 0 :l 50 :b 50 :r 10}}
```

The last optional key `:config` describe some Javascript behavior such as
whether to show the plotly or the behavior of the export button. Example:

``` clojure
{:config {:toImageButtonOptions {:format "png" :height 560 :width 960 :scale 2}
          :displayModeBar "hover"
          :displaylogo false}}
```

Hence, a configuration map for plotly might look like these:

``` clojure
;; simple line plot
{:data [{:x [0 1] :y [10 20]}]}

;; plot with two lines
{:data [{:x [0 1] :y [10 20]}
        {:x [0 1] :y [15 5]}]
 :layout {:margins {:t 0 :b 50}}}

;; bar plot without logo
{:data [{:x [0 1] :y [10 20] :type :bar}
        {:x [0 1] :y [15 5] :type :bar}]
 :config {:displaylogo false}}
```

The [JavaScript documentation](https://plotly.com/javascript/) of the library
will provide you with numerous examples. As you can read, it quickly becomes
verbose, and the goal of the library is to expose the `plotly.express`in
Clojure to mimick their API.

## Philosophy

As Clojurians, data is exposed first. Hence functions in the library
manipulates a map of plotly arguments.

In Clojure, we expose data set in two shapes: sequence of maps, or maps of
sequences. The library supports both, but the first iteration will focus on
sequence of maps and provides tools to convert maps of sequence into the
sequences of map shape before applying the library logic.

So a typical data set will look like

``` clojure
[{:a 0 :b 2 :group :foo}
 {:a 0 :b 10 :group :bar}
 {:a 1 :b 3 :group :foo}]
```

Since data is exposed, we can manipulate our configuration map with our usual
`get-in`, `assoc-in` and `update-in` functions. The official plotly
documentation is the ground truth for knowing the appropriate path, although we
provide some simple support.


## API

Layout functions support three arities: the one arity for getting the argument,
and the two argument for `assoc`ing the tree and the multiple for `update`ing
the argument.

``` clojure
(require '[plotly-pyclj.layout :as l])

(def m {:data [{:x [0 1] :y [10 20] :type :bar}]
        :layout {:margins {:t 10 :b 50}}})

(l/margins m) ;; => {:t 10 :b 50}

(l/margins m {:t 0 :b 10 :l 10})
;; => {:data [{:x [0 1] :y [10 20] :type :bar}] :layout {:t 0 :b 10 :l 10}}

(l/margins m assoc :r 10)
;; => {:data [{:x [0 1] :y [10 20] :type :bar}] :layout {:t 10 :b 50 :r 10}}

(l/margins m update :t + 10) ;; same as (update {:t 10 :b 50} :t + 10)
;; => {:data [{:x [0 1] :y [10 20] :type :bar}] :layout {:t 20 :b 50}}
```

The paths are exposed in `plotly-pyclj.layout/paths`.

## Export

Export is only supported in Clojure for now, as the interop with python is used
to export the figure.


## License

Copyright © 2020 David Pham

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
