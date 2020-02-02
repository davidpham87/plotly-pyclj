(defproject plotly-pyclj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :paths ["src/clj" "public"]
  :dependencies
  [[bk/ring-gzip "0.2.1"]
   [cnuernber/libpython-clj "1.33"]
   [luminus-immutant "0.2.5"]
   [metosin/jsonista "0.2.5"]
   [metosin/muuntaja "0.6.4"]
   [metosin/reitit "0.3.9"]
   [metosin/ring-http-response "0.9.1"]
   [nrepl "0.6.0"]
   [org.clojure/clojure "1.10.1"]
   [org.clojure/tools.logging "0.4.1"]
   [ring-webjars "0.2.0"]
   [ring/ring-core "1.7.1"]
   [ring/ring-defaults "0.3.1"]]
  :plugins [[cider/cider-nrepl "0.23.0"]]
  :repl-options {:init-ns plotly-pyclj.core})
