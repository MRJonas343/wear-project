package Components

import Medication
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.shared.ColorConstants

@Composable
fun WearReminderScreen(medication: Medication, onAction: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF501810), Color.Black), radius = 400f))) {
        ScalingLazyColumn(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            item { Icon(Icons.Default.Warning, null, tint = Color(0xFFFF6D00), modifier = Modifier.size(48.dp).padding(bottom = 8.dp)) }
            item { Text(medication.name, style = MaterialTheme.typography.title2, textAlign = TextAlign.Center) }
            item { Text(medication.dosage, style = MaterialTheme.typography.body2, color = Color.Gray, textAlign = TextAlign.Center) }
            item { Text("Due at ${medication.scheduledTimes.firstOrNull() ?: "Now"}", color = Color(0xFFFF9800), style = MaterialTheme.typography.caption1) }
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.Center) {
                    ActionButton(Icons.Default.Check, ColorConstants.SuccessGreen) { MedicationRepository.updateMedicationStatus(medication.id, MedicationStatus.TAKEN); onAction() }
                    Spacer(modifier = Modifier.width(12.dp))
                    ActionButton(Icons.Default.Schedule, ColorConstants.AlertOrange) { MedicationRepository.updateMedicationStatus(medication.id, MedicationStatus.SNOOZED); onAction() }
                    Spacer(modifier = Modifier.width(12.dp))
                    ActionButton(Icons.Default.Close, ColorConstants.ErrorRed) { MedicationRepository.updateMedicationStatus(medication.id, MedicationStatus.SKIPPED); onAction() }
                }
            }
        }
    }
}