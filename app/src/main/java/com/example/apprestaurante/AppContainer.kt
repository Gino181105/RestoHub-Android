package com.example.apprestaurante

import android.content.Context
import com.example.apprestaurante.core.PasswordHasher
import com.example.apprestaurante.data.local.RestoHubDatabase
import com.example.apprestaurante.data.local.entity.ProductEntity
import com.example.apprestaurante.data.local.entity.UserEntity
import com.example.apprestaurante.data.remote.ApiFactory
import com.example.apprestaurante.data.repository.AuthRepository
import com.example.apprestaurante.data.repository.CartRepository
import com.example.apprestaurante.data.repository.OrderRepository
import com.example.apprestaurante.data.repository.ProductRepository
import com.example.apprestaurante.data.repository.RemoteRepository
import com.example.apprestaurante.data.repository.SalesDocumentRepository
import com.example.apprestaurante.data.repository.StaffSaleRepository
import com.example.apprestaurante.data.repository.UserManagementRepository
import com.example.apprestaurante.data.session.SessionManager
import com.example.apprestaurante.domain.model.UserRole
import kotlinx.coroutines.CompletableDeferred

class AppContainer(private val context: Context) {
    private val initialization = CompletableDeferred<Unit>()
    val database: RestoHubDatabase = RestoHubDatabase.getInstance(context)
    val session = SessionManager(context).apply { ensureDataVersion(DATABASE_VERSION) }

    val authRepository = AuthRepository(database.userDao(), session) { awaitReady() }
    val userManagementRepository = UserManagementRepository(database.userDao())
    val productRepository = ProductRepository(database.productDao())
    val cartRepository = CartRepository(database)
    val orderRepository = OrderRepository(database)
    val salesDocumentRepository = SalesDocumentRepository(database)
    val staffSaleRepository = StaffSaleRepository(database) { awaitReady() }
    val remoteRepository = RemoteRepository(ApiFactory.create())

    suspend fun initialize() {
        runCatching {
            seedUsers()
            if (database.productDao().countAll() == 0) {
                database.productDao().insertAll(defaultProducts())
            }
        }.onSuccess {
            initialization.complete(Unit)
        }.onFailure {
            initialization.completeExceptionally(it)
            throw it
        }
    }

    suspend fun awaitReady() {
        initialization.await()
    }

    private suspend fun seedUsers() {
        createSeedUser(
            fullName = "Ana Administradora",
            email = ADMIN_EMAIL,
            phone = "999000111",
            password = ADMIN_PASSWORD,
            role = UserRole.ADMIN
        )
        createSeedUser(
            fullName = "Rosa Recepción",
            email = RECEPTION_EMAIL,
            phone = "999111222",
            password = RECEPTION_PASSWORD,
            role = UserRole.RECEPTIONIST
        )
        createSeedUser(
            fullName = "Carlos Cliente",
            email = CLIENT_EMAIL,
            phone = "988777666",
            password = CLIENT_PASSWORD,
            role = UserRole.CLIENT
        )
        createSeedUser(
            fullName = "Cliente Mostrador",
            email = WALK_IN_EMAIL,
            phone = "",
            password = WALK_IN_PASSWORD,
            role = UserRole.CLIENT
        )
    }

    private suspend fun createSeedUser(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        role: UserRole
    ) {
        val userDao = database.userDao()
        if (userDao.findByEmail(email) == null) {
            userDao.insert(
                UserEntity(
                    fullName = fullName,
                    email = email,
                    phone = phone,
                    passwordHash = PasswordHasher.hash(password.toCharArray()),
                    role = role.name,
                    documentType = "DNI",
                    documentNumber = if (role == UserRole.CLIENT) "74859632" else ""
                )
            )
        }
    }

    private fun resourceUri(drawableId: Int): String {
        val entryName = context.resources.getResourceEntryName(drawableId)
        return "android.resource://${context.packageName}/drawable/$entryName"
    }

    private fun defaultProducts(): List<ProductEntity> = listOf(
        ProductEntity(
            name = "Lomo saltado",
            description = "Carne de res, cebolla, tomate, papas fritas y arroz.",
            category = "Fondos",
            price = 32.90,
            stock = 20,
            imageUri = resourceUri(R.drawable.product_lomo)
        ),
        ProductEntity(
            name = "Ají de gallina",
            description = "Pollo deshilachado en crema de ají amarillo con arroz.",
            category = "Fondos",
            price = 25.50,
            stock = 18,
            imageUri = resourceUri(R.drawable.product_aji)
        ),
        ProductEntity(
            name = "Ceviche clásico",
            description = "Pescado fresco, limón, cebolla, camote y choclo.",
            category = "Entradas",
            price = 30.00,
            stock = 15,
            imageUri = resourceUri(R.drawable.product_ceviche)
        ),
        ProductEntity(
            name = "Chicha morada",
            description = "Bebida tradicional de maíz morado y frutas.",
            category = "Bebidas",
            price = 8.00,
            stock = 35,
            imageUri = resourceUri(R.drawable.product_chicha)
        ),
        ProductEntity(
            name = "Suspiro a la limeña",
            description = "Postre de manjar, merengue y canela.",
            category = "Postres",
            price = 12.50,
            stock = 12,
            imageUri = resourceUri(R.drawable.product_suspiro)
        )
    )

    companion object {
        private const val DATABASE_VERSION = 5
        const val ADMIN_EMAIL = "admin@restohub.pe"
        const val ADMIN_PASSWORD = "Admin123"
        const val RECEPTION_EMAIL = "recepcionista@restohub.pe"
        const val RECEPTION_PASSWORD = "Recepcion123"
        const val CLIENT_EMAIL = "cliente@restohub.pe"
        const val CLIENT_PASSWORD = "Cliente123"
        const val WALK_IN_EMAIL = "mostrador@restohub.pe"
        const val WALK_IN_PASSWORD = "Mostrador123"
    }
}
