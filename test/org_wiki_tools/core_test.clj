(ns org-wiki-tools.core-test
  (:require [clojure.test :refer :all]
            [clj-time.local :as l]
            [clj-http.client :as client]
            [me.raynes.fs :as fs]
            [org-wiki-tools.core :refer :all]))

;; vars
(def normal-line "* [[https://example.com][Test]] :archive:")
(def no-link-line "* No link folk!")
(def wikipedia-link "https://en.wikipedia.org/wiki/Sweet_corn")
(def wikiwand-link "https://www.wikiwand.com/en/Sweet_corn")

;; helpers
(defn make-map [length]
  (into {} (map #(hash-map %1 %2) (range length) (range length))))

(testing "info enabled"
  (with-redefs [println str
                print-info true]
    (= (info "test" "test") "testtest")))

(testing "info disabled"
  (with-redefs [println str
                print-info false]
    (= (info "test" "test") nil)))

;; list-org-files
(testing "list-org-files"
  (with-redefs [fs/glob (fn [_] (vector "test.org" "bar.org" "foo.org"))]
    (= (list-org-files "./org") ["test.org" "bar.org" "foo.org"])))

;; get-link-info
(testing "get-link-info 200"
  (with-redefs [client/get (fn [_ _] (hash-map :status 200))]
    (= {:status 200 :url "https://example.com"} (get-link-info {:url "https://example.com"}))))
;; TODO get-link-info 404

;; process-link
(testing "process-link"
  (with-redefs [l/local-now #(str "1970-01-01")
                get-link-info (fn [_] (hash-map :url "https://example.com"))]
    (= (process-link {:url "https://example.com"}) {"https://example.com" {:url "https://example.com" :processed-at "1970-01-01"}})))

;; link->map
(testing "link->map Normal link"
  (is (= {"https://example.com" {:url "https://example.com" :status nil :processed-at nil :title "Test" :site "example.com"}} (link->map normal-line))))
(testing "link->map No link"
  (is (not (link->map no-link-line))))

;; find-link
(testing "find-link Normal link"
  (is (= ["[[https://example.com][Test]]" "https://example.com" "Test"] (find-link normal-line))))
(testing "find-link No link"
  (is (= nil (find-link no-link-line))))

;; wikiwand->wikipedia
(testing "wikiwand->wikipedia"
  (is (= (wikiwand->wikipedia wikiwand-link) wikipedia-link))
  (is (= "nothing" (wikiwand->wikipedia "nothing"))))

;; select-subset
(testing "select-subset"
  (is (= (count (select-subset (make-map (* 29 10)))) 10)))

;; ->archive
(testing "->archive"
  (is (= "* whatever :archived:" (->archive "* whatever :archive:")))
  (is (= "* no nothing" (->archive "* no nothing"))))
