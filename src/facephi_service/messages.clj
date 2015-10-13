(ns facephi-service.messages)

(def errors {:bad-api-key "El API key está incorrecto o no existe."
             :database-error "Error al conectar a la base de datos."
             :duplicated-user "El usuario ya existe."
             :not-authenticated "El usuario no ha sido autenticado."
             :user-not-found "El usuario no existe."
             :data-processing "Error al procesar los datos biométricos, por favor revíselos e intente nuevamente."
             :licensing "Error con la licencia de FacePhi o el tipo de productos soportados."
             :unhandled "Ha ocurrido un error inesperado. Hemos notificado al administrador del sistema."})
