package Components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.shared.ColorConstants
import com.example.shared.Medication


@Composable
fun WearAddMedicationScreen(onSave: (Medication) -> Unit, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("Daily") }
    val times = remember { mutableStateListOf("09:00") }

    ScalingLazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        item { PageHeader("Add Medication", onBack) }
        item { Spacer(Modifier.height(8.dp)) }

        item { AppTextField(name, { name = it }, "Name") }
        item { AppTextField(dosage, { dosage = it }, "Dosage (e.g., 1 pill)") }

        item { Text("Frequency", style = MaterialTheme.typography.caption1, modifier = Modifier.padding(top = 8.dp)) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Daily", "Weekly").forEach { freq ->
                    Chip(
                        onClick = { frequency = freq },
                        label = { Text(freq) },
                        colors = ChipDefaults.chipColors(backgroundColor = if (frequency == freq) ColorConstants.PrimaryBlue else MaterialTheme.colors.surface)
                    )
                }
            }
        }

        item { Text("Time", style = MaterialTheme.typography.caption1, modifier = Modifier.padding(top = 8.dp)) }
        item { AppTextField(times.first(), { times[0] = it }, "Time (HH:MM)") }

        item {
            Button(onClick = {
                val newMed = Medication(name = name, dosage = dosage, frequency = frequency, scheduledTimes = times.toList(), instructions = "")
                onSave(newMed)
            }, enabled = name.isNotBlank() && dosage.isNotBlank()) {
                Icon(Icons.Default.Check, null)
                Spacer(Modifier.width(4.dp))
                Text("Save")
            }
        }
    }
}