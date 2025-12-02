package Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.shared.ColorConstants
import com.example.shared.Medication
import com.example.shared.MedicationStatus

@Composable
fun WearHomeScreen(
    medications: List<Medication>,
    onNavigate: (WearScreen) -> Unit,
    onMedicationClick: (Medication) -> Unit
) {
    val listState = rememberScalingLazyListState()
    val completedCount = medications.count { it.status == MedicationStatus.TAKEN }
    val pendingCount = medications.count { it.status == MedicationStatus.PENDING }
    val progress = if (medications.isNotEmpty()) completedCount.toFloat() / medications.size else 0f

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        state = listState,
        anchorType = ScalingLazyListAnchorType.ItemStart
    ) {
        item { Text("MedicTrack", style = MaterialTheme.typography.title3, modifier = Modifier.padding(bottom = 8.dp)) }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                StatusChip(Icons.Default.CheckCircle, completedCount.toString(), "Taken", ColorConstants.SuccessGreen, Modifier.weight(1f).padding(end = 4.dp))
                StatusChip(Icons.Default.Warning, pendingCount.toString(), "Due", ColorConstants.AlertOrange, Modifier.weight(1f).padding(start = 4.dp))
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1A1A1A)).padding(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Today's Progress", fontSize = 12.sp, color = Color.White)
                    Text("${(progress * 100).toInt()}%", fontSize = 12.sp, color = ColorConstants.PrimaryBlue)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = ColorConstants.PrimaryBlue
                )
            }
        }

        val upcoming = medications.filter { it.status == MedicationStatus.PENDING }.sortedBy { it.scheduledTimes.firstOrNull() }
        if (upcoming.isNotEmpty()) {
            item { Text("Upcoming", style = MaterialTheme.typography.body2, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) }
            items(upcoming) { med ->
                WearMedicationCard(med, onMedicationClick)
            }
        }


        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavIcon(Icons.Default.Home, true) {}
                NavIcon(Icons.Default.DateRange, false) { onNavigate(WearScreen.SCHEDULE) }
                NavIcon(Icons.Default.Add, false) { onNavigate(WearScreen.ADD_MEDICATION) }
                NavIcon(Icons.Default.Settings, false) {}
            }
        }
    }
}