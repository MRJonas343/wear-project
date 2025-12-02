package Components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import com.example.shared.ColorConstants

@Composable
fun NavIcon(icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick, shape = CircleShape, modifier = Modifier.size(40.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = if (selected) ColorConstants.PrimaryBlue else Color.DarkGray)
    ) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = Color.White)
    }
}