package presentation.plugins

import domain.usecase.auth.GetCurrentUserUseCase
import domain.usecase.auth.LoginUseCase
import domain.usecase.auth.RegisterUseCase
import domain.usecase.grid.CreateGridUseCase
import domain.usecase.grid.DeleteGridUseCase
import domain.usecase.grid.GetGridUseCase
import domain.usecase.grid.ListGridsUseCase
import domain.usecase.inverter.CreateInverterUseCase
import domain.usecase.inverter.DeleteInverterUseCase
import domain.usecase.inverter.GetInverterUseCase
import domain.usecase.inverter.ListInvertersUseCase
import domain.usecase.inverter.UpdateInverterUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import presentation.routes.authRoutes
import presentation.routes.gridRoutes

fun Application.configureRouting() {
    // ── Auth ───────────────────────────────────────────
    val register       by inject<RegisterUseCase>()
    val login          by inject<LoginUseCase>()
    val getCurrentUser by inject<GetCurrentUserUseCase>()

    // ── Grid ───────────────────────────────────────────
    val createGrid by inject<CreateGridUseCase>()
    val getGrid    by inject<GetGridUseCase>()
    val listGrids  by inject<ListGridsUseCase>()
    val deleteGrid by inject<DeleteGridUseCase>()

    // ── Inverter ───────────────────────────────────────
    val createInverter by inject<CreateInverterUseCase>()
    val getInverter    by inject<GetInverterUseCase>()
    val listInverters  by inject<ListInvertersUseCase>()
    val updateInverter by inject<UpdateInverterUseCase>()
    val deleteInverter by inject<DeleteInverterUseCase>()

    routing {
        get("/health") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "ok"))
        }
        authRoutes(register, login, getCurrentUser)
        gridRoutes(
            createGrid, getGrid, listGrids, deleteGrid,
            createInverter, getInverter, listInverters,
            updateInverter, deleteInverter
        )
    }
}