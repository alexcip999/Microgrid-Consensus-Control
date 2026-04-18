package com.microgrid.model.dto.inverter

import kotlinx.serialization.Serializable

@Serializable
data class InverterResponse(
    val id: String,
    val gridId: String,
    val label: String,
    val index: Int,
    val pMax: Double,
    val p0Ref: Double,
    val q0Ref: Double,
    val kdroopP: Double,
    val kdroopQ: Double,
    val rLine: Double,
    val lLine: Double,
    val epsilonP: Double,
    val epsilonQ: Double,
    val isActive: Boolean
)