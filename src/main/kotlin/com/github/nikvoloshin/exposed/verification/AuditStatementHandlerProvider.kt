package com.github.nikvoloshin.exposed.verification

import com.github.nikvoloshin.exposed.verification.AuditStatementHandler
import com.github.nikvoloshin.exposed.verification.base.AuditTarget
import com.github.nikvoloshin.exposed.verification.base.matchingTables
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.Statement
import org.jetbrains.exposed.sql.statements.StatementType

internal open class AuditStatementHandlerProvider(private val target: AuditTarget) {
    open fun handlerFor(transaction: Transaction, statement: Statement<*>): AuditStatementHandler? {
        if (target.matchingTables(transaction, statement.targets).isEmpty()) return null

        return when(statement.type) {
            StatementType.SELECT -> queryHandler(transaction, statement)
            StatementType.INSERT -> insertHandler(transaction, statement)
            StatementType.UPDATE -> updateHandler(transaction, statement)
            StatementType.DELETE -> deleteHandler(transaction, statement)
            else -> null
        }
    }

    open fun queryHandler(transaction: Transaction, statement: Statement<*>): AuditStatementHandler? = null

    open fun insertHandler(transaction: Transaction, statement: Statement<*>): AuditStatementHandler? = null

    open fun updateHandler(transaction: Transaction, statement: Statement<*>): AuditStatementHandler? = null

    open fun deleteHandler(transaction: Transaction, statement: Statement<*>): AuditStatementHandler? = null

}

