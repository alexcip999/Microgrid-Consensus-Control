package presentation.plugins

import domain.model.token.TokenConfig
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import presentation.di.appModule

fun Application.configureDI() {
    val tokenConfig = TokenConfig(
        secret = environment.config.property("jwt.secret").getString(),
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresInMs = environment.config.property("jwt.expiresInMs").getString().toLong()
    )

    install(Koin) {
        slf4jLogger()
        modules(
            module { single { tokenConfig } },
            appModule
        )
    }
}
