# plotly-pyclj

A Clojure library designed to wrap plotly and a subset of plotly express for
clojure users.

[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.davidpham87/plotly-pyclj.svg)](https://clojars.org/org.clojars.davidpham87/plotly-pyclj)

# Quick Start

Add the dependencies:

``` clojure
{:deps {org.clojars.davidpham87/plotly-pyclj {:mvn/version "LATEST"}}
```

In a namespace, require `plotly-pyclj.core`

``` clojure
(require '[plotly-pyclj.core :as pc])

(pc/start!) ;; starts the server on port 8987

;; open a browser on http://localhost:8987

(let [plotly-config {:data [{:x [0 1] :y [0 1]}
                            {:x [0 1] :y [3 2] :type :bar}]}]
  (pc/plot plotly-config))
```

See the result on the browser.

## Export to static image: Kaleido

In order to export plotly specs into a static images (png, pdf, jpeg, webp,
svg), we leverage on the [Kaleido](https://github.com/plotly/Kaleido) project
from plotly, which bundles only the necessary part of a web browser to generate
the files. The advantage is that you can install it through python `pip` or
download the binaries from the release page.

If you decide to install Kaleido with pip3, you can add the following script as
`kaleido` on your path. Otherwise, modify the `DIR` variable to point out to
the location of the root folder of the `kaleido` root folder project.

``` bash
#!/bin/bash
DIR="$(pip3 show kaleido | grep Location: | sed s/"Location: "//)/kaleido/executable"

export LD_LIBRARY_PATH=$DIR/lib:$LD_LIBRARY_PATH
export FONTCONFIG_PATH=$DIR/etc/fonts
export XDG_DATA_HOME=$DIR/xdg
unset LD_PRELOAD

cd $DIR
./bin/kaleido $@
```

You can always set the command line with the `plotly-pyclj.plot/set-kaleido-command!` function.

On Windows, you might rewrite the `kaleido.cmd` to point to the
directory. Behind a firewall, you might need to specify the path to a `plotly.js` file, this can be done as following

``` clojure
(plotly-pyclj.plot/update-kaleido-args!
 (fn [m] (assoc m :plotlyjs "path/to/plotly.js")))
```

Otherwise, before starting any export, you can always override the command line args in the `plotly-pyclj.plot/ensure-kaleido!`

``` clojure
(plotly-pyclj.plot/ensure-kaleido!
 {:exec-args ["--disable-gpu"]
  :exec-path "some/crazy/path/kaleido"})
```

If Kaleido is setup correctly, you can simply export your plot as

``` clojure
(let [plotly-spec {:data [{:x [0 1] :y [0 1]}
                          {:x [0 1] :y [3 2] :type :bar}]
                   :layout {:title "Test"}}
      export-spec {:filename "test"
                   :format "png"
                   :width 960
                   :height 540
                   :scale 1.5}]
  (plotly.core/export plotly-spec export-spec))
```

# Why the name?

The original goal of the project was to mimick
[plotly.express](https://plotly.com/python/plotly-express/) api for Clojure.

# Rationale

I just needed a simple plotting library for Clojure and ClojureScript, and I
liked [plotly.js](https://plotly.com/). Since Clojure is a language of maker, I
decided to use it. (This is it for the rationale).

The goal of the project is really just to have dead simple plotting library,
where user could simply call

``` clojure
(px/scatter {:data data :x :x :y :y})
(px/line {:data data :x :x :y [:y1 :y2]})
```

and see the result in a web browser. It has to be simple while retaining some
ability to extend and modify if necessary.

When I started to write the library, [oz](https://github.com/metasoarous/oz)
and [hanami](https://github.com/jsa-aerial/hanami) /
[saite](https://github.com/jsa-aerial/saite) were leveraging
[vega](https://vega.github.io/vega/) and
[vega-lite](https://vega.github.io/vega-lite/). Although these solution could
be strictly superior, I found the API a bit to verbose for my simple needs, and
plotly has well financially supported with a lot of human hours invested in it
to cover most of my use case.

My goal for the UI is to have something similar to R Studio plot device
(previous/next plot) and see the representation of the data (html, json,
edn). (Maybe support for hiccup).

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

API documentation are
[here](https://plotly.com/python-api-reference/generated/plotly.express.line.html)
(from python).

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

## Helper

Plotly actually defined a json schema and it is leveraged in our codebase.  We
pulled the relevant part of the schema to create a discoverable api. The
biggest frustration when dealing with plotly is to know which fields are
allowed, what are their default and their valid values. To that effect, the
plotly-pyclj.core/help supports you to discover which the tree structure of the
traces, layout and config maps.

``` clojure
(require
    '[plotly-pyclj.core :refer (help)]
    '[plotly-pyclj.traces :as traces])

(help traces/scatter) ;; same as (help [:traces :scatter]) where the vector is the path inside the json schema
{:animatable true,
 :attributes {},
 :categories
 ["cartesian"
  "svg"
  "symbols"
  "errorBarsOK"
  "showLegend"
  "scatter-like"
  "zoomScale"],
 :meta {},
 :type "scatter"}

(help traces/scatter-attributes) ;; [:traces :scatter :attributes] too big to show
(help traces/scatter-x) ;; the path is actually [:traces :scatter :attributes :x] (help traces/scatter-x)
```

In general for traces, a specific traces (e.g. scatter, bar) will be placed
under `traces/scatter` and its attributes `x` will be joined with a dash, like
`traces/scatter-x`.


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

The paths are exposed in `plotly-pyclj.layout/paths`. Idem for `:config`.

The data component (`:traces`) is trickier as the `:data` key can be a sequence
of traces.


# Development

You will need an instance of babashka (bb) on your path.

## ClojureScript

- You will probably need to install the npm dependencies `npm install --save`.
- Then `shadow-cljs watch ui`.

## Release steps

- Compile the clojurescript with `make release-ui`
- Create the jar `make jar`
- Create the `env.edn` file with the `:clojars/username` and `:clojars/password` keys and execute `make jar`.

## Export documentation

The documentation use =mkdocs-material= to generate the documentation.

## License

Copyright © 2020-2021 David Pham

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
