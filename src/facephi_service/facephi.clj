(ns facephi-service.facephi
  (:require [byte-streams :as b]
            [clojure.data.codec.base64 :as b64])
  (:import [com.facephi.sdk.matcher
            Matcher MatcherException TemplateReliability
            MatchingSecurityLevel MatcherConfigurationManager]
           [com.facephi.sdk.licensing LicenseActivationException]))

(defn b64->byte_array
  "Transforms a b64 string to a byte array."
  [pattern]
  (b64/decode (b/to-byte-array pattern)))

(defn registration-matcher-configuration
  []
  (doto (MatcherConfigurationManager.)
    (.setTemplateReliability
     TemplateReliability/ExtremeTemplateReliability)))

(defn auth-matcher-configuration
  []
  (doto (MatcherConfigurationManager.)
    (.setMatchingSecurityLevel
     MatchingSecurityLevel/HighSecurityLevel)))

(defn new-user
  ([face-1]
   (let [matcher (Matcher. (registration-matcher-configuration))
         user (.createUser matcher face-1)]
     user))
  ([face-1 face-2]
   (let [matcher (Matcher. (registration-matcher-configuration))
         user (.createUser matcher face-1)
         retrained (.retrainUser matcher user face-2)]
     retrained)))

(defn retrain
  [existing-face new-face]
  (let [matcher (Matcher. (registration-matcher-configuration))]
    (.retrainUser matcher existing-face new-face)))

(defn authenticate
  [user-face provided-face]
  (let [matcher (Matcher. (auth-matcher-configuration))]
    (.getIsPositiveMatch (.authenticate matcher user-face provided-face))))