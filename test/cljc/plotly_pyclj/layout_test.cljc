(ns plotly-pyclj.layout-test
  (:require [plotly-pyclj.layout :as sut]
            #?(:clj [clojure.test :as t :refer (is)]
               :cljs [cljs.test :as t :include-macros true])))

(def m {:layout {:width 100 :legend {:position :h :title "text"} :margin {:t 0 :r 50}}})

(t/deftest gen-path-layout
  (t/testing "Macro behavior"
    (t/is (= (sut/layout {:layout {:width 100}})
             {:width 100}))
    (t/is (= (sut/layout {:layout {:width 100}} {:height 100})
             {:layout {:height 100}}))
    (t/is (= (sut/layout {:layout {:width 100}} assoc :height 100)
             {:layout {:width 100, :height 100}}))
    (t/is (= (sut/layout {:layout {:width 100}} (fn [m _] (assoc m :height (* 2 (:width m)))) nil)
             {:layout {:width 100, :height 200}})))

  (t/testing "margin"
    (t/is (= (sut/margin m) {:t 0 :r 50}))
    (t/is (= (sut/margin m {:l 50}) (assoc-in m [:layout :margin] {:l 50})))
    (t/is (= (sut/margin m assoc :l 10)
             (assoc-in m [:layout :margin] {:t 0 :r 50 :l 10})))
    (t/is (= (sut/margin m (fn [m _] (assoc m :l (:r m))) nil)
             (assoc-in m [:layout :margin] {:t 0 :r 50 :l 50})))))
