(ns facephi-service.messages
  "Messages catalogue. These message strings are used across the service.")

(def errors {:bad-api-key "El API key está incorrecto o no existe."
             :database-error "Error al conectar a la base de datos."
             :duplicated-user "El usuario ya existe."
             :not-authenticated "El usuario no ha sido autenticado."
             :user-not-found "El usuario no existe."
             :data-processing "Error al procesar los datos biométricos, por favor revíselos e intente nuevamente."
             :licensing "Error con la licencia de FacePhi o el tipo de productos soportados."
             :unhandled "Ha ocurrido un error inesperado. Hemos notificado al administrador del sistema."
             :user-blocked "El acceso por reconocimiento facial está bloqueado para su usuario por haber superado los intentos permitidos. Por favor, ingrese con su usuario y contraseña."})
