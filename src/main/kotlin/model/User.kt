package model

import org.jetbrains.exposed.sql.Table

object UserTable : Table() {
    val id = long("id").primaryKey().autoIncrement()
    val name = varchar("user", 255)
}

//TODO could deserialize according to spec
data class User(
    @Transient val id: Long,
    val name: String
)

data class UsersResponse(val users: List<User>)
