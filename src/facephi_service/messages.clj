(ns facephi-service.messages)

(def errors {:bad-api-key "El API key está incorrecto o no existe."
             :database-error "Error al conectar a la base de datos."
             :duplicated-user "El usuario ya existe."
             :user-not-found "El usuario no existe."})
