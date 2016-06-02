(ns facephi-service.service
  "Pedestal service that implements API functions as HTTP resources."
  (:require [facephi-service.api-key :as ak]
            [facephi-service.block :as block]
            [facephi-service.conf :as conf]
            [facephi-service.database :as db]
            [facephi-service.facephi :as fp]
            [facephi-service.messages :as msg]
            [io.pedestal.http :as bootstrap]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [io.pedestal.interceptor.error :as error-int]
            [io.pedestal.interceptor.helpers :as interceptor]
            [pedestal.swagger.error :as sw.error]
            [pedestal.swagger.core :as swagger]
            [pedestal.swagger.doc :as sw.doc]
            [ring.util.response :as ring-resp]
            [schema.core :as s])
  (:import [com.facephi.sdk.matcher MatcherException]
           [com.facephi.sdk.licensing LicenseActivationException]))

(def opt s/optional-key)
(def req s/required-key)

;;;; Schemas

(s/defschema NewUserRequest
  {(req :username) s/Str
   (req :template-1) s/Str
   (opt :template-2) s/Str
   (req :identification) s/Str})

(s/defschema NewUserResponse
  {(req :username) s/Str})

(s/defschema UserDetailResponse
  {(req :username) s/Str
   (req :created) s/Inst
   (req :last_updated) s/Inst
   (req :is_active) s/Num
   (req :login_attempts) s/Num
   (req :identification) s/Str})

(s/defschema IdentificationAuthenticationRequest
  {(req :identification) s/Str
   (req :template) s/Str})

(s/defschema UsernameAuthenticationRequest
  {(req :username) s/Str
   (req :template) s/Str})

(s/defschema UsernameAuthenticationResponse
  {(req :result) s/Bool
   (req :username) s/Str
   (opt :time) s/Str})

(s/defschema UsernameRetrainingRequest
  {(req :username) s/Str
   (req :template) s/Str})

(s/defschema RetrainingResponse
  {(req :username) s/Str})

(s/defschema UnlockingRequest
  {(req :username) s/Str})

(s/defschema UnlockingResponse
  {(req :username) s/Str})

(s/defschema ErrorResponse
  {(req :message) s/Str})

;;;; Responses

(defn not-authorized
  [message]
  (-> (ring-resp/response message)
      (ring-resp/status 401)))

(defn blocked
  [message]
  (-> (ring-resp/response message)
      (ring-resp/status 405)))

(defn bad-request
  [message]
  (-> (ring-resp/response message)
      (ring-resp/status 400)))

(defn created
  [message]
  (-> (ring-resp/response message)
      (ring-resp/status 201)))

(defn ok
  [message]
  (ring-resp/response message))

(defn not-found
  [message]
  (-> (ring-resp/response message)
      (ring-resp/status 404)))

;;;; Handlers

(swagger/defhandler home-page
  {:summary "Returns key service metrics."
   :responses {200 {:description "System is running."}}}
  [request]
  (ring-resp/response {:name "FacePhi Service"
                       :version "0.1.0"
                       :database_status "Ok"
                       :facephi_sdk_status "Ok"
                       :clojure_version (clojure-version)}))

(swagger/defhandler user-registration
  {:summary "Creates a new user"
   :parameters {:body NewUserRequest}
   :responses {201 {:description "User created successfuly."
                    :schema NewUserResponse}
               400 {:description "User already exists."}}}
  [request]
  (let [db-spec (:db-spec request)
        params (:body-params request)
        username (clojure.string/lower-case (:username params))
        template-1 (:template-1 params)
        template-2 (:template-2 params)
        face (if template-2
               (fp/new-user (fp/b64->byte_array template-1)
                            (fp/b64->byte_array template-2))
               (fp/new-user (fp/b64->byte_array template-1)))
        identification (:identification params)
        existing-user (first (db/get-user db-spec username))]
    (if face
      (if existing-user
        (bad-request (:duplicated-user msg/errors))
        (do
          (db/save-user! db-spec username 1 face identification)
          (created {:username username})))
      (bad-request (:not-authenticated msg/errors)))))

(swagger/defhandler user-detail
  {:summary "Returns user details"
   :parameters {:path {(req :username) s/Str}}
   :responses {200 {:description "User found."
                    :schema UserDetailResponse}
               404 {:description "User not found."
                    :schema ErrorResponse}}}
  [request]
  (let [db-spec (:db-spec request)
        username (:username (:path-params request))
        user (first (db/get-user db-spec username))
        attempts (or (:login_attempts (first (db/get-attempts db-spec username))) 0)]
    (if user
      (ok (-> user
              (dissoc :id)
              (dissoc :face)
              (dissoc :is_locked)
              (assoc :login_attempts attempts)))
      (not-found {:message (:user-not-found msg/errors)}))))

(def block-user-by-login-attempts
  (interceptor/before
    :block-user-by-login-attempts
    (fn [context]
      (let [request (:request context)
            db-spec (:db-spec request)
            username (:username (:user request))
            login-attempts (:login_attempts
                            (first (db/get-attempts db-spec username)))]
        (if login-attempts
          (if (< login-attempts conf/allowed-login-attempts)
            context
            (assoc-in context [:response] (blocked
                                           {:message (:user-blocked msg/errors)})))
          context)))))

(swagger/defbefore load-user-from-path
  {:summary "Ensures the identity exists before proceeding to authenticate"
   :responses {404 {:description "User not found."
                    :schema ErrorResponse}}}
  [context]
  (let [db-spec (:db-spec (:request context))
        path (:path-params (:request context))
        by-id (db/get-user-by-identification-tx db-spec (:username path))
        by-username (db/get-user-by-username-tx db-spec (:username path))]
    (if by-id
      (assoc-in context [:request :user] (update-in by-id [:username]
                                                    clojure.string/lower-case))
      (if by-username
        (assoc-in context [:request :user] (update-in by-username [:username]
                                                      clojure.string/lower-case))
        (assoc-in context [:response] (not-found
                                         {:message (:user-not-found msg/errors)}))))))

(swagger/defbefore load-user-by-username
  {:summary "Ensures the username exists before proceeding to authenticate"
   :responses {404 {:description "User not found."
                    :schema ErrorResponse}}}
  [context]
  (let [db-spec (:db-spec (:request context))
        params (:body-params (:request context))
        user (db/get-user-by-username-tx db-spec (:username params))]
    (if user
      (assoc-in context [:request :user] (update-in user [:username]
                                                    clojure.string/lower-case))
      (assoc-in context [:response] (not-found
                                     {:message (:user-not-found msg/errors)})))))

(swagger/defbefore load-user-by-identification
  {:summary "Ensures the identity exists before proceeding to authenticate"
   :responses {404 {:description "User not found."
                    :schema ErrorResponse}}}
  [context]
  (let [db-spec (:db-spec (:request context))
        params (:body-params (:request context))
        user (db/get-user-by-identification-tx db-spec (:identification params))]
    (if user
      (assoc-in context [:request :user] (update-in user [:username]
                                                    clojure.string/lower-case))
      (assoc-in context [:response] (not-found
                                     {:message (:user-not-found msg/errors)})))))

(swagger/defhandler authenticate
  {:summary "Authenticates a username and face template against the user's face
            record."
   :responses {200 {:description "User authentication completed."
                    :schema UsernameAuthenticationResponse}
               401 {:description "User not authenticated"
                    :schema ErrorResponse}}}
  [{:keys [db-spec body-params user] :as request}]
  (let [user (:user request)
        request-face (fp/b64->byte_array (:template body-params))
        authenticated? (fp/authenticate (:face user) request-face)]
    (if authenticated?
      (do (-> (db/save-retrained-user!
               db-spec (fp/auto-retrain (:face user) request-face) (:username user)))
          (ok {:result authenticated?
               :username (:username user)}))
      (do (block/register-login-attempt db-spec (:username user))
          (not-authorized {:message (:not-authenticated msg/errors)})))))

(swagger/defhandler user-retraining
  {:summary "Retrains a user face profile."
   :responses {200 {:description "User retrained successfully."}
               404 {:description "User not found."
                    :schema ErrorResponse}}}
  [request]
  (let [db-spec (:db-spec request)
        user (:user request)
        request-face (fp/b64->byte_array (:template (:body-params request)))
        authenticated? (fp/retrain-authenticate (:face user) request-face)]
    (if authenticated?
      (do
        (db/save-retrained-user! db-spec
                                 (fp/manual-retrain
                                  (:face user)
                                  request-face)
                                 (:username user))
        (ok {:username (:username user)}))
      (not-authorized {:message (:not-authenticated msg/errors)}))))

(swagger/defhandler user-unlocking
  {:summary "Unlocks an user account."
   :parameters {:path {:username s/Str}}
   :responses {200 {:description "User unlocked successfully."
                    :schema UnlockingResponse}
               404 {:description "User not found."
                    :schema ErrorResponse}}}
  [request]
  (let [;db-spec (:db-spec request)
        db-spec (db/db-spec conf/database)
        user (:user request)]
    (do
      (db/reset-attempts! db-spec (:username user))
      (ok {:username (:username user)}))))

(swagger/defhandler user-deletion
  {:summary "Deletes an user account."
   :parameters {:path {:username s/Str}}}
  [request]
  (let [db-spec (:db-spec request)
        user (:user request)]
    (do
      (db/delete-user! db-spec (:identification user))
      (db/delete-block-registry! db-spec (:username user))
      (ok {}))))

;;;; Interceptors

(def service-error-handler
  (error-int/error-dispatch
   [ctx ex]
   [{:exception-type :com.facephi.sdk.matcher.MatcherException}]
   (do
     (println (str ex))
     (assoc ctx
            :response
            {:status 400 :body {:message (:data-processing msg/errors)}}))
   [{:exception-type :com.facephi.sdk.licensing.LicenseActivationException}]
   (do
     (println (str ex))
     (assoc ctx
            :response
            {:status 500 :body {:message (:licensing msg/errors)}}))
   [{:exception-type :java.lang.ArrayIndexOutOfBoundsException}]
   (do
     (println (str ex))
     (assoc ctx
            :response
            {:status 400 :body {:message (:data-processing msg/errors)}}))
   :else
   ;;(assoc ctx :io.pedestal.impl.interceptor/error ex)
   (do
     (println (str ex))
     (assoc ctx
            :response
            {:status 500 :body {:message (:unhandled msg/errors)}}))))

(def assoc-db-spec
  (interceptor/before
   ::assoc-db-specs
   (fn [context]
     (let [db (db/db-spec conf/database)]
       (assoc-in context [:request :db-spec] db)))))

(swagger/defbefore authenticate-api-key
  {:summary "All requests require a API key."
   :parameters {:header {(req "x-api-key") s/Str}}
   :responses {401 {:description "API key not authorized."}}}
  [context]
  (let [db-spec (:db-spec (:request context))
        request-api-key (get (:headers (:request context)) "x-api-key")
        hashed-api-key (ak/hashed-api-key request-api-key)
        stored-api-key (first (db/get-api-key db-spec hashed-api-key))]
    (if stored-api-key
      context
      (assoc context :response (not-authorized (:bad-api-key msg/errors))))))

(def log-authentication
  (interceptor/after
   ::log-authentication
   (fn [{:keys [request response] :as context}]
     (case (:status response)
       401 (do (db/save-user-log! (:db-spec request)
                                  (:username (:user request))
                                  (:identification (:user request))
                                  "authentication"
                                  "not_authenticated")
               context)
       200 (do (db/save-user-log! (:db-spec request)
                                  (:username (:user request))
                                  (:identification (:user request))
                                  "authentication"
                                  "authenticated")
               context)
       context))))

(def log-registration
  (interceptor/after
   ::log-registration
   (fn [{:keys [request response] :as context}]
     (if (= (:status response) 201)
       (do (db/save-user-log! (:db-spec request)
                              (:username (:body response))
                              (:identification (:body-params request))
                              "registration"
                              "registrated")
           context)
       context))))

(def log-unlocking
  (interceptor/after
   ::log-unlocking
   (fn [{:keys [request response] :as context}]
     (db/save-user-log! (:db-spec request)
                        (:username (:user request))
                        (:identification (:user request))
                        "unlocking"
                        "unlocked")
     context)))

(def log-retraining
  (interceptor/after
   ::log-retraining
   (fn [{:keys [request response] :as context}]
     (db/save-user-log! (:db-spec request)
                        (:username (:user request))
                        (:identification (:user request))
                        "retraining"
                        "retrained")
     context)))

;;;; Routes

(defn annotate
  "Adds metatata m to a swagger route"
  [m]
  (sw.doc/annotate m (interceptor/before ::annotate identity)))

(swagger/defroutes routes
  {:info {:title "FacePhi Service"
          :description "Provides biometric authentication as a service using FacePhi SDK."
          :version "0.1.0"
          :tags [{:name "monitoring"
                  :description "Key service monitoring metrics."}
                 {:name "users"
                  :description "User account management."}]}
   :basePath "/facephi-service"}
  [[["/" ^:interceptors [bootstrap/json-body
                         service-error-handler
                         sw.error/handler
                         (swagger/body-params)
                         (swagger/coerce-request)
                         (swagger/validate-response)
                         assoc-db-spec]
     ["/about" {:get [^:interceptors [(annotate {:tags ["monitoring"]})
                                      authenticate-api-key]
                      :about home-page]}]
     ["/users" ^:interceptors [(annotate {:tags ["users"]})
                               authenticate-api-key]
      ["/registration" {:post [^:interceptors [log-registration]
                               :user-registration
                               user-registration]}]
      ["/authentication/by-identification" {:post [^:interceptors
                                                   [load-user-by-identification
                                                    block-user-by-login-attempts
                                                    log-authentication]
                                                   :identification-authentication
                                                   authenticate]}]
      ["/authentication/by-username" {:post [^:interceptors
                                             [load-user-by-username
                                              block-user-by-login-attempts
                                              log-authentication]
                                             :username-authentication
                                             authenticate]}]
      ["/retraining/by-username" {:post [^:interceptors
                                         [load-user-by-username
                                          log-retraining]
                                         :user-retraining
                                         user-retraining]}]
      ["/retraining/by-identification" {:post [^:interceptors
                                               [load-user-by-identification
                                                log-retraining]
                                               :identification-retraining
                                               user-retraining]}]
      ["/:username" ^:interceptors [load-user-from-path]
       {:get [:user-detail user-detail]}
       ["/deletion" {:post [:delete-user user-deletion]}]
       ["/unblock" {:post [^:interceptors [load-user-from-path
                                           log-unlocking]
                           :user-unlocking
                           user-unlocking]}]]]
     ["/swagger.json" {:get [(swagger/swagger-json)]}]
     ["/*resource" {:get [(swagger/swagger-ui)]}]]]])

;; Consumed by facephi-service.server/create-server
;; See bootstrap/default-interceptors for additional options you can configure
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; ::bootstrap/interceptors []
              ::bootstrap/routes routes
              ::bootstrap/router :linear-search

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::bootstrap/allowed-origins ["scheme://host:port"]

              ;; Root for resource interceptor that is available by default.
              ::bootstrap/resource-path "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ;; ::bootstrap/type :jetty
              ;;::bootstrap/host "localhost"
              ::bootstrap/port 8080})
