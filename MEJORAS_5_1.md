# RestoHub 5.1 — mejoras entregadas

Esta edición se preparó tomando como referencia las pantallas de inventario y comprobante compartidas.

## Cambios visuales

- Ilustraciones nuevas y locales para los platos precargados.
- Fotografías/ilustraciones más grandes en inventario y carta.
- Miniaturas mejoradas en carrito, pedido y comprobante.
- Banner gastronómico en el inicio del cliente.
- Inventario con tarjetas redondeadas, estado visual y acciones más claras.
- Comprobante con apariencia de recibo, logo, divisores y jerarquía de total.
- Formulario de producto con previsualización redondeada.

## Funcionamiento

- Se mantienen Cliente, Recepcionista y Administrador.
- Room sigue siendo la base local principal y funciona sin depender del backend.
- Se conserva el backend REST de demostración.
- Se conserva la integración preparada para Firebase Authentication + Google.
- Si no existe `app/google-services.json`, el proyecto sigue compilando y el login local continúa disponible; el botón de Google informa qué configuración falta.

## Para abrir

Abre en Android Studio la carpeta que contiene `settings.gradle.kts` y ejecuta Sync/Rebuild con JDK 17 y SDK 36.
