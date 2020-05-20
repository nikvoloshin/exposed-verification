package com.github.nikvoloshin.exposed.verification

import org.jetbrains.exposed.dao.flushCache
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.StatementInterceptor
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi

class VerificationStatementInterceptor : StatementInterceptor {
    private var currentHandler: VerificationStatementHandler<*>? = null
    private var inHandler: Boolean = false

    override fun beforeExecution(transaction: Transaction, context: StatementContext) {
        if (inHandler) return
        if (currentHandler != null) return

        val handler = VerificationManager.managers.mapNotNull {
            it.handleStatement(transaction, context.statement)
        }.firstOrNull() ?: return

        transaction.flushCache()
        currentHandler = handler

        try {
            inHandler = true
            handler.beforeExecution()
        } finally {
            inHandler = false
        }
    }

    override fun afterExecution(transaction: Transaction, contexts: List<StatementContext>, executedStatement: PreparedStatementApi) {
        try {
            inHandler = true
            currentHandler!!.afterExecution()
        } finally {
            inHandler = false
            currentHandler = null
        }
    }
}