(ns facephi-service.block
  (:require [facephi-service.database :as db]))

(defn register-login-attempt
  [db-spec username]
  (let [has-attempts (first (db/get-attempts db-spec username))]
    (if has-attempts
      (db/increment-attempts! db-spec username)
      (do
        (db/create-attempt-registry! db-spec username)
        (db/increment-attempts! db-spec username)))))
