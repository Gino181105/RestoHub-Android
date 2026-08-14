# RestoHub 5.2 — Firebase Authentication con Google

Este proyecto ya trae implementado el botón **Continuar con Google** con **Firebase Authentication + Credential Manager**.

La única parte que no puede venir dentro del ZIP es `app/google-services.json`, porque ese archivo debe generarse desde **tu propio proyecto de Firebase** para el paquete Android de RestoHub.

## Datos que debes usar

- **Package / Application ID:** `com.example.apprestaurante`
- **Módulo Android:** `app`
- **Archivo Firebase:** `app/google-services.json`
- **Proveedor:** Google

---

## PASO 1 — Abrir tu proyecto de Firebase

1. Entra a Firebase Console.
2. Abre el proyecto que usarás para RestoHub. Puedes usar tu proyecto académico actual si deseas.
3. Entra a **Configuración del proyecto** (ícono de engranaje) > **General**.
4. En **Tus apps**, pulsa **Agregar app** > **Android**.
5. En **Nombre del paquete de Android** escribe exactamente:

```text
com.example.apprestaurante
```

6. Como apodo puedes usar `RestoHub Android`.
7. Registra la aplicación.

> El nombre del paquete es sensible a mayúsculas/minúsculas. No cambies este valor.

---

## PASO 2 — Obtener SHA-1 y SHA-256

En Android Studio abre la pestaña **Terminal** en la raíz del proyecto y ejecuta:

```bat
.\gradlew.bat signingReport
```

Busca la sección:

```text
Variant: debug
Config: debug
SHA1: XX:XX:XX:...
SHA-256: XX:XX:XX:...
```

Copia ambos valores.

Ahora vuelve a Firebase:

**Configuración del proyecto > General > Tus apps > RestoHub Android > Huellas digitales de certificado SHA**

Agrega:

1. SHA-1
2. SHA-256

---

## PASO 3 — Activar Google en Firebase Authentication

En Firebase abre:

**Authentication > Sign-in method / Método de acceso > Google**

1. Pulsa **Google**.
2. Activa **Habilitar**.
3. Selecciona/carga el correo de asistencia del proyecto si Firebase lo solicita.
4. Pulsa **Guardar**.

---

## PASO 4 — Descargar un google-services.json NUEVO

Después de habilitar Google, vuelve a:

**Configuración del proyecto > General > Tus apps > RestoHub Android**

Descarga nuevamente:

```text
google-services.json
```

Debe quedar con ese nombre exacto. No uses:

```text
google-services (1).json
google-services (2).json
```

Cópialo aquí:

```text
RestoHub_Sistema_Integral/
└── app/
    ├── build.gradle.kts
    ├── google-services.json   <-- AQUÍ
    └── src/
```

---

## PASO 5 — Validar que el archivo realmente corresponde a RestoHub

En la terminal ejecuta:

```bat
.\gradlew.bat :app:verifyFirebaseConfig
```

Si todo está correcto verás:

```text
Firebase OK: paquete Android y cliente OAuth web detectados.
```

Si indica que falta el cliente OAuth web, normalmente significa que debes:

1. Activar Google en Authentication.
2. Agregar SHA-1.
3. Descargar nuevamente `google-services.json`.

También puedes ejecutar `VALIDAR_Y_COMPILAR.bat` desde la raíz del proyecto.

---

## PASO 6 — Sincronizar Android Studio

En Android Studio:

1. **File > Sync Project with Gradle Files**.
2. **Build > Clean Project**.
3. **Build > Rebuild Project**.

Luego ejecuta la app en:

- un teléfono Android con Google Play Services; o
- un emulador creado con una imagen que incluya **Google Play**.

No uses un emulador AOSP sin Google Play para probar el inicio de sesión.

---

## PASO 7 — Probar Continuar con Google

1. Abre RestoHub.
2. En la pantalla de acceso verifica el texto de estado:

```text
Firebase Google: listo para iniciar sesión
```

3. Pulsa **Continuar con Google**.
4. Selecciona tu cuenta.
5. Firebase autentica el ID Token de Google.
6. RestoHub crea o vincula el usuario en la base local Room.
7. La app abre `MainActivity`.

Si el correo ya existe en RestoHub, conserva su rol actual. Si es un correo nuevo, se crea como `CLIENT`.

---

## PASO 8 — Comprobarlo en Firebase Console

En Firebase abre:

**Authentication > Users / Usuarios**

Después de iniciar sesión correctamente, tu cuenta de Google debe aparecer allí.

---

# Flujo implementado en el código

```text
btnGoogle
   ↓
CredentialManager
   ↓
GetSignInWithGoogleOption
   ↓
GoogleIdTokenCredential
   ↓
GoogleAuthProvider
   ↓
FirebaseAuth.signInWithCredential(...)
   ↓
Firebase user: email + nombre + foto
   ↓
AuthRepository.loginWithGoogle(...)
   ↓
Room + SessionManager
   ↓
MainActivity
```

Archivos principales:

```text
app/src/main/java/com/example/apprestaurante/ui/auth/LoginActivity.kt
app/src/main/java/com/example/apprestaurante/ui/auth/AuthViewModel.kt
app/src/main/java/com/example/apprestaurante/data/repository/AuthRepository.kt
app/build.gradle.kts
```

---

# Errores frecuentes

## `Falta app/google-services.json`

Solución: copia el archivo dentro de `app/` y sincroniza Gradle.

## `Firebase Google: configuración incompleta`

Normalmente falta `default_web_client_id`. Haz nuevamente este orden:

1. agrega SHA-1 en Firebase;
2. activa Google Authentication;
3. descarga un `google-services.json` nuevo;
4. reemplaza el archivo anterior;
5. sincroniza Gradle.

## Error de credenciales / Developer error

Revisa que la SHA-1 corresponda al mismo equipo/keystore con el que estás ejecutando la app.

Vuelve a ejecutar:

```bat
.\gradlew.bat signingReport
```

Si cambiaste de PC, es posible que tu SHA-1 de debug también haya cambiado.

## No aparece el selector de cuentas Google

Comprueba que el emulador tenga Google Play o prueba en un teléfono real con una cuenta Google iniciada.

## La app entra directamente y no muestra el login

RestoHub ya tiene una sesión local guardada. Cierra sesión desde la app o borra los datos de RestoHub desde Ajustes de Android para repetir la prueba desde cero.

---

# Dependencias Firebase ya incluidas

El proyecto ya contiene:

```kotlin
implementation(platform("com.google.firebase:firebase-bom:34.17.0"))
implementation("com.google.firebase:firebase-auth")
implementation("androidx.credentials:credentials:1.3.0")
implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
```

Y a nivel de proyecto:

```kotlin
id("com.google.gms.google-services") version "4.5.0" apply false
```

No necesitas agregar otra librería para que funcione el botón de Google.
