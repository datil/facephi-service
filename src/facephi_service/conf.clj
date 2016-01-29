(ns facephi-service.conf
  "Service configuration. These variables refer to config.edn configuration file.
  This file must be included in the /resources folder."
  (:require [clojure.edn :as edn]))

(def database (:database
               (edn/read-string
                (slurp (clojure.java.io/resource "config.edn")))))

(def datasource (:datasource
                 (edn/read-string
                  (slurp (clojure.java.io/resource "config.edn")))))

(def allowed-login-attempts 3)
