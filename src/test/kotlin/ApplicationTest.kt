package ktor

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import ktor.presentation.routes.FakeUserRepository
import ktor.presentation.routes.testModule
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun `health endpoint returns 200`() = testApplication {
        application {
            testModule(FakeUserRepository())
        }
        client.get("/health").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }
}