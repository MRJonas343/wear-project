package com.example.wear

import android.content.Context
import android.util.Log
import com.example.shared.MedicationRepository
import com.example.shared.WearableConstants
import com.example.shared.parseMedicationsFromJson
import com.google.android.gms.wearable.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class WearDataSyncService(private val context: Context) {

    private val dataClient: DataClient by lazy { Wearable.getDataClient(context) }
    private val messageClient: MessageClient by lazy { Wearable.getMessageClient(context) }
    private val nodeClient: NodeClient by lazy { Wearable.getNodeClient(context) }
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val dataListener = DataClient.OnDataChangedListener { dataEvents ->
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                if (dataItem.uri.path == WearableConstants.MEDICATION_DATA_PATH) {
                    Log.d(TAG, "Received medication data from phone via DataClient")
                    val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                    val medicationsJson = dataMap.getString(WearableConstants.KEY_MEDICATIONS_JSON)

                    if (medicationsJson != null) {
                        try {
                            val medications = parseMedicationsFromJson(medicationsJson)
                            Log.d(TAG, "Parsed ${medications.size} medications from phone")
                            MedicationRepository.updateFromSync(medications)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to parse medication data", e)
                        }
                    }
                }
            }
        }
    }

    fun start() {
        Log.d(TAG, "Starting WearDataSyncService")
        dataClient.addListener(dataListener)
    }

    fun stop() {
        Log.d(TAG, "Stopping WearDataSyncService")
        dataClient.removeListener(dataListener)
        serviceScope.cancel()
    }

    suspend fun sendMedicationAction(medicationId: String, action: MedicationAction) {
        try {
            Log.d(TAG, "Sending $action action for medication $medicationId to phone")
            val nodes = nodeClient.connectedNodes.await()
            if (nodes.isEmpty()) {
                Log.w(TAG, "No connected nodes found. Is phone connected?")
                return
            }
            val phoneNode = nodes.first()
            val messagePath = when (action) {
                MedicationAction.TAKE -> WearableConstants.MESSAGE_TAKE_MEDICATION
                MedicationAction.SKIP -> WearableConstants.MESSAGE_SKIP_MEDICATION
                MedicationAction.SNOOZE -> WearableConstants.MESSAGE_SNOOZE_MEDICATION
            }
            val messageData = medicationId.toByteArray()
            messageClient.sendMessage(phoneNode.id, messagePath, messageData).await()
            Log.d(TAG, "Successfully sent $action action to phone")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send action message to phone", e)
            throw e
        }
    }

    companion object {
        private const val TAG = "WearDataSyncService"
    }
}

enum class MedicationAction {
    TAKE,
    SKIP,
    SNOOZE
}
