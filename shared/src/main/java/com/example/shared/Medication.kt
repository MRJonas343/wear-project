import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class MedicationStatus {
    PENDING, TAKEN, SKIPPED, SNOOZED
}

data class Medication(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val dosage: String,
    val frequency: String,
    val scheduledTimes: List<String>,
    val instructions: String,
    val startDate: String = "",
    val endDate: String = "",
    val status: MedicationStatus = MedicationStatus.PENDING
)

object MedicationRepository {
    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    val medications: StateFlow<List<Medication>> = _medications.asStateFlow()

    /**
     * Callback invoked whenever the medication list changes.
     *
     * This is used by the Wearable Data Layer sync services to detect changes
     * and trigger synchronization. When medications are added or updated on one device,
     * the sync service uses this callback to push changes via DataClient.
     *
     * Example usage in sync service:
     * ```
     * MedicationRepository.setSyncCallback { medications ->
     *     syncToWearable(medications) // Push to DataClient
     * }
     * ```
     */
    private var onDataChanged: ((List<Medication>) -> Unit)? = null

    init {
        // Initializing with mock data
        _medications.value = MockData.sampleMedications
    }

    /**
     * Set a callback to be notified when medication data changes.
     * This is used by WearDataSyncService to trigger synchronization.
     */
    fun setSyncCallback(callback: (List<Medication>) -> Unit) {
        onDataChanged = callback
        // Immediately invoke with current data to perform initial sync
        callback(_medications.value)
    }

    fun addMedication(medication: Medication) {
        val currentList = _medications.value.toMutableList()
        currentList.add(medication)
        _medications.value = currentList
        // Notify sync service that data changed
        onDataChanged?.invoke(currentList)
    }

    // Generic function to update medication status
    fun updateMedicationStatus(id: String, newStatus: MedicationStatus) {
        val currentList = _medications.value.map {
            if (it.id == id) {
                // For snooze, you might want to add logic to re-trigger the reminder later
                it.copy(status = newStatus)
            } else {
                it
            }
        }
        _medications.value = currentList
        // Notify sync service that data changed
        onDataChanged?.invoke(currentList)
    }

    /**
     * Update the entire medication list from external source (e.g., synced from phone).
     *
     * This is called by the watch's sync service when it receives medication data
     * from the phone via DataClient. We don't trigger the sync callback here to
     * avoid infinite sync loops.
     */
    fun updateFromSync(medications: List<Medication>) {
        _medications.value = medications
        // Don't call onDataChanged here to avoid sync loop
    }
}

// MockData remains the same but now includes instructions
object MockData {
    val sampleMedications = listOf(
        Medication(
            name = "Lisinopril",
            dosage = "10mg",
            frequency = "Daily",
            scheduledTimes = listOf("18:00"),
            instructions = "Take with a full glass of water.",
            status = MedicationStatus.PENDING
        ),
        Medication(
            name = "Metformin",
            dosage = "500mg",
            frequency = "Twice Daily",
            scheduledTimes = listOf("08:00", "20:00"),
            instructions = "Take 30 minutes after breakfast.",
            status = MedicationStatus.TAKEN
        )
    )
}
