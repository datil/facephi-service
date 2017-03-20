;; (ns facephi-service.test-datasource
;;   (:import [org.apache.naming.java.javaURLContextFactory]))

;; (System/setProperty javax.naming.Context/INITIAL_CONTEXT_FACTORY
;;                     "org.apache.naming.java.javaURLContextFactory")

;; (System/setProperty javax.naming.Context/URL_PKG_PREFIXES
;;                     "org.apache.naming")

;; (defn setup-test-datasource
;;   [datasource-config]
;;   (let [test-ctx (new javax.naming.InitialContext)
;;         test-datasource (new oracle.jdbc.pool.OracleConnectionPoolDataSource)]
;;     (do
;;       (.createSubcontext test-ctx (:context datasource-config))
;;       (doto test-datasource
;;         (.setURL (:url datasource-config))
;;         (.setUser (:user datasource-config))
;;         (.setPassword (:password datasource-config)))
;;       (.bind test-ctx (:name datasource-config) test-datasource)
;;       datasource-config)))
