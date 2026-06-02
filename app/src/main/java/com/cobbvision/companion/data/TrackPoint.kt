package com.cobbvision.companion.data

import android.location.Location
import kotlinx.serialization.Serializable

/** A single GPS sample recorded during a drive session. */
@Serializable
data class TrackPoint(
    val latitude:           Double,
    val longitude:          Double,
    val altitudeM:          Double,   // metres
    val speedMS:            Float,    // m/s; negative = unavailable
    val accuracyM:          Float,    // horizontal accuracy in metres
    val timestampMs:        Long,     // System.currentTimeMillis()
) {
    val speedKPH: Float get() = maxOf(0f, speedMS) * 3.6f
    val speedMPH: Float get() = maxOf(0f, speedMS) * 2.23694f

    companion object {
        fun from(location: Location) = TrackPoint(
            latitude    = location.latitude,
            longitude   = location.longitude,
            altitudeM   = location.altitude,
            speedMS     = location.speed,
            accuracyM   = location.accuracy,
            timestampMs = location.time,
        )
    }
}
