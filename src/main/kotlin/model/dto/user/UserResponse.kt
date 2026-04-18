package com.microgrid.model.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val role: String,
    val createdAt: String
)
