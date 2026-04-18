import com.microgrid.plugins.configureAuthentication
import com.microgrid.plugins.configureCORS
import com.microgrid.plugins.configureDatabases
import com.microgrid.plugins.configureRouting
import com.microgrid.plugins.configureSerialization
import com.microgrid.plugins.configureStatusPages
import com.microgrid.plugins.configureWebSockets
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    configureDatabases()
    configureSerialization()
    configureAuthentication()
    configureCORS()
    configureStatusPages()
    configureWebSockets()
    configureRouting()
}