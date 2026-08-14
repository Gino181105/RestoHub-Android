# RestoHub 5.2 — Sistema integral para restaurante

Proyecto académico Android desarrollado en Kotlin. Sistematiza el proceso básico de un restaurante con tres roles: **Cliente, Recepcionista y Administrador**. El código usa clases, funciones, corrutinas, Activities, Fragments, RecyclerView, View Binding, MVVM, Room/SQLite, SharedPreferences, Retrofit, Glide, cámara, permisos e Intents.


## Mejora visual 5.2

- Se reemplazaron las ilustraciones por **fotografías reales** de lomo saltado, ají de gallina, ceviche, chicha morada y suspiro a la limeña, optimizadas como recursos WebP locales.
- Las imágenes se muestran en carta, carrito, inventario, detalle del pedido y comprobante.
- Se añadió un banner fotográfico gastronómico en el inicio del cliente.
- Inventario rediseñado con tarjetas más amplias, fotografía, estado, stock y acciones más legibles.
- Comprobante rediseñado como tarjeta/recibo con logo, mejor jerarquía y detalle visual de los productos.
- Las imágenes de demostración son recursos locales: la carta principal funciona sin Internet.

## Firebase Authentication con Google

El botón **Continuar con Google** está implementado con Firebase Authentication y Credential Manager. Para activarlo debes registrar `com.example.apprestaurante` en tu proyecto Firebase, agregar SHA-1/SHA-256, habilitar el proveedor Google y copiar el `google-services.json` actualizado dentro de `app/`.

Guía completa: `FIREBASE_GOOGLE_SETUP.md`.

Validación automática:

```bat
.\gradlew.bat :app:verifyFirebaseConfig
```

También puedes usar `VALIDAR_Y_COMPILAR.bat`.

## Proceso implementado

```text
Cliente / caja selecciona productos
        ↓
Pedido y descuento transaccional del stock
        ↓
Pendiente → Confirmado → En preparación → Listo → Entregado
        ↓
Registro del pago
        ↓
Emisión local de boleta o factura
        ↓
Consulta, detalle y PDF compartible
```

La cancelación devuelve el stock automáticamente. Solo se permite borrar un pedido cancelado que no tenga comprobante emitido.

## Rol Cliente

- Registro, inicio de sesión y recuperación de contraseña.
- Consulta del menú con búsqueda y categorías.
- Carrito persistente por usuario.
- Aumentar, disminuir y quitar productos con validación de stock.
- Pedido para mesa, recojo o delivery.
- Selección de efectivo, tarjeta o Yape/Plin.
- Solicitud de boleta o factura.
- Historial y detalle del pedido.
- Cancelación temprana y eliminación de pedidos cancelados.
- Consulta de sus comprobantes.
- Perfil, foto local y apertura de ubicación mediante Intent.

## Rol Recepcionista

- Dashboard operativo.
- Nueva venta directa de caja para un cliente o “Cliente Mostrador”.
- Selección de productos y cantidades.
- Registro opcional del pago al crear la venta.
- Emisión de boleta o factura.
- Gestión de todos los pedidos.
- Cambio controlado de estados.
- Registro de pago pendiente.
- Visualización y exportación de comprobantes en PDF.
- CRUD de productos, precios, stock, categorías e imágenes.
- Captura de fotografía o selección desde galería.

## Rol Administrador

Incluye todo lo permitido al recepcionista y además:

- Dashboard general con productos, stock bajo, pedidos, clientes, personal, comprobantes y ventas.
- Nueva venta directa desde el panel.
- Gestión de clientes, recepcionistas y administradores.
- Creación, edición, activación y desactivación de usuarios.
- Asignación y modificación de roles.
- Restablecimiento de contraseña desde el formulario de usuario.
- Gestión completa de productos e inventario.
- Consulta centralizada de pedidos, boletas y facturas.

El sistema impide que el administrador activo se desactive o se quite su propio rol.

## Boleta y factura

- Boleta: serie `B001`.
- Factura: serie `F001`.
- Numeración correlativa independiente.
- Cálculo de operación gravada e IGV del 18 % a partir del total.
- Un comprobante por pedido.
- Generación de PDF compartible.

**Importante:** son representaciones locales para una demostración académica. No se firman digitalmente ni se envían a SUNAT. Una emisión electrónica real requiere RUC, certificado, proveedor/OSE o integración oficial y reglas tributarias vigentes.

## Credenciales de demostración

```text
ADMINISTRADOR
Correo: admin@restohub.pe
Contraseña: Admin123

RECEPCIONISTA
Correo: recepcionista@restohub.pe
Contraseña: Recepcion123

CLIENTE
Correo: cliente@restohub.pe
Contraseña: Cliente123
```

También existe el cliente interno `mostrador@restohub.pe`, usado para ventas de caja sin registro previo.

## Arquitectura

```text
com.example.apprestaurante
├── core                    Resultados, validación, contraseñas, precios, IGV y PDF
├── data
│   ├── local               Entidades, DAO y base Room
│   ├── remote              Retrofit y DTO
│   ├── repository          Reglas de negocio y transacciones
│   └── session             Sesión con SharedPreferences
├── domain/model            Roles, estados, servicio, pago y comprobante
└── ui
    ├── adapters            ListAdapter, DiffUtil y ViewHolder
    ├── auth                Login, registro y recuperación
    ├── cart                Carrito y checkout
    ├── products            Carta del cliente
    ├── orders              Pedidos y flujo de estados
    ├── documents           Boletas, facturas y PDF
    ├── reception           Inventario y dashboard operativo
    ├── admin               Dashboard y usuarios
    └── sales               Venta directa de caja
```

## Base de datos Room

Tablas:

- `users`
- `products`
- `cart_items`
- `orders`
- `order_items`
- `sales_documents`

Room controla claves foráneas, índices, consultas, CRUD y transacciones. El checkout, la venta de caja, el descuento/devolución de stock y la emisión del comprobante se ejecutan en repositorios.

## Corrección de congelamiento y cierre del carrito

- Se eliminó `runBlocking` del inicio de la aplicación.
- Los datos iniciales se crean en `Dispatchers.IO`.
- El adaptador del carrito se crea en `onViewCreated()`.
- El RecyclerView se desconecta en `onDestroyView()`.
- Los layouts extensos usan `NestedScrollView` y desactivan el desplazamiento anidado de listas internas.
- Las operaciones de Room y hash de contraseña se ejecutan fuera del hilo principal.
- Las excepciones se convierten en mensajes visibles, evitando cierres silenciosos.

## Requisitos

- Android Studio.
- JDK 17, preferentemente **Embedded JDK 17**.
- Android SDK Platform 36.
- Gradle 8.13.
- Android Gradle Plugin 8.13.2.
- Emulador o teléfono con Android API 24 o superior.

## Abrir y ejecutar

1. Descomprime el proyecto.
2. Abre la carpeta que contiene `settings.gradle.kts`, `build.gradle.kts`, `gradlew` y `app`.
3. Selecciona **Embedded JDK 17** en la configuración de Gradle.
4. Instala Android SDK Platform 36 cuando Android Studio lo solicite.
5. Ejecuta **Sync Project with Gradle Files**.
6. Ejecuta **Build → Clean Project**.
7. Ejecuta **Build → Rebuild Project**.
8. Selecciona el dispositivo y ejecuta `app`.

Debido al cambio de esquema, desinstala una versión antigua de RestoHub o borra sus datos antes de la primera ejecución de esta versión.

## Backend REST incluido

La carpeta `backend-restohub` contiene un servicio Node.js sin dependencias externas con endpoints de salud, dashboard, productos, usuarios, pedidos y comprobantes.

```bash
cd backend-restohub
node server.mjs
```

La aplicación Android mantiene Room como fuente local para poder funcionar sin conexión.

## Documentación

- `docs/ARQUITECTURA.md`
- `docs/FLUJO_RESTAURANTE.md`
- `docs/COMPROBANTES.md`
- `docs/MATRIZ_SILABO.md`
- `docs/PRUEBAS_MANUALES.md`
- `docs/CORRECCION_CARRITO.md`
- `VALIDACION_REALIZADA.md`

---

## Inicio de sesión con Google (Firebase)

Esta versión incluye el botón **Continuar con Google** y la integración de código con Firebase Authentication/Credential Manager.
Para activar el acceso real con tu propio proyecto Firebase, sigue `FIREBASE_GOOGLE_SETUP.md` y coloca tu archivo `app/google-services.json`.
