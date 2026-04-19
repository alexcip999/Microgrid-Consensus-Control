package presentation.dto.request

import domain.model.user.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val role: UserRole = UserRole.VIEWER
)