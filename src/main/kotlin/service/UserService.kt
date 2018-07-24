package service

import db.Database.dbQuery
import model.User
import model.UserTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

class UserService {

    suspend fun getUser(id: Long): User? = dbQuery {
        UserTable.select { UserTable.id eq id }
            .mapNotNull { it.toUser() }
            .singleOrNull()
    }

    suspend fun getUser(name: String): User? = dbQuery {
        UserTable.select { UserTable.name eq name }
            .mapNotNull { it.toUser() }
            .singleOrNull()
    }

    suspend fun getUsers(): List<User> = dbQuery {
        model.UserTable.selectAll().limit(100)
            .map { it.toUser() }
    }

    suspend fun addUser(username: String): User {
        var userId: Long? = 0

        dbQuery {
            userId = model.UserTable.insert {
                it[name] = username
            } get UserTable.id
        }

        return getUser(userId!!)!!
    }

    private fun ResultRow.toUser(): User =
        User(
            id = this[UserTable.id],
            name = this[UserTable.name]
        )
}