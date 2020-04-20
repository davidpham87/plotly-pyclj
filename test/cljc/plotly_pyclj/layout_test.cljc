(ns plotly-pyclj.layout-test
  (:require [plotly-pyclj.layout :as sut]
            #?(:clj [clojure.test :as t :refer (is)]
               :cljs [cljs.test :as t :include-macros true])))

(def m {:layout {:width 100 :legend {:position :h :title "text"} :margin {:t 0 :r 50}}})

(t/deftest gen-path-layout
  (t/testing "Layout"
    (t/is (= (sut/width {:layout {:width 100}} 500) {:layout {:width 500}}))
    (t/is (= (sut/width {:layout {:width 100}}) 100))
    (t/is (= (sut/layout {:layout {:width 100}} assoc :height 100)
           {:layout {:width 100, :height 100}}))
    (t/is (= (sut/layout {:layout {:width 100}} (fn [m _] (assoc m :height (* 2 (:width m)))) nil)
           {:layout {:width 100, :height 200}}))))
