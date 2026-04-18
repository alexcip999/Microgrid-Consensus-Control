package com.microgrid.model.dto.simulation

import com.microgrid.model.enums.SimulationFidelity
import kotlinx.serialization.Serializable

@Serializable
data class CreateSimulationRequest(
    val gridId: String,
    val fidelity: SimulationFidelity = SimulationFidelity.LOW,
    val description: String? = null
)