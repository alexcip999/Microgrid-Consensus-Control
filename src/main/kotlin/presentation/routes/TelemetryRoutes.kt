package presentation.routes

import domain.usecase.telemetry.GetLatestSnapshotUseCase
import domain.usecase.telemetry.IngestTelemetryUseCase
import domain.usecase.telemetry.QueryTelemetryUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import presentation.dto.request.TelemetryBatchRequest
import presentation.dto.response.TelemetryBatchResponse
import presentation.mapper.toResponse
import presentation.plugins.ValidationException
import java.time.LocalDateTime
import java.util.UUID

fun Route.telemetryRoutes(
    ingestTelemetry: IngestTelemetryUseCase,
    queryTelemetry: QueryTelemetryUseCase,
    getLatestSnapshot: GetLatestSnapshotUseCase
) {
    authenticate("jwt-auth") {

        post("/telemetry/batch") {
            val body = call.receive<TelemetryBatchRequest>()

            val simulationId = try {
                UUID.fromString(body.simulationId)
            } catch (e: IllegalArgumentException) {
                throw ValidationException("Invalid simulation id format")
            }

            val points = body.points.map { point ->
                IngestTelemetryUseCase.TelemetryPoint(
                    inverterId = UUID.fromString(point.inverterId),
                    timestamp = LocalDateTime.parse(point.timestamp),
                    p = point.p,
                    q = point.q,
                    vMag = point.vMag,
                    freq = point.freq,
                    deltaOmega = point.deltaOmega,
                    consensusError = point.consensusError,
                    pNorm = point.pNorm,
                    pProduction = point.pProduction,
                    pLoad = point.pLoad,
                    pNet = point.pNet
                )
            }

            val saved = ingestTelemetry.execute(
                IngestTelemetryUseCase.Input(
                    simulationId = simulationId,
                    points = points
                )
            )

            call.respond(
                HttpStatusCode.Created,
                TelemetryBatchResponse(
                    simulationId = body.simulationId,
                    count = saved.size,
                    entries = saved.map { it.toResponse() }
                )
            )
        }

        get("/simulations/{simulationId}/telemetry") {
            val simulationId = call.simulationId()

            val inverterId = call.request.queryParameters["inverterId"]
                ?.let { UUID.fromString(it) }
            val from = call.request.queryParameters["from"]
                ?.let { LocalDateTime.parse(it) }
            val to = call.request.queryParameters["to"]
                ?.let { LocalDateTime.parse(it) }

            val entries = queryTelemetry.execute(
                QueryTelemetryUseCase.Input(
                    simulationId = simulationId,
                    inverterId = inverterId,
                    from = from,
                    to = to
                )
            )

            call.respond(HttpStatusCode.OK, entries.map { it.toResponse() })
        }

        get("/simulations/{simulationId}/telemetry/latest") {
            val simulationId = call.simulationId()
            val snapshot = getLatestSnapshot.execute(simulationId)
            call.respond(HttpStatusCode.OK, snapshot.toResponse())
        }
    }
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