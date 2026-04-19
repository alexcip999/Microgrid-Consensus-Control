package infra.db.entity

import domain.model.user.User
import infra.db.table.UsersTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class UserEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserEntity>(UsersTable)

    var email by UsersTable.email
    var passwordHash by UsersTable.passwordHash
    var role by UsersTable.role
    var createdAt by UsersTable.createdAt

    fun toDomain(): User = User(
        id = id.value,
        email = email,
        passwordHash = passwordHash,
        role = role,
        createdAt = createdAt
    )
}