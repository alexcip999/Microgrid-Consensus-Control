package domain.usecase.auth

import domain.model.token.TokenClaims
import domain.model.user.User
import domain.repository.UserRepository
import domain.service.EncryptService
import domain.service.TokenService

class LoginUseCase(
    private val userRepository: UserRepository,
    private val encryptService: EncryptService,
    private val tokenService: TokenService
) {
    data class Input(
        val email: String,
        val password: String
    )

    data class Output(
        val user: User,
        val token: String,
    )

    fun execute(input: Input): Output {
        val user = userRepository.findByEmail(input.email)
            ?: throw IllegalArgumentException("Invalid email or password")

        require(encryptService.verify(input.password, user.passwordHash)) {
            "Invalid email or password"
        }

        val token = tokenService.generate(TokenClaims(user.id, user.role))

        return Output(user = user, token = token)
    }
}