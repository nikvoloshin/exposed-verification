package com.github.nikvoloshin.exposed.verification.base.handlers

import com.github.nikvoloshin.exposed.verification.AuditManager
import com.github.nikvoloshin.exposed.verification.AuditStatementHandler
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.DeleteStatement
import java.util.*

internal open class DeleteHandler(
    manager: AuditManager,
    transaction: Transaction,
    override val statement: DeleteStatement
): AuditStatementHandler(manager, transaction, statement) {
    protected val deletedIndices = mutableListOf<UUID>()

    override fun beforeExecution(): Unit = with(manager.target) {
        deletedIndices += Query(statement.table, statement.where).apply {
            statement.limit?.let { limit(it, statement.offset ?: 0) }
        }.map { it[statement.table.index]!! }
    }
}