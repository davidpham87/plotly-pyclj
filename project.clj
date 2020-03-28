(defproject plotly-pyclj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :source-paths ["src/clj"]
  :resource-paths ["public"]
  :dependencies
  [[bk/ring-gzip "0.2.1"]
   [clj-python/libpython-clj "1.38"]
   [luminus-immutant "0.2.5"]
   [metosin/jsonista "0.2.5"]
   [metosin/muuntaja "0.6.6"]
   [metosin/reitit "0.4.2"]
   [metosin/ring-http-response "0.9.1"]
   [nrepl "0.7.0-SNAPSHOT"]
   [org.clojure/clojure "1.10.1"]
   [org.clojure/tools.logging "0.4.1"]
   [org.clojure/spec.alpha "0.2.176"]
   [org.clojure/core.specs.alpha "0.2.44"]
   [ring-webjars "0.2.0"]
   [ring/ring-core "1.7.1"]
   [ring/ring-defaults "0.3.1"]]
  :plugins [[cider/cider-nrepl "0.25.0-SNAPSHOT"]]
  :repl-options {:init-ns plotly-pyclj.core})
