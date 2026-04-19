package ktor.presentation.routes

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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import presentation.dto.request.LoginRequest
import presentation.dto.request.RegisterRequest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class AuthRoutesTest {

    private val fakeUserRepository = FakeUserRepository()

    @BeforeTest
    fun setup() = fakeUserRepository.clear()

    private fun testApp(block: suspend ApplicationTestBuilder.() -> Unit) =
        testApplication {
            application { testModule(fakeUserRepository) }
            block()
        }

    private fun ApplicationTestBuilder.jsonClient() =
        createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
        }

    @Test
    fun `register - success returns 201 with token`() = testApp {
        val response = jsonClient().post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("test@microgrid.com", "password123"))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<JsonObject>()
        assertNotNull(body["token"])
        assertNotNull(body["userId"])
        assertEquals("VIEWER", body["role"]?.jsonPrimitive?.content)
    }

    @Test
    fun `register - duplicate email returns 400`() = testApp {
        val client = jsonClient()
        val request = RegisterRequest("duplicate@microgrid.com", "password123")

        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.body<JsonObject>()
        assertEquals("BAD_REQUEST", body["error"]?.jsonPrimitive?.content)
    }

    @Test
    fun `register - blank email returns 400`() = testApp {
        val response = jsonClient().post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("", "password123"))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `register - short password returns 400`() = testApp {
        val response = jsonClient().post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("test@microgrid.com", "short"))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `register - invalid email format returns 400`() = testApp {
        val response = jsonClient().post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("not-an-email", "password123"))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `login - success returns 200 with token`() = testApp {
        val client = jsonClient()

        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("login@microgrid.com", "password123"))
        }

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("login@microgrid.com", "password123"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<JsonObject>()
        assertNotNull(body["token"])
        assertNotNull(body["userId"])
    }

    @Test
    fun `login - wrong password returns 400`() = testApp {
        val client = jsonClient()

        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("wrong@microgrid.com", "password123"))
        }

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("wrong@microgrid.com", "wrongpassword"))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `login - unknown email returns 400`() = testApp {
        val response = jsonClient().post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("unknown@microgrid.com", "password123"))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `me - valid token returns 200 with user data`() = testApp {
        val client = jsonClient()

        val registerResponse = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("me@microgrid.com", "password123"))
        }
        val token = registerResponse.body<JsonObject>()["token"]
            ?.jsonPrimitive?.content!!

        val response = client.get("/auth/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<JsonObject>()
        assertEquals("me@microgrid.com", body["email"]?.jsonPrimitive?.content)
        assertNotNull(body["id"])
        assertNotNull(body["createdAt"])
    }

    @Test
    fun `me - no token returns 401`() = testApp {
        val response = jsonClient().get("/auth/me")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `me - invalid token returns 401`() = testApp {
        val response = jsonClient().get("/auth/me") {
            header(HttpHeaders.Authorization, "Bearer invalid.token.here")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}