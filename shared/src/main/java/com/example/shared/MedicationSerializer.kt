package com.example.shared

import org.json.JSONArray
import org.json.JSONObject
import Medication

/**
 * JSON serialization helpers for Medication data.
 *
 * These extensions enable converting Medication objects to/from JSON strings,
 * which is necessary for Wearable Data Layer synchronization.
 *
 * **Why JSON?**
 * - DataClient stores data as primitive types (String, Int, etc.) in DataMap
 * - We need to serialize complex Medication objects to strings
 * - JSON is lightweight and built into Android (no extra dependencies)
 *
 * **Alternative approaches:**
 * - Protocol Buffers (more efficient but requires extra setup)
 * - Kotlin Serialization (requires kotlinx.serialization dependency)
 * - Manual byte array serialization (error-prone)
 */

/**
 * Convert a Medication object to a JSON string.
 *
 * This is used when publishing medication data via DataClient.
 * The phone app calls this to serialize medications before sending to watch.
 *
 * Example usage:
 * ```
 * val medication = Medication(name = "Aspirin", dosage = "100mg", ...)
 * val json = medication.toJson()
 * // Result: {"id":"abc123","name":"Aspirin","dosage":"100mg",...}
 * ```
 */
fun Medication.toJson(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("name", name)
        put("dosage", dosage)
        put("frequency", frequency)
        put("scheduledTimes", JSONArray(scheduledTimes))
        put("instructions", instructions)
        put("startDate", startDate)
        put("endDate", endDate)
        put("status", status.name) // Convert enum to string
    }
}

/**
 * Convert a JSON string to a Medication object.
 *
 * This is used when receiving medication data via DataClient.
 * The watch app calls this to deserialize medications received from phone.
 *
 * Example usage:
 * ```
 * val json = JSONObject(jsonString)
 * val medication = json.toMedication()
 * ```
 */
fun JSONObject.toMedication(): Medication {
    val scheduledTimesArray = getJSONArray("scheduledTimes")
    val scheduledTimes = mutableListOf<String>()
    for (i in 0 until scheduledTimesArray.length()) {
        scheduledTimes.add(scheduledTimesArray.getString(i))
    }

    return Medication(
        id = getString("id"),
        name = getString("name"),
        dosage = getString("dosage"),
        frequency = getString("frequency"),
        scheduledTimes = scheduledTimes,
        instructions = getString("instructions"),
        startDate = getString("startDate"),
        endDate = getString("endDate"),
        status = MedicationStatus.valueOf(getString("status"))
    )
}

/**
 * Convert a list of Medications to a JSON array string.
 *
 * This is the main serialization method used by DataClient sync.
 * The entire medication list is serialized and stored in the Wearable Data Layer.
 *
 * Example usage:
 * ```
 * val medications = listOf(med1, med2, med3)
 * val jsonString = medications.toJsonString()
 * // Store in DataClient
 * dataMap.putString(KEY_MEDICATIONS_JSON, jsonString)
 * ```
 */
fun List<Medication>.toJsonString(): String {
    val jsonArray = JSONArray()
    forEach { medication ->
        jsonArray.put(medication.toJson())
    }
    return jsonArray.toString()
}

/**
 * Parse a JSON array string into a list of Medications.
 *
 * This is used by the watch to deserialize medication data received from phone.
 *
 * Example usage:
 * ```
 * val jsonString = dataMap.getString(KEY_MEDICATIONS_JSON)
 * val medications = parseMedicationsFromJson(jsonString)
 * MedicationRepository.updateFromSync(medications)
 * ```
 */
fun parseMedicationsFromJson(jsonString: String): List<Medication> {
    val medications = mutableListOf<Medication>()
    val jsonArray = JSONArray(jsonString)

    for (i in 0 until jsonArray.length()) {
        val jsonObject = jsonArray.getJSONObject(i)
        medications.add(jsonObject.toMedication())
    }

    return medications
}
