package model

import org.jetbrains.exposed.sql.Table

object MessageTable : Table() {
    val id = long("id").primaryKey().autoIncrement()
    val timestamp = long("timestamp")
    val user = long("user") references UserTable.id
    val text = text("text")
}

data class Message(
    @Transient val id: Long,
    val timestamp: Long,
    val user: String,
    val text: String
)

data class IncomingMessage(
    val user: String,
    val text: String
)

data class MessagesResponse(
    val messages: List<Message>
)

