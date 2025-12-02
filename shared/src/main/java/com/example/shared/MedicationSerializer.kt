package com.example.shared

import org.json.JSONArray
import org.json.JSONObject

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
        put("status", status.name)
    }
}

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

fun List<Medication>.toJsonString(): String {
    val jsonArray = JSONArray()
    forEach { medication ->
        jsonArray.put(medication.toJson())
    }
    return jsonArray.toString()
}

fun parseMedicationsFromJson(jsonString: String): List<Medication> {
    val medications = mutableListOf<Medication>()
    val jsonArray = JSONArray(jsonString)

    for (i in 0 until jsonArray.length()) {
        val jsonObject = jsonArray.getJSONObject(i)
        medications.add(jsonObject.toMedication())
    }

    return medications
}
