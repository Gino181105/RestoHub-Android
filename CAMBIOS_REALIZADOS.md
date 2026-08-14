# Cambios realizados en RestoHub

## Login

- Diseño más limpio, moderno y centrado.
- Tarjeta de acceso con bordes redondeados.
- Jerarquía visual mejorada para logo, título y subtítulo.
- Botones más consistentes con Material 3.
- Fondo cálido en degradado.
- Nuevo botón **Continuar con Google** con el ícono multicolor de Google.

## Tipografía

- Tipografía global cambiada a una familia redondeada del sistema (`sans-serif-rounded`) para darle una apariencia más moderna y legible.
- Se agregó una guía para reemplazarla por una fuente descargada de DaFont en tu computadora si deseas usar una fuente exacta.

## Firebase / Google

- Dependencia Firebase Authentication.
- Firebase Android BoM.
- Credential Manager.
- Google ID library.
- Plugin de Google Services preparado.
- Integración de Google ID Token con Firebase Authentication.
- Creación automática del usuario Google en la base local de RestoHub como CLIENT cuando es nuevo.
- Conservación del rol existente cuando el correo ya pertenece a un usuario local.
- Foto de perfil de Google guardada en `photoUri` cuando está disponible.
- Cierre de sesión de Firebase al cerrar la sesión local.

## Importante

Por seguridad y porque depende de tu propio proyecto de Firebase, debes agregar tú mismo:

`app/google-services.json`

Consulta `FIREBASE_GOOGLE_SETUP.md` para los pasos exactos.


## Mejora visual 5.1 — imágenes y presentación

- Nuevas ilustraciones locales para los cinco productos precargados.
- Nuevo banner gastronómico en la pantalla principal del cliente.
- Tarjetas de productos e inventario rediseñadas con imágenes grandes y bordes redondeados.
- Carrito y detalle de pedidos con miniaturas más claras.
- Comprobante visual rediseñado con formato tipo recibo y logo de RestoHub.
- Estado ACTIVO/INACTIVO mostrado con etiqueta visual.
- Referencias de recursos locales guardadas por nombre para que sean más estables entre compilaciones.
- Versión de aplicación actualizada a 5.1 (versionCode 8).
