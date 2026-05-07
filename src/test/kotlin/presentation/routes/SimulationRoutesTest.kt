package ktor.presentation.routes

import domain.model.grid.GridPhase
import domain.model.grid.GridTopology
import domain.model.simulation.SimulationFidelity
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import presentation.dto.request.CreateGridRequest
import presentation.dto.request.CreateSimulationRequest
import presentation.dto.request.LoginRequest
import presentation.dto.request.RegisterRequest
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SimulationRoutesTest {

    private val fakeUserRepository       = FakeUserRepository()
    private val fakeGridRepository       = FakeGridRepository()
    private val fakeInverterRepository   = FakeInverterRepository()
    private val fakeSimulationRepository = FakeSimulationRepository()

    @BeforeTest
    fun setup() {
        fakeUserRepository.clear()
        fakeGridRepository.clear()
        fakeInverterRepository.clear()
        fakeSimulationRepository.clear()
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private fun testApp(block: suspend ApplicationTestBuilder.() -> Unit) =
        testApplication {
            application {
                testModule(
                    fakeUserRepository,
                    fakeGridRepository,
                    fakeInverterRepository,
                    fakeSimulationRepository
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

    private suspend fun ApplicationTestBuilder.getToken(
        email: String = "test@microgrid.com",
        password: String = "password123"
    ): String {
        val client = jsonClient()
        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(email, password))
        }
        return client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }.body<JsonObject>()["token"]!!.jsonPrimitive.content
    }

    private suspend fun ApplicationTestBuilder.createGrid(token: String): String {
        return jsonClient().post("/grids") {
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
    }

    private suspend fun ApplicationTestBuilder.createSimulation(
        token: String,
        gridId: String,
        fidelity: SimulationFidelity = SimulationFidelity.LOW
    ): String {
        return jsonClient().post("/grids/$gridId/simulations") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(
                CreateSimulationRequest(
                    gridId = gridId,
                    fidelity = fidelity
                )
            )
        }.body<JsonObject>()["id"]!!.jsonPrimitive.content
    }

    // ══════════════════════════════════════════════════════════════════════
    // CREATE
    // ══════════════════════════════════════════════════════════════════════

    @Test
    fun `create simulation - success returns 201 with PENDING status`() = testApp {
        val token  = getToken()
        val gridId = createGrid(token)

        val response = jsonClient().post("/grids/$gridId/simulations") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(CreateSimulationRequest(
                gridId      = gridId,
                fidelity    = SimulationFidelity.LOW,
                description = "Faza 1 - validare consens"
            ))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<JsonObject>()
        assertEquals("PENDING", body["status"]?.jsonPrimitive?.content)
        assertEquals("LOW", body["fidelity"]?.jsonPrimitive?.content)
        assertEquals("Faza 1 - validare consens", body["description"]?.jsonPrimitive?.content)
        assertNotNull(body["id"])
        assertNotNull(body["startedAt"])
        assertNull(body["endedAt"]?.jsonPrimitive?.contentOrNull)
    }

    @Test
    fun `create simulation - invalid grid returns 404`() = testApp {
        val token    = getToken()
        val randomId = UUID.randomUUID().toString()

        val response = jsonClient().post("/grids/$randomId/simulations") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(CreateSimulationRequest(gridId = randomId))
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `create simulation - duplicate active simulation returns 400`() = testApp {
        val token  = getToken()
        val gridId = createGrid(token)
        val client = jsonClient()

        // Prima simulare — succes
        client.post("/grids/$gridId/simulations") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(CreateSimulationRequest(gridId = gridId))
        }

        // A doua pe acelasi grid — trebuie sa fie 400
        val response = client.post("/grids/$gridId/simulations") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(CreateSimulationRequest(gridId = gridId))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.body<JsonObject>()
        assertEquals("BAD_REQUEST", body["error"]?.jsonPrimitive?.content)
    }

    @Test
    fun `create simulation - no token returns 401`() = testApp {
        val token  = getToken()
        val gridId = createGrid(token)

        val response = jsonClient().post("/grids/$gridId/simulations") {
            contentType(ContentType.Application.Json)
            setBody(CreateSimulationRequest(gridId = gridId))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    // ══════════════════════════════════════════════════════════════════════
    // LIST
    // ══════════════════════════════════════════════════════════════════════

    @Test
    fun `list simulations - returns all simulations for grid`() = testApp {
        val token  = getToken()
        val gridId = createGrid(token)
        val client = jsonClient()

        // Creeaza 2 simulari — a doua dupa ce prima e completata
        val sim1Id = createSimulation(token, gridId)

        // Completeaza prima simulare ca sa putem crea a doua
        client.post("/simulations/$sim1Id/start") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        client.post("/simulations/$sim1Id/stop") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        createSimulation(token, gridId)

        val response = client.get("/grids/$gridId/simulations") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(2, response.body<JsonArray>().size)
    }

    @Test
    fun `list simulations - empty list for new grid`() = testApp {
        val token  = getToken()
        val gridId = createGrid(token)

        val response = jsonClient().get("/grids/$gridId/simulations") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(0, response.body<JsonArray>().size)
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET
    // ══════════════════════════════════════════════════════════════════════

    @Test
    fun `get simulation - existing id returns 200`() = testApp {
        val token        = getToken()
        val gridId       = createGrid(token)
        val simulationId = createSimulation(token, gridId)

        val response = jsonClient().get("/simulations/$simulationId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(simulationId, response.body<JsonObject>()["id"]?.jsonPrimitive?.content)
    }

    @Test
    fun `get simulation - unknown id returns 404`() = testApp {
        val token    = getToken()
        val randomId = UUID.randomUUID()

        val response = jsonClient().get("/simulations/$randomId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    // ══════════════════════════════════════════════════════════════════════
    // STATUS
    // ══════════════════════════════════════════════════════════════════════

    @Test
    fun `get status - returns current status`() = testApp {
        val token        = getToken()
        val gridId       = createGrid(token)
        val simulationId = createSimulation(token, gridId)

        val response = jsonClient().get("/simulations/$simulationId/status") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<JsonObject>()
        assertEquals("PENDING", body["status"]?.jsonPrimitive?.content)
        assertNotNull(body["startedAt"])
    }

    // ══════════════════════════════════════════════════════════════════════
    // START
    // ══════════════════════════════════════════════════════════════════════

    @Test
    fun `start simulation - success changes status to RUNNING`() = testApp {
        val token        = getToken()
        val gridId       = createGrid(token)
        val simulationId = createSimulation(token, gridId)

        val response = jsonClient().post("/simulations/$simulationId/start") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("RUNNING", response.body<JsonObject>()["status"]?.jsonPrimitive?.content)
    }

    @Test
    fun `start simulation - already running returns 400`() = testApp {
        val token        = getToken()
        val gridId       = createGrid(token)
        val simulationId = createSimulation(token, gridId)
        val client       = jsonClient()

        // Prima pornire — succes
        client.post("/simulations/$simulationId/start") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        // A doua pornire — trebuie sa fie 400
        val response = client.post("/simulations/$simulationId/start") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `start simulation - unknown id returns 404`() = testApp {
        val token    = getToken()
        val randomId = UUID.randomUUID()

        val response = jsonClient().post("/simulations/$randomId/start") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    // ══════════════════════════════════════════════════════════════════════
    // STOP
    // ══════════════════════════════════════════════════════════════════════

    @Test
    fun `stop simulation - success changes status to COMPLETED`() = testApp {
        val token        = getToken()
        val gridId       = createGrid(token)
        val simulationId = createSimulation(token, gridId)
        val client       = jsonClient()

        client.post("/simulations/$simulationId/start") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        val response = client.post("/simulations/$simulationId/stop") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<JsonObject>()
        assertEquals("COMPLETED", body["status"]?.jsonPrimitive?.content)
        assertNotNull(body["endedAt"]?.jsonPrimitive?.contentOrNull)
    }

    @Test
    fun `stop simulation - not running returns 400`() = testApp {
        val token        = getToken()
        val gridId       = createGrid(token)
        val simulationId = createSimulation(token, gridId)

        // Stop fara start — trebuie sa fie 400
        val response = jsonClient().post("/simulations/$simulationId/stop") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `stop simulation - unknown id returns 404`() = testApp {
        val token    = getToken()
        val randomId = UUID.randomUUID()

        val response = jsonClient().post("/simulations/$randomId/stop") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `full lifecycle - PENDING to RUNNING to COMPLETED`() = testApp {
        val token        = getToken()
        val gridId       = createGrid(token)
        val simulationId = createSimulation(token, gridId)
        val client       = jsonClient()

        // Verifica PENDING
        var status = client.get("/simulations/$simulationId/status") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body<JsonObject>()["status"]?.jsonPrimitive?.content
        assertEquals("PENDING", status)

        // Start → RUNNING
        client.post("/simulations/$simulationId/start") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        status = client.get("/simulations/$simulationId/status") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body<JsonObject>()["status"]?.jsonPrimitive?.content
        assertEquals("RUNNING", status)

        // Stop → COMPLETED
        client.post("/simulations/$simulationId/stop") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        status = client.get("/simulations/$simulationId/status") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body<JsonObject>()["status"]?.jsonPrimitive?.content
        assertEquals("COMPLETED", status)
    }
}