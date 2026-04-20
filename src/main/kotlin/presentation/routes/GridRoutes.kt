package presentation.routes

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
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import presentation.dto.request.CreateGridRequest
import presentation.dto.request.CreateInverterRequest
import presentation.dto.request.UpdateInverterRequest
import presentation.mapper.toResponse
import java.util.UUID

fun Route.gridRoutes(
    createGrid: CreateGridUseCase,
    getGrid: GetGridUseCase,
    listGrids: ListGridsUseCase,
    deleteGrid: DeleteGridUseCase,
    createInverter: CreateInverterUseCase,
    getInverter: GetInverterUseCase,
    listInverters: ListInvertersUseCase,
    updateInverter: UpdateInverterUseCase,
    deleteInverter: DeleteInverterUseCase
) {
    authenticate("jwt-auth") {
        route("/grids") {

            post {
                val ownerId = call.ownerId()
                val body    = call.receive<CreateGridRequest>()
                val grid    = createGrid.execute(
                    CreateGridUseCase.Input(
                        name     = body.name,
                        phase    = body.phase,
                        topology = body.topology,
                        fNom     = body.fNom,
                        vNom     = body.vNom,
                        ownerId  = ownerId
                    )
                )
                call.respond(HttpStatusCode.Created, grid.toResponse())
            }

            get {
                val ownerId  = call.ownerId()
                val grids    = listGrids.execute(ownerId)
                val response = grids.map { grid ->
                    val inverters = listInverters.execute(grid.id).map { it.toResponse() }
                    grid.toResponse(inverters)
                }
                call.respond(HttpStatusCode.OK, response)
            }

            get("/{gridId}") {
                val id        = call.gridId()
                val grid      = getGrid.execute(id)
                val inverters = listInverters.execute(id).map { it.toResponse() }
                call.respond(HttpStatusCode.OK, grid.toResponse(inverters))
            }

            delete("/{gridId}") {
                val id = call.gridId()
                deleteGrid.execute(id)
                call.respond(HttpStatusCode.NoContent)
            }


            route("/{gridId}/inverters") {

                post {
                    val gridId   = call.gridId()
                    val body     = call.receive<CreateInverterRequest>()
                    val inverter = createInverter.execute(
                        CreateInverterUseCase.Input(
                            gridId   = gridId,
                            label    = body.label,
                            index    = body.index,
                            pMax     = body.pMax,
                            p0Ref    = body.p0Ref,
                            q0Ref    = body.q0Ref,
                            kdroopP  = body.kdroopP,
                            kdroopQ  = body.kdroopQ,
                            rLine    = body.rLine,
                            lLine    = body.lLine,
                            epsilonP = body.epsilonP,
                            epsilonQ = body.epsilonQ
                        )
                    )
                    call.respond(HttpStatusCode.Created, inverter.toResponse())
                }

                get {
                    val gridId    = call.gridId()    // fix 3: era call.inverterId()
                    val inverters = listInverters.execute(gridId).map { it.toResponse() }
                    call.respond(HttpStatusCode.OK, inverters)
                }

                get("/{inverterId}") {
                    val inverterId = call.inverterId()
                    val inverter   = getInverter.execute(inverterId)
                    call.respond(HttpStatusCode.OK, inverter.toResponse())
                }

                put("/{inverterId}") {
                    val inverterId = call.inverterId()
                    val body       = call.receive<UpdateInverterRequest>()
                    val inverter   = updateInverter.execute(
                        UpdateInverterUseCase.Input(
                            id       = inverterId,
                            p0Ref    = body.p0Ref,
                            q0Ref    = body.q0Ref,
                            kdroopP  = body.kdroopP,
                            kdroopQ  = body.kdroopQ,
                            epsilonP = body.epsilonP,
                            epsilonQ = body.epsilonQ,
                            isActive = body.isActive
                        )
                    )
                    call.respond(HttpStatusCode.OK, inverter.toResponse())
                }

                delete("/{inverterId}") {
                    val inverterId = call.inverterId()
                    deleteInverter.execute(inverterId)
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}

private fun RoutingCall.ownerId(): UUID =
    UUID.fromString(principal<JWTPrincipal>()!!.payload.getClaim("userId").asString())

private fun RoutingCall.gridId(): UUID =        // fix 4: era parameters["id"]
    try {
        UUID.fromString(parameters["gridId"] ?: throw NoSuchElementException("Missing grid id"))
    } catch (e: IllegalArgumentException) {
        throw NoSuchElementException("Invalid grid id format")
    }

private fun RoutingCall.inverterId(): UUID =
    try {
        UUID.fromString(parameters["inverterId"] ?: throw NoSuchElementException("Missing inverter id"))
    } catch (e: IllegalArgumentException) {
        throw NoSuchElementException("Invalid inverter id format")
    }