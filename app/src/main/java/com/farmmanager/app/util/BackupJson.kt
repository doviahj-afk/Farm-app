package com.farmmanager.app.util

import com.farmmanager.app.data.entity.*
import com.farmmanager.app.data.repository.FarmDataSnapshot
import org.json.JSONArray
import org.json.JSONObject

/** Serializes/deserializes a full [FarmDataSnapshot] to JSON for local backup files and cloud sync. */
object BackupJson {

    fun toJson(snapshot: FarmDataSnapshot): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        root.put("flocks", JSONArray(snapshot.flocks.map { f ->
            JSONObject().apply {
                put("id", f.id); put("name", f.name); put("breed", f.breed); put("quantity", f.quantity)
                put("acquisitionDate", f.acquisitionDate); put("acquisitionCost", f.acquisitionCost)
                put("notes", f.notes); put("hatchDate", f.hatchDate ?: JSONObject.NULL); put("cageCount", f.cageCount)
            }
        }))

        root.put("eggRecords", JSONArray(snapshot.eggRecords.map { e ->
            JSONObject().apply {
                put("id", e.id); put("flockId", e.flockId); put("date", e.date)
                put("quantityCollected", e.quantityCollected); put("quantityBroken", e.quantityBroken)
                put("notes", e.notes); put("cageNumber", e.cageNumber)
            }
        }))

        root.put("feedRecords", JSONArray(snapshot.feedRecords.map { f ->
            JSONObject().apply {
                put("id", f.id); put("flockId", f.flockId); put("feedName", f.feedName); put("date", f.date)
                put("quantityKg", f.quantityKg); put("cost", f.cost); put("notes", f.notes)
            }
        }))

        root.put("healthRecords", JSONArray(snapshot.healthRecords.map { h ->
            JSONObject().apply {
                put("id", h.id); put("flockId", h.flockId); put("date", h.date); put("type", h.type)
                put("description", h.description); put("cost", h.cost); put("notes", h.notes)
            }
        }))

        root.put("transactions", JSONArray(snapshot.transactions.map { t ->
            JSONObject().apply {
                put("id", t.id); put("type", t.type.name); put("category", t.category); put("date", t.date)
                put("amount", t.amount); put("description", t.description)
            }
        }))

        root.put("reminders", JSONArray(snapshot.reminders.map { r ->
            JSONObject().apply {
                put("id", r.id); put("title", r.title); put("dateTime", r.dateTime); put("notes", r.notes)
                put("isCompleted", r.isCompleted); put("repeatType", r.repeatType); put("alarmScheduled", r.alarmScheduled)
            }
        }))

        root.put("notes", JSONArray(snapshot.notes.map { n ->
            JSONObject().apply { put("id", n.id); put("title", n.title); put("content", n.content); put("date", n.date) }
        }))

        return root.toString(2)
    }

    fun fromJson(json: String): FarmDataSnapshot {
        val root = JSONObject(json)

        val flocks = root.getJSONArray("flocks").let { arr ->
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Flock(
                    id = o.getLong("id"), name = o.getString("name"), breed = o.getString("breed"),
                    quantity = o.getInt("quantity"), acquisitionDate = o.getLong("acquisitionDate"),
                    acquisitionCost = o.getDouble("acquisitionCost"), notes = o.optString("notes", ""),
                    hatchDate = if (o.isNull("hatchDate")) null else o.getLong("hatchDate"),
                    cageCount = o.optInt("cageCount", 1)
                )
            }
        }

        val eggRecords = root.getJSONArray("eggRecords").let { arr ->
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                EggRecord(
                    id = o.getLong("id"), flockId = o.getLong("flockId"), date = o.getLong("date"),
                    quantityCollected = o.getInt("quantityCollected"), quantityBroken = o.optInt("quantityBroken", 0),
                    notes = o.optString("notes", ""), cageNumber = o.optInt("cageNumber", 0)
                )
            }
        }

        val feedRecords = root.getJSONArray("feedRecords").let { arr ->
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                FeedRecord(
                    id = o.getLong("id"), flockId = o.getLong("flockId"), feedName = o.getString("feedName"),
                    date = o.getLong("date"), quantityKg = o.getDouble("quantityKg"), cost = o.getDouble("cost"),
                    notes = o.optString("notes", "")
                )
            }
        }

        val healthRecords = root.getJSONArray("healthRecords").let { arr ->
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                HealthRecord(
                    id = o.getLong("id"), flockId = o.getLong("flockId"), date = o.getLong("date"),
                    type = o.getString("type"), description = o.getString("description"),
                    cost = o.optDouble("cost", 0.0), notes = o.optString("notes", "")
                )
            }
        }

        val transactions = root.getJSONArray("transactions").let { arr ->
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                TransactionEntity(
                    id = o.getLong("id"), type = TransactionType.valueOf(o.getString("type")),
                    category = o.getString("category"), date = o.getLong("date"),
                    amount = o.getDouble("amount"), description = o.optString("description", "")
                )
            }
        }

        val reminders = root.getJSONArray("reminders").let { arr ->
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Reminder(
                    id = o.getLong("id"), title = o.getString("title"), dateTime = o.getLong("dateTime"),
                    notes = o.optString("notes", ""), isCompleted = o.optBoolean("isCompleted", false),
                    repeatType = o.optString("repeatType", "NONE"), alarmScheduled = o.optBoolean("alarmScheduled", true)
                )
            }
        }

        val notes = root.getJSONArray("notes").let { arr ->
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Note(id = o.getLong("id"), title = o.getString("title"), content = o.getString("content"), date = o.getLong("date"))
            }
        }

        return FarmDataSnapshot(flocks, eggRecords, feedRecords, healthRecords, transactions, reminders, notes)
    }
}
