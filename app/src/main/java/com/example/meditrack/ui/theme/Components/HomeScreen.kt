import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel // Icon for Missed
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shared.ColorConstants
import com.example.shared.Medication
import com.example.shared.MedicationRepository
import com.example.shared.MedicationStatus


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onMedicationClick: (Medication) -> Unit
) {
    val medications by MedicationRepository.medications.collectAsState()

    // 1. Filter medications by all relevant statuses
    val upcomingMedications = medications.filter { it.status == MedicationStatus.PENDING }
    val completedMedications = medications.filter { it.status == MedicationStatus.TAKEN }
    val missedMedications = medications.filter { it.status == MedicationStatus.SKIPPED }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(ColorConstants.HeaderGradient)
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Good evening Arturo", color = Color.White, fontSize = 20.sp)
                            Text("Today's medication schedule", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            val totalTracked = medications.count { it.status != MedicationStatus.PENDING && it.status != MedicationStatus.SNOOZED }
                            val adherence = if (totalTracked > 0) {
                                (completedMedications.size.toFloat() / totalTracked.toFloat() * 100).toInt()
                            } else 0
                            Text("$adherence%", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            Text("Adherence", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Status Cards
        item {
            // 2. Updated Row with three StatusCards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatusCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CheckCircle,
                    count = completedMedications.size.toString(),
                    label = "Completed",
                    color = ColorConstants.SuccessGreen,
                    progress = (completedMedications.size.toFloat() / medications.size.coerceAtLeast(1).toFloat())
                )
                StatusCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Warning,
                    count = upcomingMedications.size.toString(),
                    label = "Due Now",
                    color = ColorConstants.AlertOrange,
                    progress = (upcomingMedications.size.toFloat() / medications.size.coerceAtLeast(1).toFloat())
                )
                // 3. Add the "Missed" card
                StatusCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Cancel,
                    count = missedMedications.size.toString(),
                    label = "Missed",
                    color = ColorConstants.ErrorRed,
                    progress = (missedMedications.size.toFloat() / medications.size.coerceAtLeast(1).toFloat())
                )
            }
        }

        val groupedMedications = upcomingMedications
            .sortedBy { it.scheduledTimes.firstOrNull() }
            .groupBy { it.frequency.split(",").first().trim() } // Group by "Daily", "Weekly", etc.

        groupedMedications.forEach { (frequency, meds) ->
            item {
                Text(
                    text = "$frequency Schedule",
                    color = ColorConstants.PrimaryBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                )
            }
            items(meds) { medication ->
                MedicationListItem(medication, onMedicationClick)
            }
        }
    }
}

@Composable
private fun MedicationListItem(medication: Medication, onMedicationClick: (Medication) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMedicationClick(medication) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(medication.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(medication.dosage, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    medication.scheduledTimes.joinToString(", "),
                    color = ColorConstants.PrimaryBlue, 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = medication.frequency.substringAfter("on ").ifEmpty { "Everyday" },
                    color = Color.Gray, 
                    fontSize = 12.sp
                )
            }
        }
    }
}