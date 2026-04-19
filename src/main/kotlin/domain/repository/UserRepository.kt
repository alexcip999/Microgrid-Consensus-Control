package domain.repository

import domain.model.user.User
import java.util.UUID

interface UserRepository {
    fun findById(id: UUID): User?
    fun findByEmail(email: String): User?
    fun save(user: User): User
}