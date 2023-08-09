version=LATEST

cljs-release:
	clj -A:cljs:cljs-build:cljs-release

jar:
	clojure -T:build jar

deploy-clojars:
	env \
	CLOJARS_USERNAME=$(bb -e '(:clojars/username (edn/read-string (slurp "env.edn")))') \
	CLOJARS_PASSWORD=$(bb -e '(:clojars/password (edn/read-string (slurp "env.edn")))') \
	clojure -X:deploy :artifact '"target/plotly-pyclj.jar"'

deploy-clojars-bb:
	bb -f scripts/deploy.clj
