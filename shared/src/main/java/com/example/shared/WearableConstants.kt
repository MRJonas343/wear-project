package com.example.shared

/**
 * Constants for Wearable Data Layer API communication between phone and watch.
 *
 * The Wearable Data Layer uses path-based routing to identify different types of data and messages:
 *
 * - **DataClient paths**: Used for persistent data synchronization. Data is automatically synced
 *   when devices reconnect. Paths must start with "/" and uniquely identify the data type.
 *
 * - **MessageClient paths**: Used for one-way, transient messages. Messages are only delivered
 *   if the target device is connected. Paths identify the message type/action.
 */
object WearableConstants {

    // ==================== DataClient Paths ====================

    /**
     * DataClient path for medication list synchronization.
     *
     * This path is used to store the complete medication list in the Wearable Data Layer.
     * When the phone updates medications, it publishes a DataItem at this path.
     * The watch listens for changes to this path and updates its local repository.
     *
     * DataClient automatically handles:
     * - Syncing when devices reconnect after being offline
     * - Conflict resolution (last write wins)
     * - Efficient delta updates
     */
    const val MEDICATION_DATA_PATH = "/medications"


    // ==================== MessageClient Paths ====================

    /**
     * MessageClient path for "Take Medication" action from watch.
     *
     * When user taps the green checkmark on the watch, a message is sent to this path
     * with the medication ID as payload. The phone receives this message and updates
     * the medication status to TAKEN.
     *
     * MessageClient is used here because:
     * - It's a quick, one-time action (not persistent data)
     * - We want immediate feedback (not eventual consistency)
     * - The action only matters if the phone is connected
     */
    const val MESSAGE_TAKE_MEDICATION = "/action/take"

    /**
     * MessageClient path for "Skip Medication" action from watch.
     *
     * When user taps the red X on the watch, a message is sent to this path.
     * The phone updates the medication status to SKIPPED.
     */
    const val MESSAGE_SKIP_MEDICATION = "/action/skip"

    /**
     * MessageClient path for "Snooze Medication" action from watch.
     *
     * When user taps the orange clock on the watch, a message is sent to this path.
     * The phone updates the medication status to SNOOZED.
     */
    const val MESSAGE_SNOOZE_MEDICATION = "/action/snooze"


    // ==================== Data Keys ====================

    /**
     * Key for storing serialized medication list in DataItem.
     *
     * DataClient stores data as key-value pairs in a DataMap. This key identifies
     * the JSON string containing the complete medication list.
     */
    const val KEY_MEDICATIONS_JSON = "medications_json"

    /**
     * Key for medication ID in message payloads.
     *
     * When sending action messages via MessageClient, we include the medication ID
     * so the phone knows which medication to update.
     */
    const val KEY_MEDICATION_ID = "medication_id"

    /**
     * Key for timestamp in data synchronization.
     *
     * Including a timestamp ensures DataClient detects changes even if the medication
     * list content is identical. This triggers sync listeners on the watch.
     */
    const val KEY_TIMESTAMP = "timestamp"
}
