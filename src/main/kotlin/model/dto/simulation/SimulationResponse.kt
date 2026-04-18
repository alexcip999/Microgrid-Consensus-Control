package com.microgrid.model.dto.simulation

import kotlinx.serialization.Serializable

@Serializable
data class SimulationResponse(
    val id: String,
    val gridId: String,
    val status: String,
    val fidelity: String,
    val description: String?,
    val startedAt: String,
    val endedAt: String?
)