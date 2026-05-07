package ktor.presentation.routes

import domain.model.grid.GridPhase
import domain.model.grid.GridTopology
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import presentation.dto.request.CreateGridRequest
import presentation.dto.request.CreateInverterRequest
import presentation.dto.request.CreateSimulationRequest
import presentation.dto.request.LoginRequest
import presentation.dto.request.RegisterRequest
import presentation.dto.request.TelemetryBatchRequest
import presentation.dto.request.TelemetryPointRequest
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TelemetryRoutesTest {

    private val fakeUserRepository       = FakeUserRepository()
    private val fakeGridRepository       = FakeGridRepository()
    private val fakeInverterRepository   = FakeInverterRepository()
    private val fakeSimulationRepository = FakeSimulationRepository()
    private val fakeTelemetryRepository  = FakeTelemetryRepository()

    @BeforeTest
    fun setup() {
        fakeUserRepository.clear()
        fakeGridRepository.clear()
        fakeInverterRepository.clear()
        fakeSimulationRepository.clear()
        fakeTelemetryRepository.clear()
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private fun testApp(block: suspend ApplicationTestBuilder.() -> Unit) =
        testApplication {
            application {
                testModule(
                    fakeUserRepository,
                    fakeGridRepository,
                    fakeInverterRepository,
                    fakeSimulationRepository,
                    fakeTelemetryRepository
                )
            }
            block()
        }

    private fun ApplicationTestBuilder.jsonClient() =
        createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
        }

    private suspend fun ApplicationTestBuilder.getToken(): String {
        val client = jsonClient()
        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("test@microgrid.com", "password123"))
        }
        return client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("test@microgrid.com", "password123"))
        }.body<JsonObject>()["token"]!!.jsonPrimitive.content
    }

    private suspend fun ApplicationTestBuilder.setupRunningSimulation(): Triple<String, String, String> {
        val client = jsonClient()
        val token  = getToken()

        // Grid
        val gridId = client.post("/grids") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(
                CreateGridRequest(
                    name = "Test Grid",
                    phase = GridPhase.PHASE_1,
                    topology = GridTopology.RING
                )
            )
        }.body<JsonObject>()["id"]!!.jsonPrimitive.content

        // Invertor
        val inverterId = client.post("/grids/$gridId/inverters") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(CreateInverterRequest(label = "Inv1", index = 0))
        }.body<JsonObject>()["id"]!!.jsonPrimitive.content

        // Simulare
        val simulationId = client.post("/grids/$gridId/simulations") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(CreateSimulationRequest(gridId = gridId))
        }.body<JsonObject>()["id"]!!.jsonPrimitive.content

        // Porneste simularea
        client.post("/simulations/$simulationId/start") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        return Triple(token, simulationId, inverterId)
    }

    private fun makeTelemetryPoint(
        inverterId: String,
        timestamp: String = LocalDateTime.now().toString(),
        p: Double = 0.5,
        freq: Double = 60.0
    ) = TelemetryPointRequest(
        inverterId = inverterId,
        timestamp = timestamp,
        p = p,
        q = 0.1,
        vMag = 1.0,
        freq = freq,
        deltaOmega = 0.01,
        consensusError = 0.02,
        pNorm = p,
        pProduction = 0.6,
        pLoad = 0.1,
        pNet = 0.5
    )

    // ══════════════════════════════════════════════════════════════════════
    // INGEST BATCH
    // ══════════════════════════════════════════════════════════════════════

    @Test
    fun `ingest batch - success returns 201 with count`() = testApp {
        val (token, simulationId, inverterId) = setupRunningSimulation()

        val response = jsonClient().post("/telemetry/batch") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(
                TelemetryBatchRequest(
                    simulationId = simulationId,
                    points = listOf(
                        makeTelemetryPoint(inverterId, p = 0.5),
                        makeTelemetryPoint(inverterId, p = 0.6),
                        makeTelemetryPoint(inverterId, p = 0.7)
                    )
                )
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<JsonObject>()
        assertEquals(3, body["count"]?.jsonPrimitive?.int)
        assertEquals(simulationId, body["simulationId"]?.jsonPrimitive?.content)
        assertEquals(3, body["entries"]?.jsonArray?.size)
    }

    @Test
    fun `ingest batch - simulation not running returns 400`() = testApp {
        val client = jsonClient()
        val token  = getToken()

        // Creeaza grid + invertor + simulare (PENDING, nu RUNNING)
        val gridId = client.post("/grids") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(CreateGridRequest("Test Grid", GridPhase.PHASE_1))
        }.body<JsonObject>()["id"]!!.jsonPrimitive.content

        val inverterId = client.post("/grids/$gridId/inverters") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(CreateInverterRequest(label = "Inv1", index = 0))
        }.body<JsonObject>()["id"]!!.jsonPrimitive.content

        val simulationId = client.post("/grids/$gridId/simulations") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(CreateSimulationRequest(gridId = gridId))
        }.body<JsonObject>()["id"]!!.jsonPrimitive.content

        // Trimite telemetrie fara sa porneasca simularea
        val response = client.post("/telemetry/batch") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(TelemetryBatchRequest(
                simulationId = simulationId,
                points       = listOf(makeTelemetryPoint(inverterId))
            ))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `ingest batch - unknown simulation returns 404`() = testApp {
        val token    = getToken()
        val randomId = UUID.randomUUID().toString()

        val response = jsonClient().post("/telemetry/batch") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(TelemetryBatchRequest(
                simulationId = randomId,
                points       = listOf(makeTelemetryPoint(UUID.randomUUID().toString()))
            ))
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `ingest batch - empty points returns 400`() = testApp {
        val (token, simulationId, _) = setupRunningSimulation()

        val response = jsonClient().post("/telemetry/batch") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(TelemetryBatchRequest(
                simulationId = simulationId,
                points       = emptyList()
            ))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `ingest batch - no token returns 401`() = testApp {
        val response = jsonClient().post("/telemetry/batch") {
            contentType(ContentType.Application.Json)
            setBody(TelemetryBatchRequest(
                simulationId = UUID.randomUUID().toString(),
                points       = emptyList()
            ))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    // ══════════════════════════════════════════════════════════════════════
    // QUERY TELEMETRY
    // ══════════════════════════════════════════════════════════════════════

    @Test
    fun `query telemetry - returns all entries for simulation`() = testApp {
        val (token, simulationId, inverterId) = setupRunningSimulation()
        val client = jsonClient()

        // Injecteaza 5 puncte
        client.post("/telemetry/batch") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(TelemetryBatchRequest(
                simulationId = simulationId,
                points       = (1..5).map { makeTelemetryPoint(inverterId, p = it * 0.1) }
            ))
        }

        val response = client.get("/simulations/$simulationId/telemetry") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(5, response.body<JsonArray>().size)
    }

    @Test
    fun `query telemetry - filter by inverterId`() = testApp {
        val client                            = jsonClient()
        val (token, simulationId, inverterId) = setupRunningSimulation()

        // Al doilea invertor
        val gridId = fakeSimulationRepository.findById(UUID.fromString(simulationId))!!
            .gridId.toString()
        val inverterId2 = client.post("/grids/$gridId/inverters") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(CreateInverterRequest(label = "Inv2", index = 1))
        }.body<JsonObject>()["id"]!!.jsonPrimitive.content

        // 3 puncte pentru Inv1, 2 puncte pentru Inv2
        client.post("/telemetry/batch") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(TelemetryBatchRequest(
                simulationId = simulationId,
                points = listOf(
                    makeTelemetryPoint(inverterId),
                    makeTelemetryPoint(inverterId),
                    makeTelemetryPoint(inverterId),
                    makeTelemetryPoint(inverterId2),
                    makeTelemetryPoint(inverterId2)
                )
            ))
        }

        // Filtreaza doar Inv1
        val response = client.get("/simulations/$simulationId/telemetry?inverterId=$inverterId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(3, response.body<JsonArray>().size)
    }

    @Test
    fun `query telemetry - empty simulation returns empty list`() = testApp {
        val (token, simulationId, _) = setupRunningSimulation()

        val response = jsonClient().get("/simulations/$simulationId/telemetry") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(0, response.body<JsonArray>().size)
    }

    @Test
    fun `query telemetry - unknown simulation returns 404`() = testApp {
        val token    = getToken()
        val randomId = UUID.randomUUID()

        val response = jsonClient().get("/simulations/$randomId/telemetry") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    // ══════════════════════════════════════════════════════════════════════
    // LATEST SNAPSHOT
    // ══════════════════════════════════════════════════════════════════════

    @Test
    fun `latest snapshot - returns last entry per invertor`() = testApp {
        val client                            = jsonClient()
        val (token, simulationId, inverterId) = setupRunningSimulation()

        val base = LocalDateTime.of(2026, 4, 20, 10, 0, 0)

        client.post("/telemetry/batch") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(TelemetryBatchRequest(
                simulationId = simulationId,
                points       = listOf(
                    makeTelemetryPoint(inverterId, timestamp = base.toString(),                p = 0.3),
                    makeTelemetryPoint(inverterId, timestamp = base.plusSeconds(1).toString(), p = 0.5),
                    makeTelemetryPoint(inverterId, timestamp = base.plusSeconds(2).toString(), p = 0.8)
                )
            ))
        }

        val response = client.get("/simulations/$simulationId/telemetry/latest") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body      = response.body<JsonObject>()
        val snapshots = body["snapshots"]?.jsonArray
        assertEquals(1, snapshots?.size)
        assertEquals(0.8, snapshots?.get(0)?.jsonObject?.get("p")?.jsonPrimitive?.double)
    }

    @Test
    fun `latest snapshot - empty simulation returns empty snapshots`() = testApp {
        val (token, simulationId, _) = setupRunningSimulation()

        val response = jsonClient().get("/simulations/$simulationId/telemetry/latest") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val snapshots = response.body<JsonObject>()["snapshots"]?.jsonArray
        assertEquals(0, snapshots?.size)
    }

    @Test
    fun `latest snapshot - unknown simulation returns 404`() = testApp {
        val token    = getToken()
        val randomId = UUID.randomUUID()

        val response = jsonClient().get("/simulations/$randomId/telemetry/latest") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}