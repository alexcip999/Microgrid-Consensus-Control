package com.microgrid.model.tableobj

import com.microgrid.model.enums.UserRole
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime

object Users : UUIDTable("users") {
    val email        = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val role         = enumerationByName("role", 20, UserRole::class)
    val createdAt    = datetime("created_at")
}