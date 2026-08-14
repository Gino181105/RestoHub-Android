# Validación realizada — RestoHub 5.0

Fecha: 25/07/2026

## Revisiones ejecutadas

- 58 archivos XML analizados sin errores de estructura.
- IDs usados por View Binding comparados con sus layouts: sin referencias faltantes.
- Activities declaradas en el manifiesto comparadas con las clases existentes.
- Referencias de recursos locales verificadas y recurso de navegación corregido.
- Núcleo Kotlin, entidades, DAO y repositorios compilados con stubs de Room.
- Todos los ViewModels, incluido el de venta directa, compilados con stubs de Lifecycle.
- Factory de ViewModels compilada de forma aislada.
- Prueba ejecutable del flujo de negocio:
  - venta directa;
  - descuento de stock;
  - registro del pago;
  - emisión correlativa de boleta;
  - cálculo de operación gravada e IGV;
  - pedido desde carrito;
  - cancelación y devolución de stock.
- Backend Node probado con health, dashboard, alta de producto, alta de usuario, alta de pedido, cambio de estado y emisión de comprobante.
- Revisión de ausencia de `runBlocking` y `GlobalScope` en el código de la app.
- Revisión de adapters con retorno `Unit`, `ListAdapter` y `DiffUtil`.
- ZIP final validado con prueba de integridad.

## Resultado de la prueba del negocio

```text
LOGIC_TEST_OK order=1, document=1, restoredStock=8
```

## Limitación del entorno

No fue posible ejecutar el ensamblado Android completo porque este entorno no contiene Android SDK y no pudo descargar la distribución de Gradle ni los artefactos Maven. Por ello, la comprobación final del APK debe realizarse en Android Studio con JDK 17 y SDK 36.

Esto significa que el proyecto fue revisado estática y lógicamente, pero no se afirma que un APK haya sido generado dentro de este entorno.

---

## Revisión adicional — Google Sign-In y rediseño (09/08/2026)

- Se añadieron Firebase Authentication, Credential Manager y Google ID a Gradle.
- Se preparó el plugin `com.google.gms.google-services` para activarse cuando exista `app/google-services.json`.
- Se añadió el flujo de Google -> Firebase -> usuario local de RestoHub.
- Se añadió el botón `btnGoogle` al layout y se verificó que todos los IDs usados por `LoginActivity` existan.
- Se analizaron 60 archivos XML del proyecto y no se encontraron errores de estructura XML.
- Se verificó que la app siga teniendo un modo seguro cuando `google-services.json` no esté presente: el botón informa la configuración faltante y no intenta inicializar Firebase.
- El ensamblado Android completo no pudo ejecutarse en este entorno porque no hay Android SDK local y el wrapper de Gradle no puede descargar artefactos por falta de acceso de red. La compilación final debe hacerse en Android Studio después de añadir `app/google-services.json`.

## Validación adicional — versión visual 5.1 (09/08/2026)

- 64 archivos XML analizados correctamente: sin errores de estructura.
- IDs usados por View Binding comparados con sus layouts: sin referencias faltantes.
- Activities del manifiesto comparadas con las clases Kotlin existentes: sin faltantes.
- Referencias locales de layout/drawable/menu/xml/color/string/style revisadas: sin recursos faltantes.
- Imágenes locales verificadas y decodificadas correctamente:
  - `product_lomo.png`
  - `product_aji.png`
  - `product_ceviche.png`
  - `product_chicha.png`
  - `product_suspiro.png`
  - `hero_restohub.png`
  - `logo.png`
- Backend REST iniciado y comprobado en `/health`: `status=ok`, versión `5.1`.
- Se intentó ejecutar Gradle Wrapper; este entorno no tiene acceso de red a `services.gradle.org`, por lo que no puede descargar Gradle 8.13 ni generar el APK aquí.
- El proyecto mantiene funcionamiento local con Room. El acceso con Google permanece opcional y requiere el `app/google-services.json` propio del proyecto Firebase.
