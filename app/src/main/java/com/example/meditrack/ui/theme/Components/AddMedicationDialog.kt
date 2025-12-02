import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.shared.ColorConstants
import com.example.shared.Medication

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddMedicationDialog(onDismiss: () -> Unit, onSave: (Medication) -> Unit) {
    var medicationName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var selectedFrequency by remember { mutableStateOf("Daily") }
    val frequencies = listOf("Daily", "Weekly", "Twice a week")

    // State for day selection
    val weekDays = listOf("S", "M", "T", "W", "T", "F", "S")
    val selectedDays = remember { mutableStateListOf<Int>() }
    var dailyFrequency by remember { mutableStateOf(1) }
    val medicationTimes = remember { mutableStateListOf("09:00") }


    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Add New Medication", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = medicationName,
                    onValueChange = { medicationName = it },
                    label = { Text("Medication Name") },
                    placeholder = { Text("e.g., Aspirin") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage") },
                    placeholder = { Text("e.g., 81mg") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Instructions") },
                    placeholder = { Text("e.g., Take with food") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("Frequency", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    frequencies.forEach { frequency ->
                        val isSelected = selectedFrequency == frequency
                        OutlinedButton(
                            onClick = {
                                selectedFrequency = frequency
                                selectedDays.clear() // Clear days when frequency changes
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isSelected) ColorConstants.PrimaryBlue else Color.LightGray),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) ColorConstants.PrimaryBlue.copy(alpha = 0.1f) else Color.Transparent,
                                contentColor = if (isSelected) ColorConstants.PrimaryBlue else Color.Gray
                            )
                        ) {
                            Text(frequency)
                        }
                    }
                }

                AnimatedVisibility(visible = selectedFrequency == "Daily") {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("How many times a day?", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            (1..3).forEach { times ->
                                val isSelected = dailyFrequency == times
                                OutlinedButton(
                                    onClick = {
                                        dailyFrequency = times
                                        // Adjust the list of times
                                        while (medicationTimes.size < times) medicationTimes.add("09:00")
                                        while (medicationTimes.size > times) medicationTimes.removeAt(medicationTimes.lastIndex)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, if (isSelected) ColorConstants.PrimaryBlue else Color.LightGray),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSelected) ColorConstants.PrimaryBlue.copy(alpha = 0.1f) else Color.Transparent,
                                        contentColor = if (isSelected) ColorConstants.PrimaryBlue else Color.Gray
                                    )
                                ) {
                                    Text("$times time${if (times > 1) "s" else ""}")
                                }
                            }
                        }
                    }
                }

                // Show day selector for "Weekly" or "Twice a week"
                AnimatedVisibility(visible = selectedFrequency == "Weekly" || selectedFrequency == "Twice a week") {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedFrequency == "Weekly") "Select One Day" else "Select Two Days",
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            weekDays.forEachIndexed { index, day ->
                                val isDaySelected = selectedDays.contains(index)
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isDaySelected) ColorConstants.PrimaryBlue else Color.LightGray.copy(
                                                alpha = 0.5f
                                            )
                                        )
                                        .clickable {
                                            if (isDaySelected) {
                                                selectedDays.remove(index)
                                            } else {
                                                // Apply selection limit based on frequency
                                                val limit = if (selectedFrequency == "Weekly") 1 else 2
                                                if (selectedDays.size < limit) {
                                                    selectedDays.add(index)
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day,
                                        color = if (isDaySelected) Color.White else Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Time Pickers
                Text("Time", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    medicationTimes.forEachIndexed { index, time ->
                        OutlinedTextField(
                            value = time,
                            onValueChange = { medicationTimes[index] = it },
                            label = { Text("Time ${index + 1}") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }


                Spacer(modifier = Modifier.weight(1f, fill = false))
                Spacer(modifier = Modifier.height(32.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Black),
                        border = BorderStroke(1.dp, Color.Gray),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    // Updated save button logic
                    val isSaveEnabled = medicationName.isNotBlank() && dosage.isNotBlank() &&
                            (selectedFrequency == "Daily" ||
                                    (selectedFrequency == "Weekly" && selectedDays.size == 1) ||
                                    (selectedFrequency == "Twice a week" && selectedDays.size == 2))

                    Button(
                        onClick = {
                            val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                            val frequencyText = when (selectedFrequency) {
                                "Weekly" -> "Weekly on ${dayNames[selectedDays.first()]}"
                                "Twice a week" -> "Twice a week (${selectedDays.sorted().joinToString { dayNames[it] }})"
                                "Daily" -> {
                                    if (dailyFrequency > 1) {
                                        "Daily, $dailyFrequency times"
                                    } else {
                                        "Daily"
                                    }
                                }
                                else -> selectedFrequency
                            }
                            val newMed = Medication(
                                name = medicationName,
                                dosage = dosage,
                                frequency = frequencyText,
                                instructions = instructions,
                                scheduledTimes = medicationTimes.toList()
                            )
                            onSave(newMed)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111111)),
                        modifier = Modifier.weight(1f),
                        enabled = isSaveEnabled
                    ) {
                        Text("Save Medication")
                    }
                }
            }
        }
    }
}
