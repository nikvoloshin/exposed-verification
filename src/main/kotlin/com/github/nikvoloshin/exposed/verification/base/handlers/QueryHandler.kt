package com.github.nikvoloshin.exposed.verification.base.handlers

import com.github.nikvoloshin.exposed.verification.*
import com.github.nikvoloshin.exposed.verification.AuditStatementHandler
import com.github.nikvoloshin.exposed.verification.queries.ForceAuditQuery
import com.github.nikvoloshin.exposed.verification.queries.auditQuery
import com.github.nikvoloshin.exposed.verification.base.matchingTables
import com.github.nikvoloshin.exposed.verification.queries.forced
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.Transaction

internal class QueryHandler(
    manager: AuditManager,
    transaction: Transaction,
    private val query: Query
) : AuditStatementHandler(manager, transaction, query) {

    override fun afterExecution(): Unit = with(manager.target) {
        val auditableTables = matchingTables(transaction, query.targets)
        val neededColumns = auditableTables.flatMap { it.auditColumns }

        if (query.set.fields.containsAll(neededColumns)) {
            for (table in auditableTables) {
                for (row in query) {
                    manager.auditor.auditRow(row, table)
                }
            }
        } else {
            transaction.auditEnabled {
                val newQuery = auditQuery(query).let { if (query is ForceAuditQuery) it.forced() else it }
                newQuery.execute(transaction)
            }
        }
    }

}