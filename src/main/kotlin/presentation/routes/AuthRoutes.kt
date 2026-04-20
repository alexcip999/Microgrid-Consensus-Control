package presentation.routes

import domain.usecase.auth.GetCurrentUserUseCase
import domain.usecase.auth.LoginUseCase
import domain.usecase.auth.RegisterUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import presentation.dto.request.LoginRequest
import presentation.dto.request.RegisterRequest
import presentation.mapper.toAuthResponse
import presentation.mapper.toUserResponse
import java.util.UUID

fun Route.authRoutes(
    register: RegisterUseCase,
    login: LoginUseCase,
    getCurrentUser: GetCurrentUserUseCase
) {
    route("/auth") {
        post("/register") {
            val body = call.receive<RegisterRequest>()
            val result = register.execute(
                RegisterUseCase.Input(
                    email = body.email,
                    password = body.password,
                    role = body.role
                )
            )
            call.respond(HttpStatusCode.Created, result.user.toAuthResponse(result.token))
        }

        post("/login") {
            val body = call.receive<LoginRequest>()
            val result = login.execute(
                LoginUseCase.Input(
                    email = body.email,
                    password = body.password,
                )
            )
            call.respond(HttpStatusCode.OK, result.user.toAuthResponse(result.token))
        }

        authenticate("jwt-auth") {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = UUID.fromString(
                    principal.payload.getClaim("userId").asString()
                )
                val user = getCurrentUser.execute(userId)
                call.respond(HttpStatusCode.OK, user.toUserResponse())
            }
        }
    }
}