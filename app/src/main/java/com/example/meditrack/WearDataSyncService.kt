package com.example.meditrack

import android.content.Context
import android.util.Log
import com.example.shared.Medication
import com.example.shared.MedicationRepository
import com.example.shared.MedicationStatus
import com.example.shared.WearableConstants
import com.example.shared.toJsonString
import com.google.android.gms.wearable.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class WearDataSyncService(private val context: Context) {

    private val dataClient: DataClient by lazy { Wearable.getDataClient(context) }
    private val messageClient: MessageClient by lazy { Wearable.getMessageClient(context) }
    private val nodeClient: NodeClient by lazy { Wearable.getNodeClient(context) }
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val messageListener = MessageClient.OnMessageReceivedListener { messageEvent ->
        Log.d(TAG, "Received message from watch: ${messageEvent.path}")
        val medicationId = String(messageEvent.data)
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

    fun start() {
        Log.d(TAG, "Starting WearDataSyncService")
        messageClient.addListener(messageListener)
        MedicationRepository.setSyncCallback { medications ->
            syncMedicationsToWatch(medications)
        }
    }

    fun stop() {
        Log.d(TAG, "Stopping WearDataSyncService")
        messageClient.removeListener(messageListener)
        serviceScope.cancel()
    }

    private fun syncMedicationsToWatch(medications: List<Medication>) {
        serviceScope.launch {
            try {
                Log.d(TAG, "Syncing ${medications.size} medications to watch via DataClient")
                val medicationsJson = medications.toJsonString()
                val putDataReq = PutDataMapRequest.create(WearableConstants.MEDICATION_DATA_PATH).apply {
                    dataMap.putString(WearableConstants.KEY_MEDICATIONS_JSON, medicationsJson)
                    dataMap.putLong(WearableConstants.KEY_TIMESTAMP, System.currentTimeMillis())
                }
                val request = putDataReq.asPutDataRequest().setUrgent()
                val dataItem = dataClient.putDataItem(request).await()
                Log.d(TAG, "Successfully synced medications to watch. DataItem URI: ${dataItem.uri}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync medications to watch", e)
            }
        }
    }

    companion object {
        private const val TAG = "WearDataSyncService"
    }
}