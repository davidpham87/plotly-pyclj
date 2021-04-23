(def env (edn/read-string (slurp "env.edn")))

(println
 (:out (shell/sh
        "env"
        (str "CLOJARS_USERNAME=" (:clojars/username env))
        (str "CLOJARS_PASSWORD=" (:clojars/password env))
        "clojure" "-X:deploy"
        ":artifact"
        "\"target/plotly-pyclj.jar\"")))
