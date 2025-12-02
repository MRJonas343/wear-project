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

    init {
        // Initializing with mock data
        _medications.value = MockData.sampleMedications
    }

    fun addMedication(medication: Medication) {
        val currentList = _medications.value.toMutableList()
        currentList.add(medication)
        _medications.value = currentList
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

