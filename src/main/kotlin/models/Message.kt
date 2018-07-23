package models

data class Message(
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

