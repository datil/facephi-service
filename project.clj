(defproject facephi-service "0.1.0"
  :description "Biometric authentication service."
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
                 [commons-discovery/commons-discovery "0.2"]
                 [com.oracle/ojdbc "12.10.10"]
                 [environ "1.0.1"]
                 [frankiesardo/pedestal-swagger "0.4.4"
                  :exclusions [org.clojure/clojure
                               com.fasterxml.jackson.core/jackson-core
                               cheshire
                               clj-time
                               joda-time]]
                 [org.apache.tomcat/tomcat-catalina "8.0.26"]
                 [pandect "0.5.4"
                  :exclusions [org.clojure/clojure]]
                 [prismatic/schema "1.0.1"]
                 [yesql "0.4.2"]]
  :pedantic? :abort
  :min-lein-version "2.0.0"
  :resource-paths ["config", "resources"]
  :profiles {:dev {:aliases {"run-dev" ["trampoline" "run" "-m" "facephi-service.server/run-dev"]}
                   :dependencies [[io.pedestal/pedestal.service-tools "0.4.0"]]}
             :uberjar {:aot [facephi-service.server]}}

  :main ^{:skip-aot true} facephi-service.server)
