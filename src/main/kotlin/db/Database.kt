package db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import kotlinx.coroutines.experimental.withContext
import model.MessageTable
import model.UserTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SchemaUtils.drop
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.coroutines.experimental.CoroutineContext

object Database {
    fun init() {
        // Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        Database.connect(hikari())
        createTables()
    }

    fun createTables() {
        transaction {
            create(UserTable)
            create(MessageTable)
        }
    }

    fun dropAll() {
        transaction {
            drop(UserTable, MessageTable)
        }
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = "org.h2.Driver"
        config.jdbcUrl = "jdbc:h2:mem:test;mode=MySQL"
        config.maximumPoolSize = 3
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()
        return HikariDataSource(config)
    }

    private val dispatcher: CoroutineContext =
        newFixedThreadPoolContext(5, "database-pool")

    suspend fun <T> dbQuery(block: () -> T): T =
        withContext(dispatcher) {
            transaction { block() }
        }
}

