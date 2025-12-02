package com.example.shared

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

    private var onDataChanged: ((List<Medication>) -> Unit)? = null

    init {
        // Initializing with mock data
        _medications.value = MockData.sampleMedications
    }

    fun setSyncCallback(callback: (List<Medication>) -> Unit) {
        onDataChanged = callback
        callback(_medications.value)
    }

    fun addMedication(medication: Medication) {
        val currentList = _medications.value.toMutableList()
        currentList.add(medication)
        _medications.value = currentList
        onDataChanged?.invoke(currentList)
    }

    fun updateMedicationStatus(id: String, newStatus: MedicationStatus) {
        val currentList = _medications.value.map {
            if (it.id == id) {
                it.copy(status = newStatus)
            } else {
                it
            }
        }
        _medications.value = currentList
        onDataChanged?.invoke(currentList)
    }

    fun updateFromSync(medications: List<Medication>) {
        _medications.value = medications
    }
}

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
