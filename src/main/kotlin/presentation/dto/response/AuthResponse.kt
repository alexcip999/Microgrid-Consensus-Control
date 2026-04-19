package presentation.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String,
    val userId: String,
    val role: String
)