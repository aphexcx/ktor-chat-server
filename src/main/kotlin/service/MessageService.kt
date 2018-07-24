package service

import db.Database.dbQuery
import db.insertOrUpdate
import model.IncomingMessage
import model.Message
import model.MessageTable
import model.UserTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import unixTime

class MessageService {

    suspend fun getRecentMessages(): List<Message> = dbQuery {
        MessageTable.selectAll().limit(100)
            .map { it.toMessage() }
    }

    suspend fun getMessage(id: Long): Message? = dbQuery {
        MessageTable.select { (MessageTable.id eq id) }
            .mapNotNull { it.toMessage() }
            .singleOrNull()
    }

    suspend fun addMessage(incomingMessage: IncomingMessage): Message {
        var key: Long? = 0
        var userId: Long? = 0

        //
        dbQuery {
            userId = UserTable.insertOrUpdate(UserTable.name) {
                it[name] = incomingMessage.user
            } get UserTable.id
        }

        dbQuery {
            key = MessageTable.insert {
                it[timestamp] = unixTime()
                it[user] = userId!!
                it[text] = incomingMessage.text
            } get MessageTable.id
        }

        return getMessage(key!!)!!
    }

    private fun ResultRow.toMessage(): Message =
        Message(
            id = this[MessageTable.id],
            timestamp = this[MessageTable.timestamp],
            user = this[MessageTable.user].toString(), //TODO may need to convert to user
            text = this[MessageTable.text]
        )
}
