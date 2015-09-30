(ns facephi-service.api-key
  "Public API for API key data."
  (:require [pandect.core :as pandect]))

(defn new-api-key
  "Generates a new random API key based on an UUID."
  []
  (clojure.string/replace (.toString (java.util.UUID/randomUUID)) "-" ""))

(defn hashed-api-key
  "Hashes the API key so it can be stored in a database."
  [api-key]
  (pandect/sha512 api-key))
