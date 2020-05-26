package com.github.nikvoloshin.exposed.verification.base

import com.github.nikvoloshin.exposed.verification.AuditStatementHandler
import com.github.nikvoloshin.exposed.verification.AuditStatementHandlerProvider
import com.github.nikvoloshin.exposed.verification.DataCorruptionException
import com.github.nikvoloshin.exposed.verification.ExposedAudit
import com.github.nikvoloshin.exposed.verification.base.handlers.QueryHandler
import com.github.nikvoloshin.exposed.verification.encoding.MacEncoder
import com.github.nikvoloshin.exposed.verification.queries.ForceAuditQuery
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.Statement

internal abstract class Auditor(
    protected open val target: AuditTarget,
    protected val verifyOnSelect: Boolean
): AuditStatementHandlerProvider(target) {
    protected val manager get() = ExposedAudit.managers.first { it.auditor === this }

    internal open fun auditRow(row: ResultRow, table: Table) = with(target) {
        val matches = manager.signer.signRow(row, table) == row[table.sign]
        if (!matches) throw DataCorruptionException(target, table)
    }

    abstract fun auditTable(table: Table)

    abstract fun auditAll()

    override fun queryHandler(transaction: Transaction, statement: Statement<*>): AuditStatementHandler? {
        if (statement !is Query || statement !is ForceAuditQuery && !verifyOnSelect) return null
        return QueryHandler(manager, transaction, statement)
    }
}