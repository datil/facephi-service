(ns facephi-service.conf
  (:require [clojure.edn :as edn]))

(def database (:database
               (edn/read-string
                (slurp (clojure.java.io/resource "config.edn")))))

(def datasource (:datasource
                 (edn/read-string
                  (slurp (clojure.java.io/resource "config.edn")))))
