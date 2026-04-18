package com.microgrid.model.dto.grid

import com.microgrid.model.enums.GridPhase
import com.microgrid.model.enums.GridTopology
import kotlinx.serialization.Serializable

@Serializable
data class CreateGridRequest(
    val name: String,
    val phase: GridPhase,
    val topology: GridTopology = GridTopology.RING,
    val fNom: Double = 60.0,
    val vNom: Double = 1.0
)