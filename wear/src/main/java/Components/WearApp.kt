package Components

import Medication
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import com.example.shared.ColorConstants
import com.example.wear.WearDataSyncService

@Composable
fun WearApp(syncService: WearDataSyncService? = null) {
    val medications by MedicationRepository.medications.collectAsState()
    var currentScreen by remember { mutableStateOf(WearScreen.HOME) }
    var activeMedication by remember { mutableStateOf<Medication?>(null) }

    MaterialTheme(
        colors = Colors(
            primary = ColorConstants.PrimaryBlue,
            background = Color.Black,
            surface = Color(0xFF202124),
            onPrimary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White,
            secondary = Color.White,
            onSecondary = Color.Black,
            error = ColorConstants.ErrorRed,
            onError = Color.White
        )
    ) {
        Scaffold(timeText = { TimeText() }) {
            when (currentScreen) {
                WearScreen.HOME -> WearHomeScreen(
                    medications = medications,
                    onNavigate = { screen -> currentScreen = screen },
                    onMedicationClick = { med ->
                        activeMedication = med
                        currentScreen = WearScreen.REMINDER
                    }
                )
                WearScreen.SCHEDULE -> WearScheduleScreen(
                    medications = medications,
                    onBack = { currentScreen = WearScreen.HOME },
                    onMedicationClick = { med ->
                        activeMedication = med
                        currentScreen = WearScreen.REMINDER
                    }
                )
                WearScreen.ADD_MEDICATION -> WearAddMedicationScreen(
                    onSave = {
                        MedicationRepository.addMedication(it)
                        currentScreen = WearScreen.HOME
                    },
                    onBack = { currentScreen = WearScreen.HOME }
                )
                WearScreen.REMINDER -> {
                    val service = syncService // Explicitly capture
                    activeMedication?.let {
                        WearReminderScreen(
                            medication = it,
                            syncService = service,
                            onAction = {
                                activeMedication = null
                                currentScreen = WearScreen.HOME
                            }
                        )
                    } ?: run { currentScreen = WearScreen.HOME } // Fallback
                }
            }
        }
    }
}
