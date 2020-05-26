package com.github.nikvoloshin.exposed.verification.base.handlers

import com.github.nikvoloshin.exposed.verification.AuditStatementHandler
import com.github.nikvoloshin.exposed.verification.AuditManager
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.transaction

internal open class InsertHandler(
    manager: AuditManager,
    transaction: Transaction,
    override val statement: InsertStatement<*>
) : AuditStatementHandler(manager, transaction, statement) {
    protected val fetchedRows = mutableListOf<ResultRow>()

    override fun afterExecution(): Unit = with(manager.target) {
        val insertedRows = statement.resultedValues!!
        if (insertedRows.isEmpty()) return

        val table = statement.targets.first()
        val indices = insertedRows.map { it[table.index]!! }
        fetchedRows += table.slice(table.auditColumns).select { table.index inList indices }.toList()
        check(fetchedRows.size == insertedRows.size) { "Count of inserted rows and fetched rows doesn't match" }

        fetchedRows.forEach { row ->
            table.update({ table.index eq row[table.index] }) {
                row[sign] = manager.signer.signRow(row, table)
                it[sign] = row[sign]
            }
        }
    }
}