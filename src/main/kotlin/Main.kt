import db.Database
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.pipeline.PipelineContext
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import model.IncomingMessage
import model.MessagesResponse
import model.User
import model.UsersResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import service.MessageService
import service.UserService

val LOG: Logger = LoggerFactory.getLogger("ktor-chat-server")

fun unixTime(): Long = System.currentTimeMillis() / 1000L

fun Application.module() {
    install(DefaultHeaders)
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }

    val messageService = MessageService()
    val userService = UserService()
    Database.init()

    routing {
        get("/messages") {
            catchException {
                call.respond(MessagesResponse(messageService.getRecentMessages().map {
                    //TODO could modify the SQL query to include the users rather than patching them in later
                    it.copy(user = userService.getUser(it.user.toLong())!!.name)
                }))
            }
        }

        post("/message") {
            catchException {
                val received = call.receive<IncomingMessage>()
                println("Received Post Request: $received")
                val existingUser: User? = userService.getUser(received.user)

                val userId: Long =
                    existingUser?.id ?: userService.addUser(received.user).id

                messageService.addMessage(received, userId)
                call.respondOkJson()
            }
        }

        get("/users") {
            catchException {
                call.respond(UsersResponse(userService.getUsers()))
            }
        }
    }
}

fun main(args: Array<String>) {
    embeddedServer(Netty,
        port = 8081,
        watchPaths = listOf("MainKt"),
        module = Application::module)
        .start(wait = true)
}

private suspend fun ApplicationCall.respondOkJson(value: Boolean = true) =
    respond("""{"ok": "$value"}""")

private suspend fun <R> PipelineContext<*, ApplicationCall>.catchException(block: suspend () -> R): R? {
    return try {
        block()
    } catch (e: Exception) {
        call.respondText(
            """{"error":"$e"}""",
            ContentType.parse("application/json"),
            HttpStatusCode.InternalServerError
        )
        null
    }
}