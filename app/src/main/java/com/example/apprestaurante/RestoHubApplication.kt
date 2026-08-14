package com.example.apprestaurante

import android.app.Application
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RestoHubApplication : Application() {
    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // La carga inicial se ejecuta fuera del hilo de la interfaz para evitar ANR.
        applicationScope.launch {
            runCatching { container.initialize() }
                .onFailure { Log.e("RestoHub", "No se pudieron crear los datos iniciales", it) }
        }
    }
}
