package com.microgrid.model.dto.inverter

import kotlinx.serialization.Serializable

@Serializable
data class UpdateInverterRequest(
    val p0Ref: Double? = null,
    val q0Ref: Double? = null,
    val kdroopP: Double? = null,
    val kdroopQ: Double? = null,
    val epsilonP: Double? = null,
    val epsilonQ: Double? = null,
    val isActive: Boolean? = null
)