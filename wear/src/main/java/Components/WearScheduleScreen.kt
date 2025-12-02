package Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.shared.ColorConstants
import com.example.shared.Medication


@Composable
fun WearScheduleScreen(medications: List<Medication>, onBack: () -> Unit, onMedicationClick: (Medication) -> Unit) {
    val listState = rememberScalingLazyListState()
    val grouped = medications.sortedBy { it.scheduledTimes.firstOrNull() }
        .groupBy { it.frequency.split(",").first().trim() }

    ScalingLazyColumn(modifier = Modifier.fillMaxSize().background(Color.Black), state = listState) {
        item { PageHeader("Full Schedule", onBack) }

        grouped.forEach { (frequency, meds) ->
            item { Text(frequency, style = MaterialTheme.typography.caption1, color = ColorConstants.PrimaryBlue, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) }
            items(meds) { med -> WearMedicationCard(med, onMedicationClick) }
        }
    }
}