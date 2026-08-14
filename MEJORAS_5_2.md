# RestoHub 5.2 — mejoras aplicadas

## Presentación visual

- Se reemplazaron las ilustraciones de ceviche, lomo saltado, ají de gallina, chicha morada y suspiro limeño por fotografías reales optimizadas en WebP.
- Se reemplazó el banner ilustrado de inicio por un collage fotográfico de platos reales.
- Se mejoró la tarjeta de inventario de recepción: fotografía más grande, espaciado más limpio y mayor legibilidad.
- Se cambió el fondo de las tarjetas a blanco para que las fotografías tengan mejor contraste.
- Los errores de carga de imágenes ya no muestran el logo como si fuera una foto de producto; ahora usan un fondo neutro.

## Firebase Google

- Se conservó Firebase Authentication + Credential Manager como flujo principal del botón `Continuar con Google`.
- Se agregó un indicador visible en el login para saber si Firebase está listo, pendiente o incompleto.
- Se agregó la tarea Gradle `:app:verifyFirebaseConfig`.
- Se agregó `VALIDAR_Y_COMPILAR.bat` para validar `google-services.json` y generar el APK debug.
- Se actualizó la guía `FIREBASE_GOOGLE_SETUP.md` con un procedimiento completo.

## Versión

- `versionCode`: 9
- `versionName`: 5.2
