package service

import db.Database
import model.User
import model.UserTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll

class UserService {

    suspend fun getUsers(): List<User> = Database.dbQuery {
        model.UserTable.selectAll().limit(100)
            .map { it.toUser() }
    }

    private fun ResultRow.toUser(): User =
        User(
            id = this[UserTable.id],
            name = this[UserTable.name]
        )
}