# Arquitectura de RestoHub 5.0

## Organización por capas

### UI

Activities, Fragments y adapters muestran datos y capturan eventos. Se utiliza View Binding para evitar búsquedas manuales de vistas y `ListAdapter` con `DiffUtil` para las listas.

### ViewModel

Conserva el estado de cada pantalla y ejecuta operaciones asíncronas con `viewModelScope`. La UI no accede directamente a los DAO.

### Repository

Contiene las reglas de negocio:

- autenticación y recuperación de contraseña;
- validación del carrito;
- venta directa de caja;
- descuento y devolución de stock;
- transiciones permitidas del pedido;
- registro del pago;
- numeración y emisión de boleta o factura;
- permisos según rol;
- CRUD de usuarios y productos.

### Room

Los DAO realizan consultas, inserciones, modificaciones y eliminaciones. Las operaciones que afectan varias tablas se ejecutan con `withTransaction`.

### Sesión

`SessionManager` guarda id, nombre y rol mediante `SharedPreferences`.

## Roles

```text
CLIENT          Compra y consulta su información.
RECEPTIONIST    Atiende caja, productos, pedidos y comprobantes.
ADMIN           Control total, incluyendo usuarios y roles.
```

## Flujo del cliente

```text
Login → Carta → Carrito → Checkout → Pedido → Pago en recepción → Comprobante
```

## Flujo de caja

```text
Login personal → Nueva venta → Cliente → Productos → Servicio/Pago → Boleta/Factura
```

## Flujo del pedido

```text
PENDING → CONFIRMED → PREPARING → READY → DELIVERED
    └──────────────→ CANCELLED ←──────────┘
```

El pedido entregado debe estar pagado. La cancelación de un pedido pendiente devuelve las unidades al inventario.
