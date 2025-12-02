# MEDITRACK - 3-Minute Code Explanation

## 🎯 What is MEDITRACK?

A **multiplatform medication tracking app** that runs on both Android phones and Wear OS smartwatches, keeping medication data synchronized in real-time between devices.

---

## 🏗️ Architecture Overview

### Three-Module Structure

```
MEDITRACK/
├── app/          → Android phone app
├── wear/         → Wear OS watch app
└── shared/       → Common code (data models, constants)
```

**Why this structure?**
- **Shared module**: Medication data models used by both apps
- **Separation**: Phone and watch have different UIs but share business logic
- **Sync**: Both apps communicate via Wearable Data Layer APIs

---

## 🔄 Wearable API Synchronization

### The Three Key APIs

#### 1. **DataClient** - Persistent Data Sync
```kotlin
// Phone publishes medication list
dataClient.putDataItem(request).await()

// Watch receives automatically
dataClient.addListener { dataEvents ->
    // Parse and update local data
}
```

**Use case**: Syncing medication lists from phone to watch
- ✅ Automatic sync when devices reconnect
- ✅ Survives app restarts
- ✅ Efficient delta updates

#### 2. **MessageClient** - Quick Actions
```kotlin
// Watch sends "Take medication" action
messageClient.sendMessage(phoneNodeId, "/action/take", medicationId).await()

// Phone receives and processes
messageClient.addListener { messageEvent ->
    when (messageEvent.path) {
        "/action/take" -> updateMedicationStatus(id, TAKEN)
    }
}
```

**Use case**: Watch → Phone actions (Take/Skip/Snooze)
- ✅ Real-time, transient messages
- ✅ Fire-and-forget communication
- ✅ Only delivered if devices connected

#### 3. **Coroutines** - Clean Async Code
```kotlin
// Without coroutines (callback hell)
dataClient.putDataItem(req).addOnSuccessListener {
    nodeClient.connectedNodes.addOnSuccessListener { nodes ->
        // Nested callbacks...
    }
}

// With coroutines (sequential code)
val result = dataClient.putDataItem(req).await()
val nodes = nodeClient.connectedNodes.await()
```

**Benefit**: Converts callback-based APIs to readable sequential code

---

## 📊 Data Flow

### Phone → Watch (Adding Medication)

```
1. User adds medication on phone
   ↓
2. MedicationRepository.addMedication()
   ↓
3. Triggers sync callback
   ↓
4. WearDataSyncService serializes to JSON
   ↓
5. DataClient.putDataItem() publishes
   ↓
6. Watch DataClient listener receives
   ↓
7. Deserialize JSON → Update watch repository
   ↓
8. Watch UI updates via StateFlow
```

### Watch → Phone (Taking Medication)

```
1. User taps "Take" on watch
   ↓
2. WearReminderScreen.sendMedicationAction()
   ↓
3. MessageClient sends to "/action/take"
   ↓
4. Phone MessageClient listener receives
   ↓
5. Updates MedicationRepository status
   ↓
6. Triggers DataClient sync back to watch
   ↓
7. Watch shows updated status
```

---

## 🧩 Key Components

### Shared Module

**`Medication.kt`**
- Data class for medication info
- `MedicationRepository` - Single source of truth
- `setSyncCallback()` - Notifies when data changes
- `updateFromSync()` - Updates from external source (prevents loops)

**`WearableConstants.kt`**
- DataClient paths: `/medications`
- MessageClient paths: `/action/take`, `/action/skip`, `/action/snooze`
- Data keys for serialization

**`MedicationSerializer.kt`**
- JSON serialization helpers
- `toJsonString()` - List → JSON for DataClient
- `parseMedicationsFromJson()` - JSON → List

### Phone App

**`WearDataSyncService.kt`**
- **Publishes** medication data via DataClient
- **Receives** action messages via MessageClient
- Uses coroutines for async operations

**`MainActivity.kt`**
- Initializes sync service
- Lifecycle management (start/stop)

### Wear OS App

**`WearDataSyncService.kt`**
- **Receives** medication data via DataClient
- **Sends** action messages via MessageClient
- Uses coroutines for async operations

**`WearReminderScreen.kt`**
- UI for medication reminders
- Sends actions via MessageClient instead of direct updates
- Ensures phone remains source of truth

---

## 🎨 UI Architecture

### Compose-Based UI

Both apps use **Jetpack Compose** for declarative UI:

```kotlin
@Composable
fun WearApp(syncService: WearDataSyncService?) {
    val medications by MedicationRepository.medications.collectAsState()

    when (currentScreen) {
        WearScreen.HOME -> WearHomeScreen(medications)
        WearScreen.REMINDER -> WearReminderScreen(medication, syncService)
    }
}
```

**Pattern**: StateFlow → collectAsState() → UI auto-updates

---

## 🔑 Key Concepts Explained

### 1. Path-Based Routing
Wearable APIs use paths like URLs:
- `/medications` - DataClient path for medication list
- `/action/take` - MessageClient path for "take" action

### 2. JSON Serialization
DataClient stores primitives, so we serialize complex objects:
```kotlin
val json = medications.toJsonString()  // List → JSON
dataMap.putString("medications_json", json)
```

### 3. Sync Loop Prevention
```kotlin
// Phone: Triggers sync when data changes
fun addMedication(med: Medication) {
    _medications.value += med
    onDataChanged?.invoke(_medications.value)  // ✅ Trigger sync
}

// Watch: Updates from phone without triggering sync
fun updateFromSync(meds: List<Medication>) {
    _medications.value = meds
    // ❌ Don't call onDataChanged (prevents loop)
}
```

### 4. Coroutine Scopes
```kotlin
private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

fun stop() {
    serviceScope.cancel()  // Cancels all running coroutines
}
```

**SupervisorJob**: If one coroutine fails, others continue
**Dispatchers.IO**: Background thread for network operations

---

## 📱 User Experience Flow

1. **Add medication on phone** → Appears on watch in 2-3 seconds
2. **Watch shows reminder** → User taps "Take" button
3. **Action sent to phone** → Phone updates status
4. **Status syncs back** → Watch shows "Taken" immediately
5. **Offline?** → Data syncs automatically when reconnected

---

## 🛠️ Technical Highlights

### Dependency Injection Pattern
```kotlin
class WearDataSyncService(private val context: Context) {
    private val dataClient by lazy { Wearable.getDataClient(context) }
}
```

### Extension Functions
```kotlin
fun List<Medication>.toJsonString(): String { ... }
```

### Sealed Classes for Navigation
```kotlin
enum class WearScreen { HOME, SCHEDULE, REMINDER, ADD_MEDICATION }
```

### State Management
```kotlin
private val _medications = MutableStateFlow<List<Medication>>(emptyList())
val medications: StateFlow<List<Medication>> = _medications.asStateFlow()
```

---

## 🎓 Learning Takeaways

1. **DataClient** = Persistent sync, automatic reconnection
2. **MessageClient** = Real-time actions, requires connection
3. **Coroutines** = Clean async code with `.await()`
4. **JSON** = Simple serialization for DataClient
5. **Callbacks** = Prevent sync loops with separate update methods
6. **StateFlow** = Reactive UI updates with Compose

---

**Total Implementation**: 11 files, ~800 lines of well-commented code demonstrating production-ready Wearable API usage! 🚀
