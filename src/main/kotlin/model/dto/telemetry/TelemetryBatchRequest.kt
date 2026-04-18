package com.microgrid.model.dto.telemetry

import kotlinx.serialization.Serializable

@Serializable
data class TelemetryBatchRequest(
    val simulationId: String,
    val inverterId: String,
    val entries: List<TelemetryPoint>
)