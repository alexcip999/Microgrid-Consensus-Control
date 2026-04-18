package com.microgrid.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok", "service" to "microgrid-backend"))
        }
        // Aici vom adăuga route-urile pe măsură ce le construim:
        // authRoutes()
        // gridRoutes()
        // inverterRoutes()
        // telemetryRoutes()
    }
}