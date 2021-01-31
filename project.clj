(defproject org-wiki-tools "0.1.0"
  :description "Help maintaining order & beauty in your org-mode-wiki"
  :license {:name "BSD"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [me.raynes/fs "1.4.6"]
                 [clj-http "3.10.3"]
                 [org.clojure/core.async "1.3.610"]
                 [clj-time "0.15.2"]
                 [com.climate/claypoole "1.1.4"]]
  :main ^:skip-aot org-wiki-tools.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
