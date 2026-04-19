package application.usecase

import application.service.PasswordServiceImpl
import domain.model.token.TokenClaims
import domain.model.user.User
import domain.model.user.UserRole
import domain.repository.UserRepository
import domain.service.EncryptService
import domain.service.TokenService
import java.time.LocalDateTime
import java.util.UUID

class RegisterUseCase(
    private val encryptService: EncryptService,
    private val userRepository: UserRepository,
    private val tokenService: TokenService
) {
    data class Input(
        val email: String,
        val password: String,
        val role: UserRole = UserRole.VIEWER
    )

    data class Output(
        val user: User,
        val token: String,
    )

    fun execute(input: Input): Output {
        require(input.email.isNotBlank()) { "Email cannot be blank" }
        require(input.email.contains("@")) { "Invalid email format" }
        require(input.password.length >= 8) { "Password must be at least 8 characters" }

        val existing = userRepository.findByEmail(input.email)
        require(existing == null) { "Email already in use" }

        val user = User(
            id = UUID.randomUUID(),
            email = input.email,
            passwordHash = encryptService.hash(input.password),
            role = input.role,
            createdAt = LocalDateTime.now()
        )

        val saved = userRepository.save(user)
        val token = tokenService.generate(TokenClaims(saved.id, saved.role))

        return Output(user = saved, token = token)
    }
}