(ns org-wiki-tools.core
  (:require [clojure.edn :as edn]
            [clj-time.local :as l]
            [clojure.java.io :as io]
            [clj-http.client :as client]
            [com.climate.claypoole :as cp]
            [me.raynes.fs :as fs]))

(def link-db-path "./link.edn")
(def link-db {})

;; Load config and link-db
(def config (-> "./config.edn" slurp clojure.edn/read-string))
(try
  (def link-db (-> "./link.edn" slurp clojure.edn/read-string))
  (catch Exception ex
    (def link-db {})
    (println ex)))

;; Regex
(def org-mode-link-re #"\[?\[?(http[^]]*)\]?\[?([^]]*)\]?\]?")
(def site-re #"https?:\/\/([^/]*)/?")

;; Globals
(def print-info (:print-info config true))
(def link-processing-limit (:link-processing-limit config 29))
(def wiki-path (:wiki-path config))
(def link-to-archive (atom []))
(def new-link-db (atom {}))

;; REPL
;; (count link-db)
;; (count (filter #(nil? (:processed-at %)) (vals link-db)))
;; (println (filter #(= 404 (:status %)) (vals link-db)))

(defn info [& msg]
  (when print-info
    (apply println msg)))

(defn list-org-files [dir]
  (fs/glob (str dir "/*.org")))

(defn get-link-info [link]
  (merge link
         (try
           (select-keys (client/get (:url link) {:socket-timeout 10000 :connection-timeout 10000}) [:status])
           (catch Exception ex
             (info "caught exception: " (.getMessage ex))
             {:status (get-in (Throwable->map ex) [:data :status] 404) :error (.getMessage ex)}))))

(defn link->map [line]
  (when-let [[_ url title] (re-find org-mode-link-re line)]
    (let [[_ site] (re-find site-re url)]
      (info (str "Link: " url))
      {(str url) {:url url :status nil :processed-at nil :title title :site site}})))

(defn process-link [link]
  (info "Processing Link: " link)
  {(:url link) (merge (get-link-info link) {:processed-at (str (l/local-now))})})

(defn map-file [fn file]
  (info "Extracting link from file: " file)
  (with-open [rdr (io/reader file)]
    (doall (map fn (line-seq rdr)))))

(defn get-links [file]
  (info "Extracting link from file: " file)
  (with-open [rdr (io/reader file)]
    (into {} (map link->map (line-seq rdr)))))

(defn find-link [text]
  (re-find org-mode-link-re text))

(defn wikiwand->wikipedia [link]
  (clojure.string/replace link #"https:\/\/www\.wikiwand\.com\/(.{2})\/(.*)" (fn [[_ l k]] (format "https://%s.wikipedia.org/wiki/%s" l k))))

(defn tag-dead-link [line]
  (if-let [[_ url _] (find-link line)]
    (if (and (not= 200 (:status (link-db url) 200)) (not (re-find #":dead-link:" line)))
      (str line "\t\t\t" ":dead-link:")
      line)
    line))

(defn ->archive [line]
  (if (re-find #":archive:" line)
    (do
      (swap! link-to-archive conj (second (find-link line)))
      (clojure.string/replace line #":archive:" ":archived:"))
    line))

(defn select-subset [links]
  (let [nb (int (/ (count links) link-processing-limit))]
    (take nb (filter #(nil? (:processed-at %)) links))))

(defn extract-links [dir]
  (let [files (list-org-files dir)]
    (reset! new-link-db (into {} (map get-links files)))))

(defn process-wiki [dir]
  (let [files (list-org-files dir)]
    (doall
     (map
      #(->> %
            (map-file wikiwand->wikipedia)
            (map ->archive)
            (map tag-dead-link)
            (clojure.string/join "\n")
            (spit %))
      files)))
  (spit "to_archive.txt" (str (clojure.string/join "\n" @link-to-archive) "\n")))

(defn process-links []
  (->> link-db
       vals
       select-subset
       doall
       (cp/pmap 100 process-link)
       (into {})
       (merge link-db)
       prn-str
       (spit link-db-path)))

;; TODO Autoheal link (redirect)
;; TODO Crontab ./extract-link.sh monthly
;; TODO Crontab ./process-link.sh daily
;; TODO Crontab ./process-wiki.sh daily

(defn -main [& args]
  (condp = (first args)
    "show-link" (info link-db)
    "process-links" (do
                     (info "Processing link...")
                     (process-links))
    "process-wiki" (do
                     (info "Processing wiki...")
                     (process-wiki wiki-path))
    "extract-links" (do
                (info "Extracting...")
                (extract-links wiki-path)
                (spit link-db-path (prn-str @new-link-db))))
  (println "Done!")
  (System/exit 0))
