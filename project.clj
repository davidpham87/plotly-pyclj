(defproject plotly-pyclj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :resource-paths ["public"]
  :middleware [lein-tools-deps.plugin/resolve-dependencies-with-deps-edn]
  :plugins [[cider/cider-nrepl "RELEASE"]
            [refactor-nrepl "RELEASE"]
            [lein-tools-deps "0.4.5"]]
  :lein-tools-deps/config {:config-files [:install :user :project]
                           :aliases [:clj]}
  :repl-options {:init-ns plotly-pyclj.core})
