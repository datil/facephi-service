(ns facephi-service.database
  (:require [facephi-service.api-key :as api-key]
            [facephi-service.messages :as msg]
            [pandect.core :as pandect]
            [yesql.core :refer [defqueries]])
  (:import [org.apache.naming.java.javaURLContextFactory]))

(defn- lookup-datasource
  [name]
  (try
    (-> (new javax.naming.InitialContext)
        (.lookup name))
    (catch Exception e
      (throw (ex-info (:database-error msg/errors)
                      {:ex e
                       :cause (.getCause e)})))))

(defn db-spec
  [config]
  (assoc config :datasource (lookup-datasource (:datasource config))))

(defqueries "facephi_service/sql/api_key.sql")

(defqueries "facephi_service/sql/user_account.sql")

(defqueries "facephi_service/sql/user_log.sql")
