package domain.model.token

import domain.model.user.UserRole
import java.util.UUID

data class TokenClaims(
    val userId: UUID,
    val role: UserRole
)