package Components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.shared.ColorConstants
import com.example.shared.Medication

@Composable
fun WearMedicationCard(med: Medication, onClick: (Medication) -> Unit) {
    Card(onClick = { onClick(med) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(med.name, style = MaterialTheme.typography.body1, fontWeight = FontWeight.Bold)
                Text(med.dosage, style = MaterialTheme.typography.caption2, color = Color.LightGray)
            }
            Text(med.scheduledTimes.firstOrNull() ?: "", style = MaterialTheme.typography.body2, color = ColorConstants.PrimaryBlue, fontWeight = FontWeight.Bold)
        }
    }
}