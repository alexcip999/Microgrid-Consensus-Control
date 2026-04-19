package presentation.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import domain.service.TokenService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.koin.ktor.ext.inject

fun Application.configureAuthentication() {
    val tokenService by inject<TokenService>()
    val config = tokenService.config

    install(Authentication) {
        jwt("jwt-auth") {
            realm = this@configureAuthentication.environment.config.property("jwt.realm").getString()
            verifier(
                JWT.require(Algorithm.HMAC256(config.secret))
                    .withAudience(config.audience)
                    .withIssuer(config.issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("userId").asString() != null)
                    JWTPrincipal(credential.payload)
                else null
            }
        }
    }
}