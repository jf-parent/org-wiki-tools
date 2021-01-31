(ns org-wiki-tools.createdb
  (:require [datomic.api :as d]))

(def schema
  [{:db/doc "Link url"
    :db/ident :link/url
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db.install/_attribute :db.part/db}

   {:db/doc "Link site"
    :db/ident :link/site
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}

   {:db/doc "Link status"
    :db/ident :link/status
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}

   {:db/doc "Link body"
    :db/ident :link/body
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}

   {:db/doc "Link title"
    :db/ident :link/title
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}])

(defn create-schema [db-uri]
  (-> db-uri
      d/connect
      (d/transact schema)))

(defn create-db []
  (let [db-uri "datomic:dev://localhost:4334/test-org?password=help"]
    (println "Deleting DB...")
    ;;(d/delete-database db-uri)
    (println "Creating DB...")
    (d/create-database db-uri)
    (System/exit 0)
    (println "Creating Schema...")
    (create-schema db-uri)
    (println "Done!")
    (System/exit 0)))

(defn -main [& args]
  (create-db))
