package com.microgrid.model.dto.telemetry

import kotlinx.serialization.Serializable

@Serializable
data class GridLiveSnapshot(
    val gridId: String,
    val simulationId: String,
    val timestamp: String,
    val inverters: List<InverterLiveState>
)
