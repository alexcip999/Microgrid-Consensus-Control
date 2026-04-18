package com.microgrid.model.dto.telemetry

import kotlinx.serialization.Serializable

@Serializable
data class TelemetryPoint(
    val time: String,           // ISO 8601: "2026-04-18T10:00:00.123"
    val p: Double,
    val q: Double,
    val vMag: Double,
    val freq: Double,
    val deltaOmega: Double,
    val pNorm: Double,
    val consensusError: Double
)
