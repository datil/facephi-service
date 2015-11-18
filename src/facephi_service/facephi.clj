(ns facephi-service.facephi
  "Wraps the FacePhi Matcher SDK."
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
  "Sets the template security in registrations."
  []
  (doto (MatcherConfigurationManager.)
    (.setMatchingSecurityLevel MatchingSecurityLevel/MediumSecurityLevel)
    (.setTemplateReliability TemplateReliability/ExtremeTemplateReliability)
    (.setMatcherType MatcherType/Any)))

(defn authentication-matcher-configuration
  "Sets the template reliability and security level in authentications."
  []
  (doto (MatcherConfigurationManager.)
    (.setTemplateReliability TemplateReliability/ExtremeTemplateReliability)
    (.setMatchingSecurityLevel MatchingSecurityLevel/HighSecurityLevel)
    (.setMatcherType MatcherType/Any)))

(defn manual-retrain-matcher-configuration
  "Retrains a template in a manual reatraining operations, such as after a
  denied authentication attempt.."
  []
  (doto (MatcherConfigurationManager.)
    (.setTemplateReliability TemplateReliability/ExtremeTemplateReliability)
    (.setMatchingSecurityLevel MatchingSecurityLevel/HighSecurityLevel)
    (.setMatcherType MatcherType/Any)))

(defn auto-retrain-matcher-configuration
  "retrains a template in automatic retraining operations, such as after
  a successful authentication."
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
   (let [matcher (Matcher. (registration-matcher-configuration))
         user (.createUser matcher face-1)
         is-same-user? (.getIsPositiveMatch
                        (.authenticate matcher user face-2))]
     (if is-same-user?
       (.retrainUser matcher user face-2)
       nil))))

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
