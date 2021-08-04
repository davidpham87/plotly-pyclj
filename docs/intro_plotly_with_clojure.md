---
title: Data Visualization, Plotly and the Clojure Way
author: David
toc: false

...

# Introduction

The goal of this presentation is to justify the use of `plotly.js` and provide
a good understanding of its concepts and tools in order to create stunning
visualizations. Moreover, `plotly-pyclj`, a wrapper library in Clojure is
presented.

# Visualization (Personal Experience)

- Creating a visualization is summarized as mapping information/data into lines
  and dots, and to draw those on a canvas or some area. In modern days, one
  also has to include user events, which might reshape or extend the raw data.

- In the 2000s, most visualization technologies (Excel, Matlab, R, matplotlib)
  were around static graph rendering, led by an API driven definition of
  graphics resulting in using OOP and global mutable state cacophony.

- In the 2010s, start of interactive, animated, 3D and web rendered
  visualizations (mostly JavaScript and low-level GPU C/C++) with the advent of
  data only APIs.

# Evolution: API with global states R

``` r
plot.new()

plot(c(0, 1, 2), c(2, 5, 10))
line(c(0, 1, 2), c(5, 10, 20))
# ...
line(c(0, 1, 2), c(20, 5, 10))

# Could have been performed with a single call
title('Popularity of American names')
title(xlab='Time')
title(ylab='Number of babies born')

dev.off()

```

- Global state.
- Mixing data transformation and plot definition.
- Hard to serialize.

# Evolution: API with Grammar of Graphics

Around 2010s, the R community (especially Hadley Wickham) started to push for
the grammar of graphics concepts with `ggplot2`.

``` r
ggplot(data, aes(x=year, y=n, group=name, color=name)) +
  geom_line() +
  scale_color_viridis(discrete = TRUE) +
  ggtitle("Popularity of American names") +
  theme_ipsum() +
  ylab("Number of babies born")
```

- The package overload the `+` operator to change the properties of the plot
object.

- This already leads to the first improvement: a clear separation between the
definition and the rendered object.

- Still impossible to serialize.

# Evolution (cont'd): Specification Based (edn)

Same as before, with plotly vocabulary (others are similar).

``` clojure
{:data [{:x [0 1 2] :y [2 5 10]}
        {:x [0 1 2] :y [5 10 20]}
        ;; ...
        {:x [0 1 2] :y [15 5 10}]
 :layout {:title ""
          :xaxis "Time"
          :yaxis "Number of babies Born"}}
```

- This configuration can then be passed to a rendering function which will take
  care of creating the correct transformation and mapping to make the
  visualization.

- This is inert data and has no tie to the state. The advantage is
  obviously trivial serialization and data manipulation.

# Plotly.js - Why?

- Plotly is one of many contender for interactive data-driven visualization
  library leveraging `d3.js` (a common low level svg manipulation library).

- Other solutions: vega(-lite).js, react-vis, bokeh.js.

- Most of its funding have been provided by financial institution and hence it
  solves many of our visualization problems (meaning time series).

- As said, plotly is data-driven, meaning its output can be derived solely from
  a configuration file taking the form of tree of data.

- One of few libraries that really tries to solve the static image export.

- Open source and free.

# Plotly.js, Plotly R, Plotly.express, Dash

- Most practitioners learns high level APIs of Plotly in their preferred
  languages (usually R or python). This is why Dash (an equivalent to R Shiny)
  was developed.

- But there is a contradiction here: plotly is data-driven, yet most use code
  instructions to create the plots.

# Clojure, Why?

- There are many API for different languages for plotly already (R, python,
  julia).

- I contend these are too much oriented to making good OOP, and hence
  superfluous, overly complicated and rarely compose.

- Plotly is defined through its configuration (a plain data tree), and hence
  one is better served by working directly with data.

- Remember DATA $<<<$ API $<<<$ DSL in complexity (where each $<<<$ sign is one
  order of magnitude).

- Exactly same functions will be shared on both backend and frontend, as they
  are data manipulating functions (think of setting and remove keys and values
  from a dictionary).

# Plotly Concepts

- A plotly chart specification is an open hash-map/dictionary/structure with the
keys `[:data :layout :config]`.

- The `:data` key is an ordered collection (vector) of the *traces*, a specific
  group of data points displayed together. E.g. each line in a plot belong to a
  different *trace*. A trace is map whose content depends of its *type*. The
  most common examples are:
  - `{:x [...], :y [...], :name "standard-line"}`
  - `{:values [...], :type :pie, :name "standard-pie"}`

- The `:layout` key is about the overall look plot, e.g. margins, labels,
  axes, legends, fonts.

- The `:config` key defines the meta behavior of the plot such as displayed
  button, displayed logo etc. This is an advanced feature and is rarely used
  when doing exploratory data analysis.

# Plotly Example (cont'd)

``` clojure
{:data [{:x [0 1] :y [10 20] :name "first trace"}
        {:x [0 1] :y [15 5] :name "second trace"
         :mode :markers}]
 :layout {:margins {:t 0 :l 50 :b 50 :r 10}}
 :config {:toImageButtonOptions
          {:format "png" :height 560
           :width 960 :scale 2}
          :displayModeBar "hover"
          :displaylogo false}}
```
Most of the time, the challenge is to define transform the data correctly to
wrap them around traces.

# Clojure bindings: plotly-pyclj

## Why?

- Clojure was missing a simple visualization library that could handle
interactive and static export of charts.

- In my free time, I built this little library which uses the browser as UI and
makes a server that to serve plotly configuration through websockets for fast
updated.

## Quick Start?

``` clojure
(require '[plotly-pyclj.core :as pc])
(pc/start!) ;; starts the server on port 8987
;; open a browser on http://localhost:8987

(let [plotly-config
      {:data [{:x [0 1] :y [0 1]}
              {:x [0 1] :y [3 2] :type :bar}]}]
  (pc/plot plotly-config))
```

# Static Export of Interactive Charts

 - This is one of the biggest challenge of the previous years.
 - The solution is usually to use a headless browser (a browser without UI), to
   render some JavaScript and then to ask the result back.
 - One of the biggest disadvantages is that the solution is quite hard to
   install and implemented behind tight firewalls.
 - Solutions is *kaleido* from plotly, which can be installed through `pip`. It
   is a light version of chrome and can distributed with its binaries.

# Kaleido

After installing Kaleido through pip, you need to find its repository with `pip
show kaleido`. If Kaleido is callable from your `$PATH`, you can simply export
your plot as

``` clojure
(require '[plotly-pyclj.core :as pc])
(let [plotly-spec
      {:data [{:x [0 1] :y [0 1]}
              {:x [0 1] :y [3 2] :type :bar}]
       :layout {:title "Test"}}

      export-spec {:filename "test" :format "png"
                   :width 960 :height 540
                   :scale 1.5}]
  (pc/export plotly-spec export-spec))
```

# Kaleido (cont'd)

In secure environment, that is behind firewalls, kaleido is usually unable to
download `plotly.js`, so it has to be downloaded through `pip install plotly`
as well. You need get the path of its minified `js` file usually in the folder
of

\small
``` bash
echo "$(pip show plotly)/plotly/package_data/plotly.min.js"
```
\normalsize

Take that path and before calling `plotly.core/export` call

``` clojure
(require '[plotly-pyclj.plot :as pp])
(pp/update-kaleido-args!
 (fn [m] (assoc m :plotlyjs "path/to/plotly.js")))
```

# Server Mode

If you create the following `deps.edn`

``` clojure
{:deps
 {org.clojars.davidpham87/plotly-pyclj
 {:mvn/version "0.1.7"}}}
```

You can then create a server mode

``` bash
clojure -m plotly-pyclj.core
# starts the server on port 8987
# then open a browser on http://localhost:8987
```

Open a browser on `http://localhost:8987`, then send post request to
`http://localhost:8987/plotly`.

# Server Mode (cont'd, clojure)

In Clojure

``` clojure
(require '[org.httpkit.client :as client])
(require '[jsonista.core :as j])
(let [m {:data [{:x [0 1 3] :y [4 2 1] :type :bar}
                {:x [0 1] :y [-2 4]}]}]
  (client/post
   "http://localhost:8987/plotly"
   {:headers {"content-type" "application/json"}
    :body (j/write-value-as-string m)}))
```

# Server Mode (cont'd, python)

In Python

``` python
import requests
import json

requests.post(
    "http://localhost:8987/plotly",
    headers={"content-type": "application/json"},
    data=json.dumps(
    {"data": [{"x": [0, 1], "y": [-1, 1]},
              {"x": [0, 1], "y": [-2, 2]}]}))
```

# Extensions

- .Net languages (officially supported), API already exists, but an custom
  extension would be necessary to work with the `json` object itself.

- Matlab, since 2015, supports the `webwrite` function which make `POST`
  request and `json` encoding and decoding is supported since 2017b. Hence by
  writing the chart specification as a data structure, Matlab could support
  `plotly` as well.

- R and python have their own API, but nothing prevents to use the data way.

# Further work

- The library has a primitive support for helping the user with documentation
  by leveraging the entire plotly specification, but it is not thoroughly
  tested. However, documentation website from plotly.js is so good, that this
  has never been a real problem.

- Create some sugar functions to handle orderd collections of maps or
  tech.ml.dataset (dataframe) structures.

# Conclusion and Take Aways

As usual, the old advice works: simplification means disentangling things
(global state, input data transformation, chart specification).

The key take away of the talk is the concept of visualization is more about
manipulation of information and generic data structures than about calling
specialized functions on after each other.

This will allow you to get leverage and reuse, or as someone said it with
better words:

    This is the way (The Mandalorian)
