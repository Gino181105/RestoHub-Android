# Backend REST académico de RestoHub

Servicio Node.js sin paquetes externos. Persiste información en `db.json` y demuestra CRUD y consumo REST sin reemplazar la base Room de la aplicación Android.

## Ejecutar

```bash
cd backend-restohub
node server.mjs
```

Servidor local: `http://localhost:3000`.

Desde el emulador Android, la computadora se accede mediante `http://10.0.2.2:3000/`.

## Rutas

### Estado y panel

- `GET /health`
- `GET /dashboard`

### Productos

- `GET /products`
- `GET /products/:id`
- `POST /products`
- `PUT /products/:id`
- `DELETE /products/:id` — desactivación lógica

### Usuarios

- `GET /users`
- `GET /users/:id`
- `POST /users`
- `PUT /users/:id`

### Pedidos

- `GET /orders`
- `GET /orders/:id`
- `POST /orders`
- `PATCH /orders/:id/status`
- `DELETE /orders/:id` — solo cancelados

### Boletas y facturas

- `GET /documents`
- `GET /documents/:id`
- `POST /documents`

Los comprobantes del backend y de la app son representaciones académicas. No se transmiten a SUNAT.
