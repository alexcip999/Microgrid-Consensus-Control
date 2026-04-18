package com.microgrid.model.dto.telemetry

import kotlinx.serialization.Serializable

@Serializable
data class TelemetryQueryResponse(
    val inverterId: String,
    val simulationId: String,
    val entries: List<TelemetryPoint>
)
