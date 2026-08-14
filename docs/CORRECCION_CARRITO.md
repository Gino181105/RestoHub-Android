# Corrección del cierre del carrito

## Causa probable encontrada

El adaptador anterior se construía como propiedad del Fragment usando referencias como `viewModel::increase`. Esa expresión podía solicitar el ViewModel antes de que el Fragment estuviera asociado a una Activity y provocar un cierre al entrar al carrito.

## Corrección aplicada

El adaptador se declara con `lateinit` y se construye dentro de `onViewCreated()`:

```kotlin
cartAdapter = CartAdapter(
    onIncrease = { item -> viewModel.increase(item) },
    onDecrease = { item -> viewModel.decrease(item) },
    onRemove = { item -> confirmRemove(item) }
)
```

En `onDestroyView()` se desconecta del RecyclerView:

```kotlin
if (::cartAdapter.isInitialized) binding.rvCart.adapter = null
_binding = null
```

## Otras protecciones

- Carrito separado por usuario.
- Validación de sesión.
- Comprobación de stock al agregar, modificar y confirmar.
- Operaciones Room dentro de corrutinas.
- Excepciones convertidas en mensajes de interfaz.
- Checkout completo dentro de `withTransaction`.
- Restauración automática de stock al cancelar.
- Nueva versión de la base de datos para evitar conflictos con esquemas antiguos.
