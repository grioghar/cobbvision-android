package com.cobbvision.companion.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class DriveSession(
    val id:          String      = UUID.randomUUID().toString(),
    val startedAtMs: Long        = System.currentTimeMillis(),
    var endedAtMs:   Long?       = null,
    var trackPoints: List<TrackPoint> = emptyList(),
    var vehicleId:   String?     = null,
    var uploaded:    Boolean     = false,
) {
    val durationMs: Long get() = (endedAtMs ?: System.currentTimeMillis()) - startedAtMs
    val durationSeconds: Long  get() = durationMs / 1000
    val pointCount: Int        get() = trackPoints.size
}
