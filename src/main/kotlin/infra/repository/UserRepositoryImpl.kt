package infra.repository

import domain.model.user.User
import domain.repository.UserRepository
import infra.db.entity.UserEntity
import infra.db.table.UsersTable
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class UserRepositoryImpl : UserRepository {
    override fun findById(id: UUID): User? = transaction {
        UserEntity.Companion.findById(id)?.toDomain()
    }

    override fun findByEmail(email: String): User? = transaction {
        UserEntity.Companion
            .find { UsersTable.email eq email }
            .firstOrNull()
            ?.toDomain()
    }

    override fun save(user: User): User = transaction {
        UserEntity.Companion.new(user.id) {
            email = user.email
            passwordHash = user.passwordHash
            role = user.role
            createdAt = user.createdAt
        }.toDomain()
    }
}