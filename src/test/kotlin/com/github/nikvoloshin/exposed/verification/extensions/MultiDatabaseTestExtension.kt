package com.github.nikvoloshin.exposed.verification.extensions

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.extension.*
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
        val schema = schemaProperty?.getter?.call(testInstance) as Collection<Table>

        return databases.map {
            MultiDatabaseTestTemplateInvocationContext(it.first, it.second, schema)
        }.map { it as TestTemplateInvocationContext }.stream()
    }

}

private class MultiDatabaseTestTemplateInvocationContext(
    private val displayName: String,
    private val db: Database,
    private val schema: Collection<Table>?
) : TestTemplateInvocationContext {
    override fun getDisplayName(invocationIndex: Int) = displayName

    override fun getAdditionalExtensions() = mutableListOf<Extension>().apply {
        if (schema != null) add(SchemaController(db, schema))
        add(DatabaseParameterResolver(db))
    }

}

private class SchemaController(private val db: Database, private val schema: Collection<Table>) : BeforeTestExecutionCallback, AfterTestExecutionCallback {
    override fun beforeTestExecution(context: ExtensionContext?) = transaction(db) {
        SchemaUtils.create(*schema.toTypedArray())
    }

    override fun afterTestExecution(context: ExtensionContext?) = transaction(db) {
        SchemaUtils.drop(*schema.toTypedArray())
    }

}

private class DatabaseParameterResolver(private val db: Database) : ParameterResolver {
    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext) =
        parameterContext.parameter.type == Database::class.java

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext) = db

}