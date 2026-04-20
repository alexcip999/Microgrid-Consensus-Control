package domain.usecase.auth

import domain.model.user.User
import domain.repository.UserRepository
import java.util.UUID

class GetCurrentUserUseCase(
    private val userRepository: UserRepository,
) {
    fun execute(userId: UUID): User =
        userRepository.findById(userId)
            ?: throw NoSuchElementException("User with id $userId not found")
}