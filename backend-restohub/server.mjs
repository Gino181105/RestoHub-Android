import http from 'node:http';
import { readFile, writeFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const PORT = Number(process.env.PORT ?? 3000);
const ROOT = path.dirname(fileURLToPath(import.meta.url));
const DB_FILE = path.join(ROOT, 'db.json');

async function readDb() {
  return JSON.parse(await readFile(DB_FILE, 'utf8'));
}

async function saveDb(db) {
  await writeFile(DB_FILE, `${JSON.stringify(db, null, 2)}\n`, 'utf8');
}

function send(response, status, body) {
  response.writeHead(status, {
    'Content-Type': 'application/json; charset=utf-8',
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Headers': 'Content-Type',
    'Access-Control-Allow-Methods': 'GET,POST,PUT,PATCH,DELETE,OPTIONS'
  });
  response.end(status === 204 ? '' : JSON.stringify(body));
}

async function bodyAsJson(request) {
  const chunks = [];
  for await (const chunk of request) chunks.push(chunk);
  return chunks.length === 0 ? {} : JSON.parse(Buffer.concat(chunks).toString('utf8'));
}

function nextId(items) {
  return items.reduce((max, item) => Math.max(max, Number(item.id) || 0), 0) + 1;
}

function findById(items, id) {
  return items.find(item => Number(item.id) === id);
}

const server = http.createServer(async (request, response) => {
  try {
    if (request.method === 'OPTIONS') return send(response, 204, {});

    const url = new URL(request.url ?? '/', `http://${request.headers.host ?? 'localhost'}`);
    const parts = url.pathname.split('/').filter(Boolean);
    const resource = parts[0] ?? '';
    const id = Number(parts[1] ?? 0);
    const db = await readDb();

    if (request.method === 'GET' && url.pathname === '/health') {
      return send(response, 200, { status: 'ok', service: 'RestoHub REST', version: '5.1' });
    }

    if (request.method === 'GET' && url.pathname === '/dashboard') {
      const paidDocuments = db.documents ?? [];
      return send(response, 200, {
        activeProducts: db.products.filter(item => item.active).length,
        lowStockProducts: db.products.filter(item => item.active && item.stock <= 5).length,
        activeOrders: db.orders.filter(item => !['DELIVERED', 'CANCELLED'].includes(item.status)).length,
        clients: db.users.filter(item => item.role === 'CLIENT' && item.active).length,
        staff: db.users.filter(item => ['ADMIN', 'RECEPTIONIST'].includes(item.role) && item.active).length,
        documents: paidDocuments.length,
        sales: paidDocuments.reduce((sum, item) => sum + Number(item.total || 0), 0)
      });
    }

    if (resource === 'products') {
      if (request.method === 'GET' && !id) return send(response, 200, db.products);
      if (request.method === 'GET' && id) {
        const product = findById(db.products, id);
        return product ? send(response, 200, product) : send(response, 404, { message: 'Producto no encontrado' });
      }
      if (request.method === 'POST') {
        const input = await bodyAsJson(request);
        const product = {
          id: nextId(db.products),
          name: String(input.name ?? '').trim(),
          description: String(input.description ?? '').trim(),
          category: String(input.category ?? '').trim(),
          price: Number(input.price ?? 0),
          stock: Number(input.stock ?? 0),
          imageUrl: String(input.imageUrl ?? '').trim(),
          active: input.active !== false
        };
        if (product.name.length < 3 || product.price <= 0 || product.stock < 0) {
          return send(response, 400, { message: 'Datos de producto no válidos' });
        }
        db.products.push(product);
        await saveDb(db);
        return send(response, 201, product);
      }
      if (request.method === 'PUT' && id) {
        const index = db.products.findIndex(item => item.id === id);
        if (index < 0) return send(response, 404, { message: 'Producto no encontrado' });
        const input = await bodyAsJson(request);
        db.products[index] = { ...db.products[index], ...input, id };
        await saveDb(db);
        return send(response, 200, db.products[index]);
      }
      if (request.method === 'DELETE' && id) {
        const product = findById(db.products, id);
        if (!product) return send(response, 404, { message: 'Producto no encontrado' });
        product.active = false;
        await saveDb(db);
        return send(response, 200, { message: 'Producto desactivado', product });
      }
    }

    if (resource === 'users') {
      if (request.method === 'GET' && !id) return send(response, 200, db.users);
      if (request.method === 'GET' && id) {
        const user = findById(db.users, id);
        return user ? send(response, 200, user) : send(response, 404, { message: 'Usuario no encontrado' });
      }
      if (request.method === 'POST') {
        const input = await bodyAsJson(request);
        const user = {
          id: nextId(db.users),
          fullName: String(input.fullName ?? '').trim(),
          email: String(input.email ?? '').trim().toLowerCase(),
          role: String(input.role ?? 'CLIENT').toUpperCase(),
          documentNumber: String(input.documentNumber ?? '').trim(),
          active: input.active !== false
        };
        if (user.fullName.length < 3 || !user.email.includes('@') || !['CLIENT', 'RECEPTIONIST', 'ADMIN'].includes(user.role)) {
          return send(response, 400, { message: 'Datos de usuario no válidos' });
        }
        if (db.users.some(item => item.email === user.email)) {
          return send(response, 409, { message: 'El correo ya existe' });
        }
        db.users.push(user);
        await saveDb(db);
        return send(response, 201, user);
      }
      if (request.method === 'PUT' && id) {
        const index = db.users.findIndex(item => item.id === id);
        if (index < 0) return send(response, 404, { message: 'Usuario no encontrado' });
        const input = await bodyAsJson(request);
        db.users[index] = { ...db.users[index], ...input, id };
        await saveDb(db);
        return send(response, 200, db.users[index]);
      }
    }

    if (resource === 'orders') {
      if (request.method === 'GET' && !id) return send(response, 200, db.orders);
      if (request.method === 'GET' && id) {
        const order = findById(db.orders, id);
        return order ? send(response, 200, order) : send(response, 404, { message: 'Pedido no encontrado' });
      }
      if (request.method === 'POST') {
        const input = await bodyAsJson(request);
        const order = {
          id: nextId(db.orders),
          clientId: Number(input.clientId ?? 0),
          items: Array.isArray(input.items) ? input.items : [],
          total: Number(input.total ?? 0),
          serviceType: String(input.serviceType ?? 'En mesa'),
          paymentMethod: String(input.paymentMethod ?? 'Efectivo'),
          paymentStatus: 'PENDING',
          status: 'PENDING',
          createdAt: new Date().toISOString()
        };
        if (order.clientId <= 0 || order.items.length === 0 || order.total <= 0) {
          return send(response, 400, { message: 'Datos de pedido no válidos' });
        }
        db.orders.push(order);
        await saveDb(db);
        return send(response, 201, order);
      }
      if (request.method === 'PATCH' && id && parts[2] === 'status') {
        const order = findById(db.orders, id);
        if (!order) return send(response, 404, { message: 'Pedido no encontrado' });
        const input = await bodyAsJson(request);
        const allowed = ['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'DELIVERED', 'CANCELLED'];
        const status = String(input.status ?? '').toUpperCase();
        if (!allowed.includes(status)) return send(response, 400, { message: 'Estado no válido' });
        order.status = status;
        await saveDb(db);
        return send(response, 200, order);
      }
      if (request.method === 'DELETE' && id) {
        const order = findById(db.orders, id);
        if (!order) return send(response, 404, { message: 'Pedido no encontrado' });
        if (order.status !== 'CANCELLED') return send(response, 409, { message: 'Solo se eliminan pedidos cancelados' });
        db.orders = db.orders.filter(item => item.id !== id);
        await saveDb(db);
        return send(response, 200, { message: 'Pedido eliminado' });
      }
    }

    if (resource === 'documents') {
      if (request.method === 'GET' && !id) return send(response, 200, db.documents);
      if (request.method === 'GET' && id) {
        const document = findById(db.documents, id);
        return document ? send(response, 200, document) : send(response, 404, { message: 'Comprobante no encontrado' });
      }
      if (request.method === 'POST') {
        const input = await bodyAsJson(request);
        const type = String(input.type ?? 'BOLETA').toUpperCase();
        if (!['BOLETA', 'FACTURA'].includes(type) || Number(input.total ?? 0) <= 0) {
          return send(response, 400, { message: 'Datos de comprobante no válidos' });
        }
        const series = type === 'FACTURA' ? 'F001' : 'B001';
        const number = db.documents.filter(item => item.series === series).length + 1;
        const total = Number(input.total);
        const document = {
          id: nextId(db.documents),
          orderId: Number(input.orderId ?? 0),
          type,
          series,
          number,
          formattedNumber: `${series}-${String(number).padStart(8, '0')}`,
          clientName: String(input.clientName ?? '').trim(),
          clientDocument: String(input.clientDocument ?? '').trim(),
          subtotal: Number((total / 1.18).toFixed(2)),
          igv: Number((total - total / 1.18).toFixed(2)),
          total,
          issuedAt: new Date().toISOString()
        };
        db.documents.push(document);
        const order = findById(db.orders, document.orderId);
        if (order) order.paymentStatus = 'PAID';
        await saveDb(db);
        return send(response, 201, document);
      }
    }

    return send(response, 404, { message: 'Ruta no encontrada' });
  } catch (error) {
    return send(response, 500, {
      message: error instanceof Error ? error.message : 'Error interno'
    });
  }
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`RestoHub REST ejecutándose en http://localhost:${PORT}`);
});
