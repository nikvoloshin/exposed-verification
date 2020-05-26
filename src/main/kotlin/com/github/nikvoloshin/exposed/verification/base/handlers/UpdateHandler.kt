package com.github.nikvoloshin.exposed.verification.base.handlers

import com.github.nikvoloshin.exposed.verification.AuditStatementHandler
import com.github.nikvoloshin.exposed.verification.AuditManager
import com.github.nikvoloshin.exposed.verification.auditEnabled
import com.github.nikvoloshin.exposed.verification.queries.auditQuery
import com.github.nikvoloshin.exposed.verification.queries.forced
import com.github.nikvoloshin.exposed.verification.utils.randomSalt64
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.BatchUpdateStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import java.util.*
import kotlin.random.Random

internal open class UpdateHandler(
    manager: AuditManager,
    transaction: Transaction,
    override val statement: UpdateStatement
) : AuditStatementHandler(manager, transaction, statement) {
    protected lateinit var table: Table
    protected val fetchedRows = mutableListOf<ResultRow>()
    private val updatedIndices = mutableListOf<UUID>()

    @Suppress("UNCHECKED_CAST")
    override fun beforeExecution(): Unit = with(manager.target) {
        val updatedRowsQuery = if (statement is BatchUpdateStatement) {
            val stmt = statement as BatchUpdateStatement
            val idTable = stmt.table as IdTable<Comparable<Any>>
            val data = stmt.data.map { it.first.value as Comparable<Any> }

            table = stmt.table
            idTable.select { idTable.id inList data }
        } else {
            table = statement.firstDataSet.map { it.first.table }.distinct().singleOrNull() ?: return
            Query(statement.targetsSet, statement.where).apply { statement.limit?.let { limit(it) } }
        }

        updatedIndices += transaction.auditEnabled { auditQuery(updatedRowsQuery).forced().map { it[table.index]!! } }
    }

    override fun afterExecution(): Unit = with(manager.target) {
        fetchedRows += table.slice(table.auditColumns).select { table.index inList updatedIndices }
        fetchedRows.forEach { row ->
            table.update({ table.index eq row[table.index]!! }) {
                row[salt] = Random.Default.randomSalt64()
                row[sign] = manager.signer.signRow(row, table)
                it[salt] = row[salt]
                it[sign] = row[sign]
            }
        }
    }
}

