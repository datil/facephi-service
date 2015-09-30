(ns facephi-service.service
  (:require [facephi-service.api-key :as ak]
            [facephi-service.database :as db]
            [facephi-service.messages :as msg]
            [io.pedestal.http :as bootstrap]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [io.pedestal.interceptor.helpers :as interceptor]
            [pedestal.swagger.error :as sw.error]
            [pedestal.swagger.core :as swagger]
            [pedestal.swagger.doc :as sw.doc]
            [ring.util.response :as ring-resp]
            [schema.core :as s]))

(def opt s/optional-key)
(def req s/required-key)

;;;; Responses

(defn not-authorized
  [message]
  (-> (ring-resp/response {:message message})
      (ring-resp/status 401)))


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

(swagger/defhandler new-user
  {:summary "Creates new user account"
   :responses {201 {:description "User created successfuly."}}}
  [request]
  {:username "test-user"})

;;;; Interceptors

(def assoc-db-spec
  (interceptor/before
   ::assoc-db-specs
   (fn [context]
     (let [db db/db-spec]
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
                  :description "User account management."}]}}
  [[["/"
     ^:interceptors [bootstrap/json-body
                     sw.error/handler
                     (swagger/body-params)
                     (swagger/coerce-request)
                     (swagger/validate-response)
                     assoc-db-spec]
     ["/about" {:get [^:interceptors [(annotate {:tags ["monitoring"]})
                                      authenticate-api-key]
                      :about home-page]}]
     ["/users" {:post [^:interceptors [(annotate {:tags ["users"]})
                                       authenticate-api-key]
                       :users home-page]}]
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
              ::bootstrap/type :jetty
              ;;::bootstrap/host "localhost"
              ::bootstrap/port 8080})
