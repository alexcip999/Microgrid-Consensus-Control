package com.microgrid.model.dto.alert

import kotlinx.serialization.Serializable

@Serializable
data class AlertResponse(
    val id: String,
    val inverterId: String,
    val simulationId: String,
    val type: String,
    val value: Double,
    val threshold: Double,
    val triggeredAt: String
)