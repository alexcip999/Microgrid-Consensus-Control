package com.microgrid.model.dto.telemetry

import kotlinx.serialization.Serializable

@Serializable
data class InverterLiveState(
    val inverterId: String,
    val label: String,
    val p: Double,
    val q: Double,
    val vMag: Double,
    val freq: Double,
    val deltaOmega: Double,
    val pNorm: Double,
    val consensusError: Double,
    val alerts: List<String> = emptyList()
)