package com.microgrid.model.dto.auth

import com.microgrid.model.enums.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val role: UserRole = UserRole.VIEWER
)
