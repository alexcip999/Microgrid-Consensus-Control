package presentation.plugins

import application.usecase.GetCurrentUserUseCase
import application.usecase.LoginUseCase
import application.usecase.RegisterUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import presentation.routes.authRoutes

fun Application.configureRouting() {
    val register by inject<RegisterUseCase>()
    val login by inject<LoginUseCase>()
    val getCurrentUser by inject<GetCurrentUserUseCase>()

    routing {
        get("/health") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "ok"))
        }

        authRoutes(register, login, getCurrentUser)
    }
}