package com.example.apprestaurante.core

import android.content.Context
import com.example.apprestaurante.RestoHubApplication

val Context.app: RestoHubApplication
    get() = applicationContext as RestoHubApplication
