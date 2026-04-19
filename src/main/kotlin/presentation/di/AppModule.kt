package presentation.di

import application.service.JwtServiceImpl
import application.service.PasswordServiceImpl
import application.usecase.GetCurrentUserUseCase
import application.usecase.LoginUseCase
import application.usecase.RegisterUseCase
import domain.repository.UserRepository
import domain.service.EncryptService
import domain.service.TokenService
import infra.db.UserRepositoryImpl
import org.koin.dsl.module

val appModule = module {
    // ── Infrastructure ─────────────────────────────────
    single<UserRepository> { UserRepositoryImpl() }

    // ── Services ───────────────────────────────────────
    single<EncryptService> { PasswordServiceImpl() }
    single<TokenService> { JwtServiceImpl(get()) }
    // ── Use cases ──────────────────────────────────────
    single { RegisterUseCase(get(), get(), get()) }
    single { LoginUseCase(get(), get(), get()) }
    single { GetCurrentUserUseCase(get()) }
}