package com.microgrid.model.entity

import com.microgrid.model.tableobj.Users
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class UserEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserEntity>(Users)

    var email        by Users.email
    var passwordHash by Users.passwordHash
    var role         by Users.role
    var createdAt    by Users.createdAt
}