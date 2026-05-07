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
import domain.usecase.simulation.CreateSimulationUseCase
import domain.usecase.simulation.GetSimulationStatusUseCase
import domain.usecase.simulation.GetSimulationUseCase
import domain.usecase.simulation.ListSimulationsUseCase
import domain.usecase.simulation.StartSimulationUseCase
import domain.usecase.simulation.StopSimulationUseCase
import domain.usecase.telemetry.GetLatestSnapshotUseCase
import domain.usecase.telemetry.IngestTelemetryUseCase
import domain.usecase.telemetry.QueryTelemetryUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import presentation.routes.authRoutes
import presentation.routes.gridRoutes
import presentation.routes.simulationRoutes
import presentation.routes.telemetryRoutes

fun Application.configureRouting() {
    // Auth
    val register by inject<RegisterUseCase>()
    val login by inject<LoginUseCase>()
    val getCurrentUser by inject<GetCurrentUserUseCase>()

    // Grid
    val createGrid by inject<CreateGridUseCase>()
    val getGrid by inject<GetGridUseCase>()
    val listGrids by inject<ListGridsUseCase>()
    val deleteGrid by inject<DeleteGridUseCase>()

    // Inverter
    val createInverter by inject<CreateInverterUseCase>()
    val getInverter by inject<GetInverterUseCase>()
    val listInverters by inject<ListInvertersUseCase>()
    val updateInverter by inject<UpdateInverterUseCase>()
    val deleteInverter by inject<DeleteInverterUseCase>()

    // Simulation
    val createSimulation by inject<CreateSimulationUseCase>()
    val getSimulation by inject<GetSimulationUseCase>()
    val listSimulations by inject<ListSimulationsUseCase>()
    val startSimulation by inject<StartSimulationUseCase>()
    val stopSimulation by inject<StopSimulationUseCase>()
    val getSimulationStatus by inject<GetSimulationStatusUseCase>()

    // Telemetry
    val ingestTelemetry by inject<IngestTelemetryUseCase>()
    val queryTelemetry by inject<QueryTelemetryUseCase>()
    val getLatestSnapshot by inject<GetLatestSnapshotUseCase>()

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
        simulationRoutes(
            createSimulation, getSimulation, listSimulations,
            startSimulation, stopSimulation, getSimulationStatus
        )
        telemetryRoutes(ingestTelemetry, queryTelemetry, getLatestSnapshot)
    }
}