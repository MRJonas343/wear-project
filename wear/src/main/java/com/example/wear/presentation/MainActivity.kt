import Components.WearApp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.wear.WearDataSyncService

class MainActivity : ComponentActivity() {

    /**
     * Wearable Data Layer sync service.
     *
     * This service handles receiving medication data from the phone:
     * - Listens for medication updates via DataClient
     * - Sends quick actions to phone via MessageClient
     *
     * Initialized in onCreate() and cleaned up in onDestroy().
     */
    private lateinit var syncService: WearDataSyncService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Wearable sync service
        // This starts listening for medication data from phone
        syncService = WearDataSyncService(this)
        syncService.start()

        setContent {
            WearApp(syncService = syncService)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up sync service to avoid memory leaks
        syncService.stop()
    }
}
