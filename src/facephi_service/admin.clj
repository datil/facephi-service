(ns facephi-service.admin
  (:require [facephi-service.api-key :as ak]
            [facephi-service.database :as db]
            [facephi-service.test-datasource :as td]))

(defn create-new-api-key
  []
  (let [db-spec db/db-spec
        api-key "76448c73c5754408b2d04aa3223c25ae"
        hashed-api-key (ak/hashed-api-key api-key)]
    (db/save-api-key! db-spec hashed-api-key 1)
    {:api-key api-key
     :hashed-api-key hashed-api-key}))
