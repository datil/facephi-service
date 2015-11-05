(defproject facephi-service "0.1.0"
  :description "Biometric authentication microservice."
  :url "https://github.com/datil/facephi-service"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [io.pedestal/pedestal.service "0.4.0"]

                 ;; Remove this line and uncomment one of the next lines to
                 ;; use Immutant or Tomcat instead of Jetty:
                 [io.pedestal/pedestal.jetty "0.4.0"]
                 ;; [io.pedestal/pedestal.immutant "0.4.0"]
                 ;; [io.pedestal/pedestal.tomcat "0.4.0"]

                 ;; Logging
                 [ch.qos.logback/logback-classic "1.1.2" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.7"]
                 [org.slf4j/jcl-over-slf4j "1.7.7"]
                 [org.slf4j/log4j-over-slf4j "1.7.7"]

                 ;; Service
                 [byte-streams "0.2.0"]
                 [commons-discovery/commons-discovery "0.2"]
                 [com.oracle/ojdbc "12.10.10"]
                 [org.clojure/data.codec "0.1.0"]
                 [environ "1.0.1"]
                 [fphi-matcher-java/fphi-matcher-java "5.3.0"]
                 [fphi-licensing-java/fphi-licensing-java "5.3.0"]
                 [frankiesardo/pedestal-swagger "0.4.4"
                  :exclusions [org.clojure/clojure
                               com.fasterxml.jackson.core/jackson-core
                               cheshire
                               clj-time
                               joda-time]]
                 [org.apache.tomcat/tomcat-catalina "8.0.26"]
                 [pandect "0.5.4"
                  :exclusions [org.clojure/clojure
                               clj-tuple
                               riddley]]
                 [prismatic/schema "1.0.1"]
                 [yesql "0.4.2"]]
  :plugins [[lein-codox "0.9.0"]]
  ;:pedantic? :abort
  :min-lein-version "2.0.0"
  :resource-paths ["config", "resources"]
  :profiles {:dev {:aliases {"run-dev" ["trampoline" "run" "-m" "facephi-service.server/run-dev"]}
                   :dependencies [[io.pedestal/pedestal.service-tools "0.4.0"]
                                  [org.clojure/core.async "0.1.346.0-17112a-alpha"]]}
             :uberjar {:aot [facephi-service.server]}}
  :repl-options {:host "0.0.0.0"
                 :port 4001}
  :autodoc { :name "facephi-service", :page-title "FacePhi Service API Reference"}
  :main ^{:skip-aot false} facephi-service.server)
