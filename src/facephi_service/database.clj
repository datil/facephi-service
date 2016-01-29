(ns facephi-service.database
  "Database API"
  (:require [byte-streams :as b]
            [facephi-service.api-key :as api-key]
            [facephi-service.messages :as msg]
            [clojure.java.jdbc :as jdbc]
            [pandect.core :as pandect]
            [yesql.core :refer [defqueries]])
  (:import [org.apache.naming.java.javaURLContextFactory]))

(defn- lookup-datasource
  "Looks up a JNI datasource to connect to the database."
  [name]
  (try
    (-> (new javax.naming.InitialContext)
        (.lookup name))
    (catch Exception e
      (throw (ex-info (:database-error msg/errors)
                      {:ex e
                       :cause (.getCause e)})))))

(defn db-spec
  "Generates a database specifification map that you can pass to
   functions that query the database."
  [config]
  (assoc config :datasource (lookup-datasource (:datasource config))))

(defqueries "facephi_service/sql/api_key.sql")

(defqueries "facephi_service/sql/user_account.sql")

(defqueries "facephi_service/sql/user_log.sql")

(defqueries "facephi_service/sql/user_block.sql")

(defn get-user-by-username-tx
  "Wraps a database query so we can filter the template bytes[] accordingly."
  [db-spec username]
  (jdbc/with-db-transaction [connection db-spec]
    (when-let [user (first (get-user connection username))]
      (update-in user
                 [:face]
                 (fn [v]
                   (b/to-byte-array (.getBinaryStream v)))))))

(defn get-user-by-identification-tx
  "Wraps a database query so we can filter the template bytes[] accordingly."
  [db-spec identification]
  (jdbc/with-db-transaction [connection db-spec]
    (when-let [user (first (get-user-by-identification connection identification))]
      (update-in user
                 [:face]
                 (fn [v]
                   (b/to-byte-array (.getBinaryStream v)))))))

(defn reset-attempts-tx
  "Wraps reset attempts in a transaction to make it atomic."
  [db-spec username]
  (jdbc/with-db-transaction [connection db-spec]
    (reset-attempts connection username)))
