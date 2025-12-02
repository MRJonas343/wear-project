package com.example.meditrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {

    /**
     * Wearable Data Layer sync service.
     *
     * This service handles bi-directional communication with the watch:
     * - Sends medication updates to watch via DataClient
     * - Receives action messages from watch via MessageClient
     *
     * Initialized in onCreate() and cleaned up in onDestroy().
     */
    private lateinit var syncService: WearDataSyncService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Wearable sync service
        // This starts listening for watch messages and sets up DataClient sync
        syncService = WearDataSyncService(this)
        syncService.start()

        setContent {
            MediTrackApp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up sync service to avoid memory leaks
        syncService.stop()
    }
}
