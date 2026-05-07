package ktor.presentation.routes

import infra.service.JwtServiceImpl
import infra.service.PasswordServiceImpl
import domain.usecase.auth.GetCurrentUserUseCase
import domain.usecase.auth.LoginUseCase
import domain.usecase.auth.RegisterUseCase
import domain.model.token.TokenConfig
import domain.repository.UserRepository
import domain.model.user.User
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.routing.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import domain.model.grid.Grid
import domain.model.grid.inverter.Inverter
import domain.model.simulation.Simulation
import domain.model.simulation.SimulationStatus
import domain.repository.GridRepository
import domain.repository.InverterRepository
import domain.repository.SimulationRepository
import domain.usecase.grid.CreateGridUseCase
import domain.usecase.grid.DeleteGridUseCase
import domain.usecase.grid.GetGridUseCase
import domain.usecase.grid.ListGridsUseCase
import domain.usecase.inverter.CreateInverterUseCase
import domain.usecase.inverter.DeleteInverterUseCase
import domain.usecase.inverter.GetInverterUseCase
import domain.usecase.inverter.ListInvertersUseCase
import domain.usecase.inverter.UpdateInverterUseCase
import domain.usecase.simulation.CreateSimulationUseCase
import domain.usecase.simulation.GetSimulationStatusUseCase
import domain.usecase.simulation.GetSimulationUseCase
import domain.usecase.simulation.ListSimulationsUseCase
import domain.usecase.simulation.StartSimulationUseCase
import domain.usecase.simulation.StopSimulationUseCase
import presentation.plugins.ErrorResponse
import kotlinx.serialization.json.Json
import presentation.plugins.ValidationException
import presentation.routes.authRoutes
import presentation.routes.gridRoutes
import presentation.routes.simulationRoutes
import java.util.UUID

val testTokenConfig = TokenConfig(
    secret = "test-secret-key-minimum-32-characters",
    issuer = "microgrid-test",
    audience = "microgrid-mobile-test",
    expiresInMs = 86400000L
)

class FakeUserRepository : UserRepository {
    private val store = mutableMapOf<UUID, User>()

    override fun findById(id: UUID) = store[id]
    override fun findByEmail(email: String) =
        store.values.firstOrNull { it.email == email }

    override fun save(user: User): User {
        store[user.id] = user
        return user
    }

    fun clear() = store.clear()
}

class FakeGridRepository : GridRepository {
    private val store = mutableMapOf<UUID, Grid>()
    override fun findById(id: UUID) = store[id]
    override fun findAllByOwner(ownerId: UUID) = store.values.filter { it.ownerId == ownerId }
    override fun save(grid: Grid): Grid = grid.also { store[it.id] = it }
    override fun delete(id: UUID): Boolean = store.remove(id) != null
    fun clear() = store.clear()
}

class FakeInverterRepository : InverterRepository {
    private val store = mutableMapOf<UUID, Inverter>()
    override fun findById(id: UUID) = store[id]
    override fun findAllByGrid(gridId: UUID) = store.values.filter { it.gridId == gridId }
    override fun save(inverter: Inverter): Inverter = inverter.also { store[it.id] = it }
    override fun update(inverter: Inverter): Inverter = inverter.also { store[it.id] = it }
    override fun delete(id: UUID): Boolean = store.remove(id) != null
    fun clear() = store.clear()
}

class FakeSimulationRepository : SimulationRepository {
    private val store = mutableMapOf<UUID, Simulation>()

    override fun findById(id: UUID): Simulation? =
        store[id]

    override fun findAllByGrid(gridId: UUID): List<Simulation> =
        store.values
            .filter { it.gridId == gridId }
            .sortedByDescending { it.startedAt }

    override fun findActiveByGrid(gridId: UUID): Simulation? =
        store.values.firstOrNull {
            it.gridId == gridId &&
                    it.status in listOf(SimulationStatus.PENDING, SimulationStatus.RUNNING)
        }

    override fun save(simulation: Simulation): Simulation =
        simulation.also { store[it.id] = it }

    override fun update(simulation: Simulation): Simulation =
        simulation.also { store[it.id] = it }

    fun clear() = store.clear()
}

fun Application.testModule(
    userRepository: FakeUserRepository,
    gridRepository: FakeGridRepository = FakeGridRepository(),
    inverterRepository: FakeInverterRepository = FakeInverterRepository(),
    simulationRepository: FakeSimulationRepository = FakeSimulationRepository()
) {
    val passwordService = PasswordServiceImpl()
    val tokenService = JwtServiceImpl(testTokenConfig)

    // Auth
    val register = RegisterUseCase(passwordService, userRepository, tokenService)
    val login = LoginUseCase(userRepository, passwordService, tokenService)
    val getCurrentUser = GetCurrentUserUseCase(userRepository)

    // Grid
    val createGrid = CreateGridUseCase(gridRepository)
    val getGrid = GetGridUseCase(gridRepository)
    val listGrids = ListGridsUseCase(gridRepository)
    val deleteGrid = DeleteGridUseCase(gridRepository)

    // Inverter
    val createInverter = CreateInverterUseCase(inverterRepository, gridRepository)
    val getInverter = GetInverterUseCase(inverterRepository)
    val listInverters = ListInvertersUseCase(inverterRepository)
    val updateInverter = UpdateInverterUseCase(inverterRepository)
    val deleteInverter = DeleteInverterUseCase(inverterRepository)

    // Simulation
    val createSimulation = CreateSimulationUseCase(simulationRepository, gridRepository)
    val getSimulation = GetSimulationUseCase(simulationRepository)
    val listSimulations = ListSimulationsUseCase(simulationRepository)
    val startSimulation = StartSimulationUseCase(simulationRepository)
    val stopSimulation = StopSimulationUseCase(simulationRepository)
    val getSimulationStatus = GetSimulationStatusUseCase(simulationRepository)

    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }

    install(Authentication) {
        jwt("jwt-auth") {
            realm = "microgrid-test"
            verifier(
                JWT.require(Algorithm.HMAC256(testTokenConfig.secret))
                    .withAudience(testTokenConfig.audience)
                    .withIssuer(testTokenConfig.issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("userId").asString() != null)
                    JWTPrincipal(credential.payload)
                else null
            }
        }
    }

    install(StatusPages) {
        exception<presentation.plugins.NotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse("NOT_FOUND", cause.message ?: "Resource not found")
            )
        }
        exception<ValidationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("BAD_REQUEST", cause.message ?: "Validation failed")
            )
        }
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("BAD_REQUEST", cause.message ?: "Invalid input")
            )
        }
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred")
            )
        }
    }

    routing {
        get("/health") { call.respond(HttpStatusCode.OK, mapOf("status" to "ok")) }
        authRoutes(register, login, getCurrentUser)
        gridRoutes(
            createGrid, getGrid, listGrids, deleteGrid,
            createInverter, getInverter, listInverters,
            updateInverter, deleteInverter
        )
        simulationRoutes(
            createSimulation, getSimulation, listSimulations,
            startSimulation, stopSimulation, getSimulationStatus
        )
    }
}