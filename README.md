# plotly-pyclj

A Clojure library designed to wrap plotly and plotly express for clojure users.

# Usage

Installation using deps.edn for now.

``` clojure
{:deps {ch.dpham/plotly-pyclj {:git }}

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
