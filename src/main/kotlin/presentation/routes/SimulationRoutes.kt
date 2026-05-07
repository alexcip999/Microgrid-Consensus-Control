package presentation.routes

import domain.usecase.simulation.CreateSimulationUseCase
import domain.usecase.simulation.GetSimulationStatusUseCase
import domain.usecase.simulation.GetSimulationUseCase
import domain.usecase.simulation.ListSimulationsUseCase
import domain.usecase.simulation.StartSimulationUseCase
import domain.usecase.simulation.StopSimulationUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import presentation.dto.request.CreateSimulationRequest
import presentation.mapper.toResponse
import java.util.UUID

fun Route.simulationRoutes(
    createSimulation: CreateSimulationUseCase,
    getSimulation: GetSimulationUseCase,
    listSimulations: ListSimulationsUseCase,
    startSimulation: StartSimulationUseCase,
    stopSimulation: StopSimulationUseCase,
    getSimulationStatus: GetSimulationStatusUseCase
) {
    authenticate("jwt-auth") {

        route("/grids/{gridId}/simulations") {

            post {
                val gridId = call.gridId()
                val body = call.receive<CreateSimulationRequest>()

                val simulation = createSimulation.execute(
                    CreateSimulationUseCase.Input(
                        gridId = gridId,
                        fidelity = body.fidelity,
                        description = body.description
                    )
                )
                call.respond(HttpStatusCode.Created, simulation.toResponse())
            }

            get {
                val gridId = call.gridId()
                val simulations = listSimulations.execute(gridId)
                    .map { it.toResponse() }
                call.respond(HttpStatusCode.OK, simulations)
            }
        }

        get("/simulations/{simulationId}") {
            val id         = call.simulationId()
            val simulation = getSimulation.execute(id)
            call.respond(HttpStatusCode.OK, simulation.toResponse())
        }

        get("/simulations/{simulationId}/status") {
            val id         = call.simulationId()
            val simulation = getSimulationStatus.execute(id)
            call.respond(HttpStatusCode.OK, simulation)
        }

        post("/simulations/{simulationId}/start") {
            val id         = call.simulationId()
            val simulation = startSimulation.execute(id)
            call.respond(HttpStatusCode.OK, simulation.toResponse())
        }

        post("/simulations/{simulationId}/stop") {
            val id         = call.simulationId()
            val simulation = stopSimulation.execute(id)
            call.respond(HttpStatusCode.OK, simulation.toResponse())
        }
    }
}

private fun RoutingCall.gridId(): UUID =
    try {
        UUID.fromString(
            parameters["gridId"]
                ?: throw NotFoundException("Missing grid id")
        )
    } catch (e: IllegalArgumentException) {
        throw NotFoundException("Invalid grid id format")
    }

private fun RoutingCall.simulationId(): UUID =
    try {
        UUID.fromString(
            parameters["simulationId"]
                ?: throw NotFoundException("Missing simulation id")
        )
    } catch (e: IllegalArgumentException) {
        throw NotFoundException("Invalid simulation id format")
    }