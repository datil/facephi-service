(ns facephi-service.facephi
  (:require [byte-streams :as b]
            [clojure.data.codec.base64 :as b64])
  (:import [com.facephi.sdk.matcher
            Matcher MatcherException TemplateReliability MatcherType
            MatchingSecurityLevel MatcherConfigurationManager]
           [com.facephi.sdk.licensing LicenseActivationException]))

(defn b64->byte_array
  "Transforms a b64 string to a byte array."
  [pattern]
  (b64/decode (b/to-byte-array pattern)))

(defn registration-matcher-configuration
  []
  (doto (MatcherConfigurationManager.)
    (.setTemplateReliability TemplateReliability/ExtremeTemplateReliability)
    (.setMatcherType MatcherType/Any)))

(defn authentication-matcher-configuration
  []
  (doto (MatcherConfigurationManager.)
    (.setTemplateReliability TemplateReliability/ExtremeTemplateReliability)
    (.setMatchingSecurityLevel MatchingSecurityLevel/HighSecurityLevel)
    (.setMatcherType MatcherType/Any)))

(defn manual-retrain-matcher-configuration
  []
  (doto (MatcherConfigurationManager.)
    (.setTemplateReliability TemplateReliability/ExtremeTemplateReliability)
    (.setMatchingSecurityLevel MatchingSecurityLevel/HighSecurityLevel)
    (.setMatcherType MatcherType/Any)))

(defn auto-retrain-matcher-configuration
  []
  (doto (MatcherConfigurationManager.)
    (.setTemplateReliability TemplateReliability/ExtremeTemplateReliability)
    (.setMatchingSecurityLevel MatchingSecurityLevel/VeryHighSecurityLevel)
    (.setMatcherType MatcherType/Any)))

(defn new-user
  "Returns a new retrained user formatted as byte[]. Does an automatic retrain
  with the second template if provided."
  ([face-1]
   (let [matcher (Matcher. (registration-matcher-configuration))
         user (.createUser matcher face-1)]
     user))
  ([face-1 face-2]
   (let [registration-matcher (Matcher. (registration-matcher-configuration))
         retrain-matcher (Matcher. (auto-retrain-matcher-configuration))
         user (.createUser registration-matcher face-1)
         retrained (.retrainUser retrain-matcher user face-2)]
     retrained)))

(defn manual-retrain
  "Manually retrains an user profile."
  [existing-face new-face]
  (let [matcher (Matcher. (manual-retrain-matcher-configuration))]
    (.retrainUser matcher existing-face new-face)))

(defn auto-retrain
  "Automatically retrains an user profile after registration or successful
  authentication."
  [existing-face new-face]
  (let [matcher (Matcher. (auto-retrain-matcher-configuration))]
    (.retrainUser matcher existing-face new-face)))

(defn authenticate
  "Returns true or false whether the faces was authenticated or not."
  [user-face provided-face]
  (let [matcher (Matcher. (authentication-matcher-configuration))]
    (.getIsPositiveMatch (.authenticate matcher user-face provided-face))))
