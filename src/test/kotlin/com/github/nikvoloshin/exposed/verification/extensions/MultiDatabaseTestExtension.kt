package com.github.nikvoloshin.exposed.verification.extensions

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager
import org.junit.jupiter.api.extension.*
import org.testcontainers.containers.JdbcDatabaseContainer
import java.util.stream.Stream
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties

class MultiDatabaseTestExtension : TestTemplateInvocationContextProvider {
    override fun supportsTestTemplate(context: ExtensionContext): Boolean =
        context.testClass.map { it.isAnnotationPresent(MultiDatabaseTest::class.java) }.orElse(false)

    @ExperimentalStdlibApi
    @Suppress("UNCHECKED_CAST")
    override fun provideTestTemplateInvocationContexts(context: ExtensionContext): Stream<TestTemplateInvocationContext> {
        val testInstance = context.requiredTestInstance!!
        val schemaProperty = testInstance::class.memberProperties.firstOrNull { it.hasAnnotation<Schema>() }
        val schema = schemaProperty?.getter?.call(testInstance) as Collection<Table>?

        return containers.map {
            MultiDatabaseTestTemplateInvocationContext(it.first, it.second, schema)
        }.map { it as TestTemplateInvocationContext }.stream()
    }

}

private class MultiDatabaseTestTemplateInvocationContext(
    private val displayName: String,
    private val container: JdbcDatabaseContainer<Nothing>,
    private val schema: Collection<Table>?
) : TestTemplateInvocationContext {
    override fun getDisplayName(invocationIndex: Int) = displayName

    override fun getAdditionalExtensions() = mutableListOf<Extension>(DatabaseController(container, schema))

}

private class DatabaseController(
    private val container: JdbcDatabaseContainer<Nothing>,
    private val schema: Collection<Table>?
) : BeforeEachCallback, AfterEachCallback {
    private lateinit var db: Database

    override fun beforeEach(context: ExtensionContext) {
        db = Database.connect(container.jdbcUrl, container.driverClassName, container.username, container.password)
        TransactionManager.resetCurrent(db.transactionManager)

        if (schema != null) {
            transaction {
                SchemaUtils.create(*schema.toTypedArray())
            }
        }
    }

    override fun afterEach(context: ExtensionContext) {
        if (schema != null) {
            transaction {
                SchemaUtils.drop()
            }
        }

        TransactionManager.closeAndUnregister(db)
        TransactionManager.resetCurrent(null)
    }
}
