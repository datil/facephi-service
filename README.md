# facephi-service

Provides biometric authentication as a service using FacePhi SDK.

## Pre-requisites

To run this service you will need:

- Running Oracle database.
- Java 8.
- Leiningen 2.x.
- [FacePhi SDK](http://facephi.com/) (this is commercial software so you will need a license).

## Getting Started

1. Start the application: `lein run-dev` \*
2. Go to [localhost:8080](http://localhost:8080/) to see the service documentation.

\* `lein run-dev` automatically detects code changes. Alternatively, you can run in production mode
with `lein run`.

## Configuration

To configure logging see config/logback.xml. By default, the app logs to stdout and logs/.
To learn more about configuring Logback, read its [documentation](http://logback.qos.ch/documentation.html).

## Deploy as a WAR

To deploy as a WAR execute the _deploy.sh_ script. You will need to install the following tools:

- Git
- Maven

## Links

* [Pedestal WAR deployment](https://github.com/pedestal/pedestal/blob/master/guides/documentation/service-war-deployment.md)
* [Pedestal examples](https://github.com/pedestal/samples)
