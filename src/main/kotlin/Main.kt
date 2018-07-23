import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
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
import models.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

val LOG: Logger = LoggerFactory.getLogger("ktor-chat-server")

val messages: MutableList<Message> = mutableListOf()
val users: MutableSet<User> = mutableSetOf()


fun unixTime(): Long = System.currentTimeMillis() / 1000L

fun main(args: Array<String>) {
    val server = embeddedServer(Netty, port = 8081) {
        install(DefaultHeaders)
        install(CORS) {
            maxAge = Duration.ofDays(1)
        }
        install(ContentNegotiation) {
            gson {
                setPrettyPrinting()
            }
        }

        routing {
            get("/messages") {
                catchException {
                    call.respond(MessagesResponse(messages.take(100)))
                }
            }
            post("/message") {
                catchException {
                    val received = call.receive<IncomingMessage>()
                    println("Received Post Request: $received")
                    messages.add(Message(unixTime(), received.user, received.text))
                    users.add(received.user)
                    call.respondOkJson()
                }
            }
            get("/users") {
                catchException {
                    call.respond(UsersResponse(users.take(100)))
                }
            }
        }
    }
    server.start(wait = true)
}

private suspend fun ApplicationCall.respondOkJson(value: Boolean = true) = respond("""{"ok": "$value"}""")


//TODO
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