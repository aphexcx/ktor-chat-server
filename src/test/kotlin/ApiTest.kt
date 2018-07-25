import com.google.gson.Gson
import db.Database
import io.ktor.application.Application
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationResponse
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import model.IncomingMessage
import model.MessagesResponse
import model.UsersResponse
import org.junit.After
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class KtorTest {

    private val defaultMessage = IncomingMessage(user = "superman", text = "hello")
    private val json = "application/json"

    private val gson = Gson()

    @After
    fun clear() {
        //TODO This can use a test Db, if in the future we use a disk-based db instead of an in-memory one like H2.
        Database.dropAll()
        Database.createTables()
    }

    @Test
    fun postMessageTest() = withTestApplication(Application::module) {
        val msg = IncomingMessage(user = "superman", text = "hello")
        sendMessage(msg).let {
            assertEquals(OK_JSON, it.content)
        }
    }

    @Test
    fun getAllMessagesTest() = withTestApplication(Application::module) {
        val msg1 = IncomingMessage(user = "superman", text = "hello")
        val msg2 = IncomingMessage(user = "batman", text = "cripes!")
        sendMessage(msg1)
        sendMessage(msg2)

        get(ALL_MSG_ENDPOINT).let {
            assertEquals(HttpStatusCode.OK, it.status())

            val messages = gson.fromJson(it.content, MessagesResponse::class.java).messages
            messages.forEach { println(it) }

            messages.find { it.user == msg1.user && it.text == msg1.text } ?: fail()
            messages.find { it.user == msg2.user && it.text == msg2.text } ?: fail()
            assertEquals(2, messages.size)
        }
    }

    @Test
    fun getAllUsersTest() = withTestApplication(Application::module) {
        val msg1 = IncomingMessage(user = "superman", text = "hello")
        val msg2 = IncomingMessage(user = "batman", text = "cripes!")
        sendMessage(msg1)
        sendMessage(msg2)

        get(ALL_USER_ENDPOINT).let {
            assertEquals(HttpStatusCode.OK, it.status())

            val users = gson.fromJson(it.content, UsersResponse::class.java).users
            users.forEach { println(it) }

            users.find { it.name == msg1.user } ?: fail()
            users.find { it.name == msg2.user } ?: fail()
            assertEquals(2, users.size)
        }
    }

    private fun TestApplicationEngine.sendMessage(message: IncomingMessage = defaultMessage): TestApplicationResponse {
        post(message).let {
            assertEquals(io.ktor.http.HttpStatusCode.OK, it.status())
            kotlin.io.println(it.content)
            return it
        }
    }

    private fun TestApplicationEngine.get(endpoint: String): TestApplicationResponse =
        handleRequest(HttpMethod.Get, endpoint) {
            addHeader("Accept", json)
        }.response

    private fun TestApplicationEngine.post(message: IncomingMessage): TestApplicationResponse =
        handleRequest(io.ktor.http.HttpMethod.Post, MSG_ENDPOINT) {
            body = gson.toJson(message)
            addHeader("Content-Type", json)
            addHeader("Accept", json)
        }.response

    companion object {
        private val OK_JSON: String = """{"ok": "true"}"""
        private val MSG_ENDPOINT: String = "/message"
        private val ALL_MSG_ENDPOINT: String = "/messages"
        private val ALL_USER_ENDPOINT: String = "/users"
    }
}