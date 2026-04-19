import presentation.plugins.configureAuthentication
import presentation.plugins.configureDatabases
import presentation.plugins.configureRouting
import presentation.plugins.configureSerialization
import presentation.plugins.configureStatusPages
import io.ktor.server.application.*
import io.ktor.server.netty.*
import presentation.plugins.configureDI

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    configureDI()
    configureDatabases()
    configureSerialization()
    configureAuthentication()
    configureStatusPages()
    configureRouting()
}