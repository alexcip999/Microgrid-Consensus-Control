package infra.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import domain.model.token.TokenClaims
import domain.model.token.TokenConfig
import domain.service.TokenService
import java.util.Date

class JwtServiceImpl(override val config: TokenConfig) : TokenService {
    override fun generate(claims: TokenClaims): String =
        JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .withClaim("userId", claims.userId.toString())
            .withClaim("role", claims.role.name)
            .withExpiresAt(Date(System.currentTimeMillis() + config.expiresInMs))
            .sign(Algorithm.HMAC256(config.secret))

}