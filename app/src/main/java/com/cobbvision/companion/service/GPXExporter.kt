package com.cobbvision.companion.service

import com.cobbvision.companion.data.DriveSession
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object GPXExporter {

    private val iso8601 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun export(session: DriveSession, outputDir: File): File {
        val filename = "cobbvision_${session.startedAtMs}.gpx"
        val file = File(outputDir, filename)
        file.writeText(buildGPX(session))
        return file
    }

    private fun buildGPX(session: DriveSession): String {
        val sessionName = "CobbVision ${formatDate(session.startedAtMs)}"
        val created     = iso8601.format(Date(session.startedAtMs))

        return buildString {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
            appendLine("""<gpx version="1.1"""")
            appendLine("""     creator="CobbVision Companion/1.0"""")
            appendLine("""     xmlns="http://www.topografix.com/GPX/1/1"""")
            appendLine("""     xmlns:gpxtpx="http://www.garmin.com/xmlschemas/TrackPointExtension/v2"""")
            appendLine("""     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"""")
            appendLine("""     xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">""")
            appendLine("""  <metadata>""")
            appendLine("""    <name>${xmlEscape(sessionName)}</name>""")
            appendLine("""    <time>$created</time>""")
            appendLine("""  </metadata>""")
            appendLine("""  <trk>""")
            appendLine("""    <name>${xmlEscape(sessionName)}</name>""")
            appendLine("""    <trkseg>""")

            for (pt in session.trackPoints) {
                val time  = iso8601.format(Date(pt.timestampMs))
                val speed = "%.4f".format(maxOf(0f, pt.speedMS))
                appendLine("""      <trkpt lat="${pt.latitude}" lon="${pt.longitude}">""")
                appendLine("""        <ele>${"%.2f".format(pt.altitudeM)}</ele>""")
                appendLine("""        <time>$time</time>""")
                appendLine("""        <extensions>""")
                appendLine("""          <gpxtpx:TrackPointExtension>""")
                appendLine("""            <gpxtpx:speed>$speed</gpxtpx:speed>""")
                appendLine("""          </gpxtpx:TrackPointExtension>""")
                appendLine("""        </extensions>""")
                appendLine("""      </trkpt>""")
            }

            appendLine("""    </trkseg>""")
            appendLine("""  </trk>""")
            appendLine("""</gpx>""")
        }
    }

    private fun formatDate(ms: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(ms))

    private fun xmlEscape(s: String) = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}
