package com.example.wear

import android.content.Context
import android.util.Log
import com.example.shared.*
import com.google.android.gms.wearable.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await // Extension function for Task.await()
import MedicationRepository

/**
 * Service responsible for synchronizing medication data between watch and phone.
 *
 * This is the **watch-side** sync service that demonstrates all three Wearable APIs:
 * 1. **DataClient** - Receives medication list from phone (persistent, automatic)
 * 2. **MessageClient** - Sends quick actions to phone (transient, real-time)
 * 3. **Coroutines** - Handles async operations cleanly without callbacks
 *
 * ## Architecture Overview
 *
 * Watch (this service)                         Phone
 * ┌─────────────────────┐                     ┌─────────────────────┐
 * │ WearReminderScreen  │                     │ MedicationRepository│
 * │        ↓            │                     │        ↑            │
 * │  sendActionMessage  │                     │  updateStatus       │
 * │        ↓            │                     │        ↑            │
 * │ WearDataSyncService │ ──MessageClient──→  │ WearDataSyncService │
 * │        ↑            │                     │        ↓            │
 * │    DataClient       │ ←──DataClient─────  │    DataClient       │
 * │        ↑            │                     └─────────────────────┘
 * │ MedicationRepository│
 * └─────────────────────┘
 *         ↑
 *    Receives medication
 *    data from phone
 */
class WearDataSyncService(private val context: Context) {

    // ==================== Wearable API Clients ====================

    /**
     * DataClient - Used for receiving persistent data from phone.
     *
     * On the watch, DataClient:
     * - Listens for medication updates from phone
     * - Automatically syncs when reconnecting after offline
     * - Stores data locally for offline access
     */
    private val dataClient: DataClient by lazy { Wearable.getDataClient(context) }

    /**
     * MessageClient - Used for sending transient messages to phone.
     *
     * We use MessageClient to send quick actions (Take/Skip/Snooze)
     * because these are one-time events that don't need persistence.
     */
    private val messageClient: MessageClient by lazy { Wearable.getMessageClient(context) }

    /**
     * NodeClient - Used for discovering the connected phone.
     *
     * We need the phone's node ID to send messages to it.
     */
    private val nodeClient: NodeClient by lazy { Wearable.getNodeClient(context) }


    // ==================== Coroutine Scope ====================

    /**
     * Coroutine scope for async operations.
     *
     * **Why Coroutines?**
     * - Wearable APIs are callback-based (Google Play Services Tasks)
     * - Coroutines + .await() = clean sequential code
     * - Automatic cancellation when service stops
     * - Better error handling than callbacks
     *
     * Example: Sending a message with coroutines
     * ```
     * suspend fun sendMessage() {
     *     val nodes = nodeClient.connectedNodes.await()
     *     val phoneNode = nodes.firstOrNull()
     *     phoneNode?.let {
     *         messageClient.sendMessage(it.id, path, data).await()
     *     }
     * }
     * ```
     */
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)


    // ==================== DataClient Listener ====================

    /**
     * Listener for data changes from the phone.
     *
     * This demonstrates **DataClient** receiving data.
     * When the phone updates medications, this listener is automatically invoked.
     *
     * **Data Flow:**
     * 1. User adds medication on phone
     * 2. Phone's WearDataSyncService publishes to DataClient
     * 3. Data Layer syncs to watch
     * 4. This listener receives the update
     * 5. We parse the data and update local repository
     * 6. UI automatically updates via StateFlow
     */
    private val dataListener = DataClient.OnDataChangedListener { dataEvents ->
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem

                // Check if this is medication data
                if (dataItem.uri.path == WearableConstants.MEDICATION_DATA_PATH) {
                    Log.d(TAG, "Received medication data from phone via DataClient")

                    // Extract data from DataItem
                    val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                    val medicationsJson = dataMap.getString(WearableConstants.KEY_MEDICATIONS_JSON)

                    if (medicationsJson != null) {
                        try {
                            // Deserialize medications from JSON
                            val medications = parseMedicationsFromJson(medicationsJson)

                            Log.d(TAG, "Parsed ${medications.size} medications from phone")

                            // Update local repository
                            // Using updateFromSync() to avoid triggering sync loop
                            MedicationRepository.updateFromSync(medications)

                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to parse medication data", e)
                        }
                    }
                }
            }
        }
    }


    // ==================== Public Methods ====================

    /**
     * Start the sync service.
     *
     * This should be called from MainActivity.onCreate().
     * It registers the DataClient listener to receive medication updates.
     */
    fun start() {
        Log.d(TAG, "Starting WearDataSyncService")

        // Register DataClient listener to receive medication updates from phone
        dataClient.addListener(dataListener)
    }

    /**
     * Stop the sync service.
     *
     * This should be called from MainActivity.onDestroy().
     * It cleans up resources and cancels ongoing coroutines.
     */
    fun stop() {
        Log.d(TAG, "Stopping WearDataSyncService")

        // Unregister DataClient listener
        dataClient.removeListener(dataListener)

        // Cancel all coroutines
        serviceScope.cancel()
    }


    // ==================== MessageClient Actions ====================

    /**
     * Send a medication action message to the phone.
     *
     * This demonstrates **MessageClient** sending messages.
     *
     * **How MessageClient Works:**
     * 1. Find connected phone node using NodeClient
     * 2. Create message payload (medication ID as bytes)
     * 3. Send message to specific path (e.g., /action/take)
     * 4. Phone's MessageClient listener receives and processes
     *
     * **Why MessageClient for Actions?**
     * - Actions are one-time events (not persistent data)
     * - We want immediate feedback (not eventual consistency)
     * - Simpler than DataClient for simple commands
     * - Only matters if phone is connected (actions need immediate processing)
     *
     * **Coroutines in Action:**
     * Notice how we use `suspend` and `.await()` to write sequential code
     * without callback nesting. Much cleaner than:
     * ```
     * nodeClient.connectedNodes.addOnSuccessListener { nodes ->
     *     messageClient.sendMessage(...).addOnSuccessListener {
     *         // Nested callbacks...
     *     }
     * }
     * ```
     */
    suspend fun sendMedicationAction(medicationId: String, action: MedicationAction) {
        try {
            Log.d(TAG, "Sending $action action for medication $medicationId to phone")

            // Find connected phone node
            // The .await() extension converts Task<List<Node>> to suspend function
            val nodes = nodeClient.connectedNodes.await()

            if (nodes.isEmpty()) {
                Log.w(TAG, "No connected nodes found. Is phone connected?")
                return
            }

            // Get the first connected node (usually the phone)
            val phoneNode = nodes.first()

            // Determine message path based on action
            val messagePath = when (action) {
                MedicationAction.TAKE -> WearableConstants.MESSAGE_TAKE_MEDICATION
                MedicationAction.SKIP -> WearableConstants.MESSAGE_SKIP_MEDICATION
                MedicationAction.SNOOZE -> WearableConstants.MESSAGE_SNOOZE_MEDICATION
            }

            // Convert medication ID to bytes for message payload
            val messageData = medicationId.toByteArray()

            // Send message to phone
            // The .await() extension converts Task<Int> to suspend function
            messageClient.sendMessage(phoneNode.id, messagePath, messageData).await()

            Log.d(TAG, "Successfully sent $action action to phone")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to send action message to phone", e)
            throw e // Re-throw so UI can show error
        }
    }

    companion object {
        private const val TAG = "WearDataSyncService"
    }
}

/**
 * Enum representing medication actions that can be sent from watch to phone.
 */
enum class MedicationAction {
    TAKE,
    SKIP,
    SNOOZE
}
