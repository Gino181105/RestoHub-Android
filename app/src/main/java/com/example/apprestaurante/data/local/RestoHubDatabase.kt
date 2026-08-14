package com.example.apprestaurante.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.apprestaurante.data.local.dao.CartDao
import com.example.apprestaurante.data.local.dao.OrderDao
import com.example.apprestaurante.data.local.dao.ProductDao
import com.example.apprestaurante.data.local.dao.SalesDocumentDao
import com.example.apprestaurante.data.local.dao.UserDao
import com.example.apprestaurante.data.local.entity.CartItemEntity
import com.example.apprestaurante.data.local.entity.OrderEntity
import com.example.apprestaurante.data.local.entity.OrderItemEntity
import com.example.apprestaurante.data.local.entity.ProductEntity
import com.example.apprestaurante.data.local.entity.SalesDocumentEntity
import com.example.apprestaurante.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        SalesDocumentEntity::class
    ],
    version = 5,
    exportSchema = true
)
abstract class RestoHubDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun salesDocumentDao(): SalesDocumentDao

    companion object {
        @Volatile
        private var instance: RestoHubDatabase? = null

        fun getInstance(context: Context): RestoHubDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    RestoHubDatabase::class.java,
                    "restohub.db"
                )
                    // Para el proyecto académico se recrea la base cuando cambia el esquema.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
