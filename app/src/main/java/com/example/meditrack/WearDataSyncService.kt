package com.example.meditrack

import android.content.Context
import android.util.Log
import com.example.shared.*
import com.google.android.gms.wearable.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await // Extension function for Task.await()
import MedicationRepository
import Medication

/**
 * Service responsible for synchronizing medication data between phone and watch.
 *
 * This service demonstrates all three key Wearable APIs:
 * 1. **DataClient** - Syncs medication list to watch (persistent, automatic)
 * 2. **MessageClient** - Receives quick actions from watch (transient, real-time)
 * 3. **Coroutines** - Handles async operations cleanly without callbacks
 *
 * ## Architecture Overview
 *
 * Phone (this service)                          Watch
 * ┌─────────────────────┐                      ┌─────────────────────┐
 * │ MedicationRepository│                      │ MedicationRepository│
 * │        ↓            │                      │        ↑            │
 * │  setSyncCallback    │                      │  updateFromSync     │
 * │        ↓            │                      │        ↑            │
 * │ WearDataSyncService │ ←──MessageClient───→ │ WearDataSyncService │
 * │        ↓            │                      │        ↑            │
 * │    DataClient       │ ───DataClient────→   │    DataClient       │
 * └─────────────────────┘                      └─────────────────────┘
 *         ↓                                             ↑
 *    Publishes medication                         Receives medication
 *    data to Data Layer                           data from Data Layer
 */
class WearDataSyncService(private val context: Context) {

    // ==================== Wearable API Clients ====================

    /**
     * DataClient - Used for persistent data synchronization.
     *
     * DataClient automatically:
     * - Syncs data when devices reconnect after being offline
     * - Handles conflict resolution (last write wins)
     * - Provides efficient delta updates
     * - Stores data persistently in the Wearable Data Layer
     */
    private val dataClient: DataClient by lazy { Wearable.getDataClient(context) }

    /**
     * MessageClient - Used for receiving transient messages from watch.
     *
     * MessageClient is ideal for:
     * - One-time actions (Take/Skip/Snooze medication)
     * - Real-time commands that don't need persistence
     * - Fire-and-forget communications
     */
    private val messageClient: MessageClient by lazy { Wearable.getMessageClient(context) }

    /**
     * NodeClient - Used for discovering connected devices.
     *
     * We use this to find the watch device ID when sending messages.
     */
    private val nodeClient: NodeClient by lazy { Wearable.getNodeClient(context) }


    // ==================== Coroutine Scope ====================

    /**
     * Coroutine scope for async operations.
     *
     * **Why Coroutines?**
     * - Wearable APIs return Google Play Services Tasks (callback-based)
     * - Coroutines let us use `.await()` to convert Tasks to sequential code
     * - Much cleaner than nested callbacks
     * - Automatic cancellation when service stops
     *
     * Example without coroutines (callback hell):
     * ```
     * dataClient.putDataItem(request).addOnSuccessListener {
     *     nodeClient.connectedNodes.addOnSuccessListener { nodes ->
     *         // Nested callbacks...
     *     }
     * }
     * ```
     *
     * Example with coroutines (clean sequential code):
     * ```
     * val result = dataClient.putDataItem(request).await()
     * val nodes = nodeClient.connectedNodes.await()
     * ```
     */
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)


    // ==================== Message Listener ====================

    /**
     * Listener for messages from the watch.
     *
     * This demonstrates **MessageClient** receiving messages.
     * When the watch sends a message (e.g., "Take medication"), this listener
     * is invoked with the message path and data.
     *
     * **Message Flow:**
     * 1. User taps "Take" button on watch
     * 2. Watch sends message to MESSAGE_TAKE_MEDICATION path with medication ID
     * 3. This listener receives the message
     * 4. We update the repository, which triggers DataClient sync back to watch
     */
    private val messageListener = MessageClient.OnMessageReceivedListener { messageEvent ->
        Log.d(TAG, "Received message from watch: ${messageEvent.path}")

        // Extract medication ID from message payload
        val medicationId = String(messageEvent.data)

        // Handle different message types based on path
        when (messageEvent.path) {
            WearableConstants.MESSAGE_TAKE_MEDICATION -> {
                Log.d(TAG, "Watch user marked medication as TAKEN: $medicationId")
                MedicationRepository.updateMedicationStatus(medicationId, MedicationStatus.TAKEN)
            }

            WearableConstants.MESSAGE_SKIP_MEDICATION -> {
                Log.d(TAG, "Watch user marked medication as SKIPPED: $medicationId")
                MedicationRepository.updateMedicationStatus(medicationId, MedicationStatus.SKIPPED)
            }

            WearableConstants.MESSAGE_SNOOZE_MEDICATION -> {
                Log.d(TAG, "Watch user marked medication as SNOOZED: $medicationId")
                MedicationRepository.updateMedicationStatus(medicationId, MedicationStatus.SNOOZED)
            }

            else -> {
                Log.w(TAG, "Unknown message path: ${messageEvent.path}")
            }
        }
    }


    // ==================== Public Methods ====================

    /**
     * Start the sync service.
     *
     * This should be called from MainActivity.onCreate().
     * It sets up:
     * 1. MessageClient listener for watch actions
     * 2. Repository callback for DataClient sync
     */
    fun start() {
        Log.d(TAG, "Starting WearDataSyncService")

        // Register MessageClient listener to receive messages from watch
        messageClient.addListener(messageListener)

        // Set up repository callback to sync via DataClient when data changes
        MedicationRepository.setSyncCallback { medications ->
            syncMedicationsToWatch(medications)
        }
    }

    /**
     * Stop the sync service.
     *
     * This should be called from MainActivity.onDestroy().
     * It cleans up resources and cancels ongoing coroutines.
     */
    fun stop() {
        Log.d(TAG, "Stopping WearDataSyncService")

        // Unregister MessageClient listener
        messageClient.removeListener(messageListener)

        // Cancel all coroutines
        serviceScope.cancel()
    }


    // ==================== DataClient Sync ====================

    /**
     * Sync medication list to watch using DataClient.
     *
     * This demonstrates **DataClient** publishing data.
     *
     * **How DataClient Works:**
     * 1. Create a PutDataRequest with a unique path
     * 2. Add data as key-value pairs in a DataMap
     * 3. Include a timestamp to ensure change detection
     * 4. Call dataClient.putDataItem() to publish
     * 5. Data Layer automatically syncs to all connected devices
     * 6. Watch's DataClient listener receives the update
     *
     * **Key Benefits:**
     * - Automatic sync when devices reconnect
     * - Efficient delta updates (only changed data is sent)
     * - Persistent storage (survives app restarts)
     * - Conflict resolution (last write wins)
     */
    private fun syncMedicationsToWatch(medications: List<Medication>) {
        serviceScope.launch {
            try {
                Log.d(TAG, "Syncing ${medications.size} medications to watch via DataClient")

                // Serialize medications to JSON
                val medicationsJson = medications.toJsonString()

                // Create a PutDataMapRequest for the medication data path
                val putDataReq = PutDataMapRequest.create(WearableConstants.MEDICATION_DATA_PATH).apply {
                    // Add medications as JSON string
                    dataMap.putString(WearableConstants.KEY_MEDICATIONS_JSON, medicationsJson)

                    // Add timestamp to ensure DataClient detects changes
                    // Without this, identical medication lists wouldn't trigger sync
                    dataMap.putLong(WearableConstants.KEY_TIMESTAMP, System.currentTimeMillis())
                }

                // Set urgent flag for immediate sync (optional)
                // Without this, sync might be delayed to save battery
                val request = putDataReq.asPutDataRequest().setUrgent()

                // Publish data to Wearable Data Layer
                // The .await() extension converts the Task to a suspend function
                // This is where **coroutines** make the code cleaner!
                val dataItem = dataClient.putDataItem(request).await()

                Log.d(TAG, "Successfully synced medications to watch. DataItem URI: ${dataItem.uri}")

            } catch (e: Exception) {
                // Handle errors (e.g., watch disconnected, API unavailable)
                Log.e(TAG, "Failed to sync medications to watch", e)
            }
        }
    }

    companion object {
        private const val TAG = "WearDataSyncService"
    }
}
