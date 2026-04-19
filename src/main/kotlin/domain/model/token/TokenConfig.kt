package domain.model.token

data class TokenConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val expiresInMs: Long
)
