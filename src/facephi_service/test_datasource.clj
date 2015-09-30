(ns facephi-service.test-datasource
  (:import [org.apache.naming.java.javaURLContextFactory]))

(System/setProperty javax.naming.Context/INITIAL_CONTEXT_FACTORY
                    "org.apache.naming.java.javaURLContextFactory")

(System/setProperty javax.naming.Context/URL_PKG_PREFIXES
                    "org.apache.naming")

(def test-ctx (new javax.naming.InitialContext))

(def test-oracle-datasource (new oracle.jdbc.pool.OracleConnectionPoolDataSource))

(def datasource-config (:datasource (clojure.java.io/resource "config.edn")))

(defn setup-test-datasource
  []
  (do
    (.createSubcontext test-ctx (:context datasource-config))
    (doto test-oracle-datasource
      (.setURL (:url datasource-config))
      (.setUser (:user datasource-config))
      (.setPassword (:password datasource-config)))
    (.bind test-ctx (:name datasource-config) test-oracle-datasource)
    datasource-config))
