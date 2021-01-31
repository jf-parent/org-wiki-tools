(ns org-wiki-tools.core-test
  (:require [clojure.test :refer :all]
            [org-wiki-tools.core :refer :all]))

(def normal-line "* [[https://example.com][Test]] :archive:")
(def no-link-line "* No link folk!")
(def wikipedia-link "https://en.wikipedia.org/wiki/Sweet_corn")
(def wikiwand-link "https://www.wikiwand.com/en/Sweet_corn")

(defn make-map [length]
  (into {} (map #(hash-map %1 %2) (range length) (range length))))

(testing "link->map Normal link"
  (is (= {"https://example.com" {:url "https://example.com" :status nil :processed-at nil :title "Test" :site "example.com"}} (link->map normal-line))))
(testing "link->map No link"
  (is (not (link->map no-link-line))))

(testing "find-link Normal link"
  (is (= ["[[https://example.com][Test]]" "https://example.com" "Test"] (find-link normal-line))))
(testing "find-link No link"
  (is (= nil (find-link no-link-line))))

(testing "wikiwand->wikipedia"
  (is (= (wikiwand->wikipedia wikiwand-link) wikipedia-link))
  (is (= "nothing" (wikiwand->wikipedia "nothing"))))

(testing "select-subset"
  (is (= (count (select-subset (make-map (* 29 10)))) 10)))

(testing "->archive"
  (is (= "* whatever :archived:" (->archive "* whatever :archive:")))
  (is (= "* no nothing" (->archive "* no nothing"))))
