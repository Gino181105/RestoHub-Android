# Flujo funcional del restaurante

## 1. Cliente realiza un pedido

1. Inicia sesión o se registra.
2. Revisa la carta.
3. Agrega productos al carrito.
4. Selecciona mesa, recojo o delivery.
5. Selecciona el medio de pago.
6. Solicita boleta o factura.
7. Confirma el pedido.
8. Room registra cabecera, detalle y descuenta stock en una transacción.

## 2. Recepción procesa el pedido

1. Consulta pedidos pendientes.
2. Confirma el pedido.
3. Cambia a “En preparación”.
4. Cambia a “Listo para entregar”.
5. Registra el pago y genera el comprobante.
6. Cambia a “Entregado”.

## 3. Venta directa de caja

1. El personal abre “Nueva venta en caja”.
2. Selecciona un cliente o “Cliente Mostrador”.
3. Agrega productos.
4. Registra mesa, recojo o delivery.
5. Elige medio de pago y comprobante.
6. Puede cobrar y emitir inmediatamente o dejar el pago pendiente.

## 4. Cancelación

- El cliente cancela mientras el pedido esté pendiente o confirmado y no esté pagado.
- Recepción o administración puede cancelar durante las primeras etapas si no existe pago.
- La transacción devuelve el stock.
- Solo un pedido cancelado sin comprobante puede borrarse.

## 5. Administración

- Gestiona productos, stock e imágenes.
- Gestiona usuarios y roles.
- Consulta el total de comprobantes y ventas.
- Atiende pedidos y ventas de caja igual que recepción.
