package com.example.wear

import Components.WearApp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {

    private lateinit var syncService: WearDataSyncService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        syncService = WearDataSyncService(this)
        syncService.start()

        setContent {
            WearApp(syncService = syncService)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        syncService.stop()
    }
}