package com.github.nikvoloshin.exposed.verification.base

import com.github.nikvoloshin.exposed.verification.utils.randomSalt64
import com.github.nikvoloshin.exposed.verification.utils.toHex
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.security.MessageDigest
import kotlin.properties.ReadOnlyProperty
import kotlin.random.Random
import kotlin.reflect.KProperty

open class AuditTarget internal constructor(
    val database: Database,
    val schema: String,
    private val targets: List<Pair<Table, List<Column<*>>>>,
    private val obscureNames: Boolean
) {
    val tables = targets.map { it.first }
    private val additionalTables = mutableListOf<Table>()
    private val extensionColumns = mutableListOf<ExtensionColumn<*>>()

    val Table.index by registerColumn { uuid(name("audit_index")).autoGenerate().nullable() }
    val Table.sign by registerColumn { varchar(name("audit_sign"), 128).nullable() }
    val Table.salt by registerColumn { varchar(name("audit_salt"), 64).clientDefault { Random.Default.randomSalt64() }.nullable() }

    val Table.contentColumns
        get() = targets.first { it.first == this }.let { it.second - additionalColumns + it.first.index + it.first.salt }.distinct()

    val Table.additionalColumns
        get() = extensionColumns.map { it.getValue(this, ::tables) }

    val Table.auditColumns
        get() = (additionalColumns + contentColumns).distinct()

    protected fun name(name: String): String = name.takeIf { !obscureNames }
        ?: MessageDigest.getInstance("SHA-256").digest(name.toByteArray()).toHex().take(15)

    fun initializeSchema() {
        requireTransaction(TransactionManager.current())

        SchemaUtils.createMissingTablesAndColumns(
            *(tables + additionalTables).toTypedArray(),
            inBatch = true
        )
    }

    protected fun registerTable(table: Table) {
        additionalTables += table
    }

    protected fun <T> registerColumn(registerColumn: Table.() -> Column<T>): ReadOnlyProperty<Table, Column<T>> {
        val name = tables.map { it.registerColumn() }.first().name
        return ExtensionColumn<T>(name).also { extensionColumns += it }
    }

    internal class ExtensionColumn<T>(private val columnName: String) : ReadOnlyProperty<Table, Column<T>> {
        @Suppress("UNCHECKED_CAST")
        override fun getValue(thisRef: Table, property: KProperty<*>): Column<T> {
            return thisRef.columns.find { it.name == columnName } as? Column<T>
                ?: throw IllegalArgumentException("Column $columnName is not registered in table ${thisRef.tableName}")
        }
    }

}

internal fun AuditTarget.matches(transaction: Transaction = TransactionManager.current()) =
    transaction.let { database == it.db && schema == it.connection.schema }

internal fun AuditTarget.matches(transaction: Transaction = TransactionManager.current(), table: Table) =
    matches(transaction) && table in tables

internal fun AuditTarget.matchingTables(transaction: Transaction = TransactionManager.current(), tables: List<Table>) =
    tables.filter { matches(transaction, it) }.distinct()

internal fun AuditTarget.requireTransaction(transaction: Transaction = TransactionManager.current()) =
    require(matches(transaction)) { "Current transaction doesn't correspond to this auditor" }

internal fun AuditTarget.requireTable(transaction: Transaction = TransactionManager.current(), table: Table) {
    requireTransaction(transaction)
    require(matches(transaction, table)) { "Table ${table.tableName} doesn't correspond to this auditor" }
}
