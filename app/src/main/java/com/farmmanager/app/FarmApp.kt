package com.farmmanager.app

import android.app.Application
import com.farmmanager.app.data.AppDatabase
import com.farmmanager.app.data.repository.FarmRepository

class FarmApp : Application() {
    lateinit var repository: FarmRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(this)
        repository = FarmRepository(db)
    }
}
