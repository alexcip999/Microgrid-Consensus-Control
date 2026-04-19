package ktor.presentation.routes

import application.service.JwtServiceImpl
import application.service.PasswordServiceImpl
import application.usecase.GetCurrentUserUseCase
import application.usecase.LoginUseCase
import application.usecase.RegisterUseCase
import domain.model.token.TokenConfig
import domain.repository.UserRepository
import domain.model.user.User
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.routing.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import presentation.plugins.ErrorResponse
import kotlinx.serialization.json.Json
import presentation.routes.authRoutes
import java.util.UUID

val testTokenConfig = TokenConfig(
    secret = "test-secret-key-minimum-32-characters",
    issuer = "microgrid-test",
    audience = "microgrid-mobile-test",
    expiresInMs = 86400000L
)

class FakeUserRepository : UserRepository {
    private val store = mutableMapOf<UUID, User>()

    override fun findById(id: UUID) = store[id]
    override fun findByEmail(email: String) =
        store.values.firstOrNull { it.email == email }

    override fun save(user: User): User {
        store[user.id] = user
        return user
    }

    fun clear() = store.clear()
}

fun Application.testModule(userRepository: FakeUserRepository) {
    val passwordService = PasswordServiceImpl()
    val tokenService = JwtServiceImpl(testTokenConfig)
    val register = RegisterUseCase(passwordService, userRepository, tokenService)
    val login = LoginUseCase(userRepository, passwordService, tokenService)
    val getCurrentUser = GetCurrentUserUseCase(userRepository)

    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }

    install(Authentication) {
        jwt("jwt-auth") {
            realm = "microgrid-test"
            verifier(
                JWT.require(Algorithm.HMAC256(testTokenConfig.secret))
                    .withAudience(testTokenConfig.audience)
                    .withIssuer(testTokenConfig.issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("userId").asString() != null)
                    JWTPrincipal(credential.payload)
                else null
            }
        }
    }

    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("BAD_REQUEST", cause.message ?: "Invalid input")
            )
        }
        exception<NoSuchElementException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse("NOT_FOUND", cause.message ?: "Resource not found")
            )
        }
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred")
            )
        }
    }

    routing {
        authRoutes(register, login, getCurrentUser)
        get("/health") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "ok"))
        }
    }
}