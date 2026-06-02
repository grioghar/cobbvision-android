package com.cobbvision.companion.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object ApiClient {

    var baseUrl: String = "https://cobbvision.com"
    var apiKey:  String = ""

    // ── Vehicles ─────────────────────────────────────────────────────────────

    @Serializable
    data class Vehicle(
        val id:        String,
        val name:      String,
        val make:      String,
        val model:     String,
        val year:      String,
        val ap_serial: String? = null,
    )

    suspend fun fetchVehicles(): List<Vehicle> = withContext(Dispatchers.IO) {
        val conn = openGet("/api/v1/vehicles")
        check(conn.responseCode == 200) { "HTTP ${conn.responseCode}" }
        Json { ignoreUnknownKeys = true }
            .decodeFromString<List<Vehicle>>(conn.inputStream.bufferedReader().readText())
    }

    // ── GPS track upload ──────────────────────────────────────────────────────

    data class UploadResult(
        val sessionId:   String,
        val healthScore: Int?,
        val analysisUrl: String?,
    )

    suspend fun uploadGPX(gpxFile: File, vehicleId: String): UploadResult =
        withContext(Dispatchers.IO) {
            val boundary = "CobbVisionBoundary-${System.currentTimeMillis()}"
            val url      = URL("$baseUrl/api/v1/gps-track")
            val conn     = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput      = true
                setRequestProperty("Authorization",  "Bearer $apiKey")
                setRequestProperty("Content-Type",   "multipart/form-data; boundary=$boundary")
                connectTimeout = 30_000
                readTimeout    = 60_000
            }

            conn.outputStream.use { out ->
                // vehicle_id field
                out.write("--$boundary\r\n".toByteArray())
                out.write("Content-Disposition: form-data; name=\"vehicle_id\"\r\n\r\n".toByteArray())
                out.write("$vehicleId\r\n".toByteArray())
                // gpx file
                out.write("--$boundary\r\n".toByteArray())
                out.write("Content-Disposition: form-data; name=\"gpx\"; filename=\"${gpxFile.name}\"\r\n".toByteArray())
                out.write("Content-Type: application/gpx+xml\r\n\r\n".toByteArray())
                gpxFile.inputStream().use { it.copyTo(out) }
                out.write("\r\n--$boundary--\r\n".toByteArray())
            }

            check(conn.responseCode == 201) {
                "Upload failed HTTP ${conn.responseCode}: " +
                    conn.errorStream?.bufferedReader()?.readText().orEmpty()
            }

            val body = conn.inputStream.bufferedReader().readText()
            val j    = Json { ignoreUnknownKeys = true }
            val map  = j.decodeFromString<Map<String, kotlinx.serialization.json.JsonElement>>(body)
            UploadResult(
                sessionId   = map["session_id"]?.toString()?.trim('"')  ?: "",
                healthScore = map["health_score"]?.toString()?.toIntOrNull(),
                analysisUrl = map["analysis_url"]?.toString()?.trim('"'),
            )
        }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun openGet(path: String): HttpURLConnection =
        (URL("$baseUrl$path").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Authorization", "Bearer $apiKey")
            connectTimeout = 15_000
            readTimeout    = 15_000
        }
}
