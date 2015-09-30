(ns facephi-service.facephi)

(defn new-user
  [face-1 & face-2]
  (.getBytes face-1))

(defn retrain
  [existing-face new-face]
  new-face)

(defn authenticate
  [user-face provided-face]
  user-face)
