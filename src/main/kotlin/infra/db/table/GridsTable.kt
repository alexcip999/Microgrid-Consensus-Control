package infra.db.table

import domain.model.grid.GridPhase
import domain.model.grid.GridTopology
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime

object GridsTable : UUIDTable("grids") {
    val name      = varchar("name", 100)
    val phase     = enumerationByName("phase", 10, GridPhase::class)
    val topology  = enumerationByName("topology", 10, GridTopology::class)
    val fNom      = double("f_nom").default(60.0)
    val vNom      = double("v_nom").default(1.0)
    val ownerId   = reference("owner_id", UsersTable)
    val createdAt = datetime("created_at")
}