import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun BottomNavBar() {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.DateRange, null) }, label = { Text("Schedule") })
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Star, null) }, label = { Text("Analytics") })
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profile") })
    }
}