package presentation.di

import domain.repository.GridRepository
import domain.repository.InverterRepository
import domain.repository.SimulationRepository
import infra.service.JwtServiceImpl
import infra.service.PasswordServiceImpl
import domain.usecase.auth.GetCurrentUserUseCase
import domain.usecase.auth.LoginUseCase
import domain.usecase.auth.RegisterUseCase
import domain.repository.UserRepository
import domain.service.EncryptService
import domain.service.TokenService
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
import infra.repository.GridRepositoryImpl
import infra.repository.InverterRepositoryImpl
import infra.repository.SimulationRepositoryImpl
import infra.repository.UserRepositoryImpl
import org.koin.dsl.module

val appModule = module {
    // ── Infrastructure ─────────────────────────────────
    single<UserRepository> { UserRepositoryImpl() }
    single<GridRepository> { GridRepositoryImpl() }
    single<InverterRepository> { InverterRepositoryImpl() }
    single<SimulationRepository> { SimulationRepositoryImpl() }

    // ── Services ───────────────────────────────────────
    single<EncryptService> { PasswordServiceImpl() }
    single<TokenService> { JwtServiceImpl(get()) }
    // ── Use cases ──────────────────────────────────────

    // ── Auth use cases ─────────────────────────────────
    single { RegisterUseCase(get(), get(), get()) }
    single { LoginUseCase(get(), get(), get()) }
    single { GetCurrentUserUseCase(get()) }

    // ── Grid use cases ─────────────────────────────────
    single { CreateGridUseCase(get()) }
    single { GetGridUseCase(get()) }
    single { ListGridsUseCase(get()) }
    single { DeleteGridUseCase(get()) }

    // ── Inverter use cases ─────────────────────────────
    single { CreateInverterUseCase(get(), get()) }
    single { GetInverterUseCase(get()) }
    single { ListInvertersUseCase(get()) }
    single { UpdateInverterUseCase(get()) }
    single { DeleteInverterUseCase(get()) }

    // ── Simulation use cases ───────────────────────────────────
    single { CreateSimulationUseCase(get(), get()) }
    single { GetSimulationUseCase(get()) }
    single { ListSimulationsUseCase(get()) }
    single { StartSimulationUseCase(get()) }
    single { StopSimulationUseCase(get()) }
    single { GetSimulationStatusUseCase(get()) }
}