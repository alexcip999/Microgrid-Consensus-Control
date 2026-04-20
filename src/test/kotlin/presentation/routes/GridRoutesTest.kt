package ktor.presentation.routes

import domain.model.grid.GridPhase
import domain.model.grid.GridTopology
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
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
import kotlinx.serialization.json.jsonPrimitive
import presentation.dto.request.CreateGridRequest
import presentation.dto.request.CreateInverterRequest
import presentation.dto.request.UpdateInverterRequest
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GridRoutesTest {

    // ── Fake repositories ──────────────────────────────────────────────────
    private val fakeUserRepository  = FakeUserRepository()
    private val fakeGridRepository  = FakeGridRepository()
    private val fakeInverterRepository = FakeInverterRepository()

    @BeforeTest
    fun setup() {
        fakeUserRepository.clear()
        fakeGridRepository.clear()
        fakeInverterRepository.clear()
    }

    // ── Test helpers ───────────────────────────────────────────────────────
    private fun testApp(block: suspend ApplicationTestBuilder.() -> Unit) =
        testApplication {
            application {
                testModule(fakeUserRepository, fakeGridRepository, fakeInverterRepository)
            }
            block()
        }

    private fun ApplicationTestBuilder.jsonClient() =
        createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
        }

    // Register + login, returneaza token
    private suspend fun ApplicationTestBuilder.getToken(
        email: String = "test@microgrid.com",
        password: String = "password123"
    ): String {
        val client = jsonClient()
        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(presentation.dto.request.RegisterRequest(email, password))
        }
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(presentation.dto.request.LoginRequest(email, password))
        }
        return response.body<JsonObject>()["token"]!!.jsonPrimitive.content
    }

    // ══════════════════════════════════════════════════════════════════════
    // GRID
    // ══════════════════════════════════════════════════════════════════════

    @Test
    fun `create grid - success returns 201`() = testApp {
        val token = getToken()
        val response = jsonClient().post("/grids") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(
                CreateGridRequest(
                    name = "Microgrid Alpha",
                    phase = GridPhase.PHASE_1,
                    topology = GridTopology.RING,
                    fNom = 60.0,
                    vNom = 1.0
                )
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<JsonObject>()
        assertEquals("Microgrid Alpha", body["name"]?.jsonPrimitive?.content)
        assertEquals("PHASE_1", body["phase"]?.jsonPrimitive?.content)
        assertEquals("RING", body["topology"]?.jsonPrimitive?.content)
        assertNotNull(body["id"])
    }

    @Test
    fun `create grid - no token returns 401`() = testApp {
        val response = jsonClient().post("/grids") {
            contentType(ContentType.Application.Json)
            setBody(CreateGridRequest(
                name  = "Microgrid Alpha",
                phase = GridPhase.PHASE_1
            ))
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `create grid - blank name returns 400`() = testApp {
        val token = getToken()
        val response = jsonClient().post("/grids") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(CreateGridRequest(
                name  = "",
                phase = GridPhase.PHASE_1
            ))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `create grid - invalid frequency returns 400`() = testApp {
        val token = getToken()
        val response = jsonClient().post("/grids") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(CreateGridRequest(
                name  = "Test Grid",
                phase = GridPhase.PHASE_1,
                fNom  = 100.0    // invalida — trebuie 50..60
            ))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `list grids - returns only owner grids`() = testApp {
        val client = jsonClient()

        // User 1 creeaza 2 griduri
        val token1 = getToken("user1@microgrid.com")
        client.post("/grids") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token1")
            setBody(CreateGridRequest("Grid A", GridPhase.PHASE_1))
        }
        client.post("/grids") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token1")
            setBody(CreateGridRequest("Grid B", GridPhase.PHASE_2))
        }

        // User 2 creeaza 1 grid
        val token2 = getToken("user2@microgrid.com")
        client.post("/grids") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token2")
            setBody(CreateGridRequest("Grid C", GridPhase.PHASE_3))
        }

        // User 1 vede doar gridurile lui
        val response = client.get("/grids") {
            header(HttpHeaders.Authorization, "Bearer $token1")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<JsonArray>()
        assertEquals(2, body.size)
    }

    @Test
    fun `get grid - existing id returns 200`() = testApp {
        val client = jsonClient()
        val token  = getToken()

        val createResponse = client.post("/grids") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(CreateGridRequest("Test Grid", GridPhase.PHASE_1))
        }
        val gridId = createResponse.body<JsonObject>()["id"]!!.jsonPrimitive.content

        val response = client.get("/grids/$gridId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(gridId, response.body<JsonObject>()["id"]?.jsonPrimitive?.content)
    }

    @Test
    fun `get grid - unknown id returns 404`() = testApp {
        val token    = getToken()
        val randomId = UUID.randomUUID()
        val response = jsonClient().get("/grids/$randomId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `delete grid - success returns 204`() = testApp {
        val client = jsonClient()
        val token  = getToken()

        val createResponse = client.post("/grids") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(CreateGridRequest("Grid To Delete", GridPhase.PHASE_1))
        }
        val gridId = createResponse.body<JsonObject>()["id"]!!.jsonPrimitive.content

        val deleteResponse = client.delete("/grids/$gridId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.NoContent, deleteResponse.status)

        // Verifica ca nu mai exista
        val getResponse = client.get("/grids/$gridId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }

    // ══════════════════════════════════════════════════════════════════════
    // INVERTER
    // ══════════════════════════════════════════════════════════════════════

    @Test
    fun `create inverter - success returns 201`() = testApp {
        val client = jsonClient()
        val token  = getToken()

        val gridId = client.post("/grids") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(CreateGridRequest("Test Grid", GridPhase.PHASE_1))
        }.body<JsonObject>()["id"]!!.jsonPrimitive.content

        val response = client.post("/grids/$gridId/inverters") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(CreateInverterRequest(
                label    = "Inv1",
                index    = 0,
                pMax     = 1.0,
                p0Ref    = 0.5,
                kdroopP  = 0.02,
                epsilonP = 0.20
            ))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<JsonObject>()
        assertEquals("Inv1", body["label"]?.jsonPrimitive?.content)
        assertEquals(0, body["index"]?.jsonPrimitive?.int)
        assertEquals(gridId, body["gridId"]?.jsonPrimitive?.content)
    }

    @Test
    fun `create inverter - invalid grid returns 404`() = testApp {
        val token    = getToken()
        val randomId = UUID.randomUUID()
        val response = jsonClient().post("/grids/$randomId/inverters") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(CreateInverterRequest(label = "Inv1", index = 0))
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `create inverter - invalid epsilon returns 400`() = testApp {
        val client = jsonClient()
        val token  = getToken()

        val gridId = client.post("/grids") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(CreateGridRequest("Test Grid", GridPhase.PHASE_1))
        }.body<JsonObject>()["id"]!!.jsonPrimitive.content

        val response = client.post("/grids/$gridId/inverters") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(CreateInverterRequest(
                label    = "Inv1",
                index    = 0,
                epsilonP = 0.9
            ))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `list inverters - returns all inverters for grid`() = testApp {
        val client = jsonClient()
        val token  = getToken()

        val gridId = client.post("/grids") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(CreateGridRequest("Test Grid", GridPhase.PHASE_2))
        }.body<JsonObject>()["id"]!!.jsonPrimitive.content

        // Adauga 3 invertoare
        listOf("Inv1", "Inv2", "Inv3").forEachIndexed { index, label ->
            client.post("/grids/$gridId/inverters") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(CreateInverterRequest(label = label, index = index))
            }
        }

        val response = client.get("/grids/$gridId/inverters") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(3, response.body<JsonArray>().size)
    }

    @Test
    fun `update inverter - partial update succeeds`() = testApp {
        val client = jsonClient()
        val token  = getToken()

        val gridId = client.post("/grids") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(CreateGridRequest("Test Grid", GridPhase.PHASE_1))
        }.body<JsonObject>()["id"]!!.jsonPrimitive.content

        val inverterId = client.post("/grids/$gridId/inverters") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(CreateInverterRequest(label = "Inv1", index = 0, kdroopP = 0.02))
        }.body<JsonObject>()["id"]!!.jsonPrimitive.content

        // Actualizeaza doar kdroopP
        val response = client.put("/grids/$gridId/inverters/$inverterId") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(UpdateInverterRequest(kdroopP = 0.05))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(0.05, response.body<JsonObject>()["kdroopP"]?.jsonPrimitive?.double)
    }

    @Test
    fun `delete inverter - success returns 204`() = testApp {
        val client = jsonClient()
        val token  = getToken()

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

        val response = client.delete("/grids/$gridId/inverters/$inverterId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `get grid - includes inverters in response`() = testApp {
        val client = jsonClient()
        val token  = getToken()

        val gridId = client.post("/grids") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(CreateGridRequest("Test Grid", GridPhase.PHASE_1))
        }.body<JsonObject>()["id"]!!.jsonPrimitive.content

        // Adauga 2 invertoare
        listOf("Inv1" to 0, "Inv2" to 1).forEach { (label, index) ->
            client.post("/grids/$gridId/inverters") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(CreateInverterRequest(label = label, index = index))
            }
        }

        val response = client.get("/grids/$gridId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val inverters = response.body<JsonObject>()["inverters"]?.jsonArray
        assertEquals(2, inverters?.size)
    }
}