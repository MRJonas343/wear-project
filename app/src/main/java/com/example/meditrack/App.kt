package com.example.meditrack

import AddMedicationDialog
import BottomNavBar
import HomeScreen
import ReminderScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.shared.ColorConstants
import com.example.shared.Medication
import com.example.shared.MedicationRepository
import com.example.shared.MedicationStatus

@Composable
fun MediTrackApp() {
    var showAddDialog by remember { mutableStateOf(false) }
    var activeReminder by remember { mutableStateOf<Medication?>(null) }

    Scaffold(
        bottomBar = { BottomNavBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ColorConstants.PrimaryBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        containerColor = ColorConstants.BackgroundGray
    ) { padding ->
        HomeScreen(
            modifier = Modifier.padding(padding),
            onMedicationClick = { medication ->
                activeReminder = medication
            }
        )
    }

    if (showAddDialog) {
        AddMedicationDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newMedication ->
                MedicationRepository.addMedication(newMedication)
                showAddDialog = false
            }
        )
    }

    activeReminder?.let { med ->
        ReminderScreen(
            medication = med,
            onTaken = {
                MedicationRepository.updateMedicationStatus(med.id, MedicationStatus.TAKEN)
                activeReminder = null
            },
            onSnooze = {
                MedicationRepository.updateMedicationStatus(med.id, MedicationStatus.SNOOZED)
                activeReminder = null
            },
            onMissed = {
                MedicationRepository.updateMedicationStatus(med.id, MedicationStatus.SKIPPED)
                activeReminder = null
            }
        )
    }
}
