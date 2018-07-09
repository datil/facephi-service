# facephi-service

Servicio de autenticación facial usando FacePhi SDK.

## Requerimientos

Para ejecutar este servicio necesitas:

- Base de datos Oracle
- Java 8
- Leiningen 2.x
- [FacePhi SDK](http://facephi.com/)

## Cómo empezar

1. Inicia la aplicación: `lein run-dev` \*
2. Visita [localhost:8080](http://localhost:8080/) para ver la documentación del servicio.

\* `lein run-dev` detecta cambios de código fuente. También lo puedes ejecutar en producción con `lein run`.

## Desplegar usando un WAR

El proyecto incluye una rutina de generación de un WAR para WebLogic 12 o superior. Para ejecutarlo requieres:

- [Leiningen 2.x](https://github.com/technomancy/leiningen)
- [Git](https://git-scm.com/downloads)
- [Maven](https://maven.apache.org/download.cgi)

Sigue estos pasos para generar el WAR:

1. Crea una carpeta llamada `libs` dentro de la carpeta del proyecto. En esta copia los siguientes JAR:

- fphi-licensing-java-5.5.1.jar (Descárgalo del sitio web de FacePhi)
- fphi-matcher-java-5.5.1.jar (Descárgalo del sitio web de FacePhi)
- ojdbc7.jar (Descárgalo del [sitio web de Oracle](http://www.oracle.com/technetwork/database/features/jdbc/default-2280470.html))

2. Instala los JAR en el repositorio Maven local de tu estación de trabajo:

```shell
$ lein localrepo install lib/fphi-licensing-java-5.5.1.jar fphi-licensing-java/fphi-matcher-java 5.5.1

$ lein localrepo install lib/fphi-matcher-java-5.5.1.jar fphi-matcher-java/fphi-matcher-java 5.5.1

$ lein localrepo install lib/ojdbc7.jar com.oracle/ojdbc "12.10.10"
```

3. Genera el JAR ejecutando la rutina _deploy.sh_:

```shell
$ ./deploy.sh
```

El JAR se generará en la carpeta `target`.

## Links

* [Pedestal WAR deployment](https://github.com/pedestal/pedestal/blob/master/guides/documentation/service-war-deployment.md)
* [Pedestal examples](https://github.com/pedestal/samples)
