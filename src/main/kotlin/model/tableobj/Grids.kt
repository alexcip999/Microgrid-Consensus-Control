package com.microgrid.model.tableobj

import com.microgrid.model.enums.GridPhase
import com.microgrid.model.enums.GridTopology
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime

object Grids : UUIDTable("grids") {
    val name      = varchar("name", 100)
    val phase     = enumerationByName("phase", 10, GridPhase::class)
    val topology  = enumerationByName("topology", 10, GridTopology::class)
    val fNom      = double("f_nom").default(60.0)      // Hz
    val vNom      = double("v_nom").default(1.0)       // pu
    val ownerId   = reference("owner_id", Users)
    val createdAt = datetime("created_at")
}