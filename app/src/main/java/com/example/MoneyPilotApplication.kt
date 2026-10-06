package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MoneyPilotApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Pre-initialize Room database instance and ensure default categories exist
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val count = db.categoryDao().getCount()
            if (count == 0) {
                AppDatabase.populateInitialCategories(db.categoryDao())
            }
        }
    }
}
