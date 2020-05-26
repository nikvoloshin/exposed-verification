package com.github.nikvoloshin.exposed.verification

import org.jetbrains.exposed.dao.flushCache
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.Statement
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.StatementInterceptor
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.jetbrains.exposed.sql.transactions.transactionScope
import java.util.*

object ExposedAuditInterceptor : StatementInterceptor {
    private val runningStatements by transactionScope { IdentityHashMap<Statement<*>, List<AuditStatementHandler>>() }
    internal var enabled by transactionScope { true }

    override fun beforeExecution(transaction: Transaction, context: StatementContext) {
        if (!enabled || runningStatements[context.statement] != null) {
            return
        }

        transaction.flushCache()

        ExposedAudit.managers
            .flatMap { listOf(it.auditor, it.signer) }
            .mapNotNull { it.handlerFor(transaction, context.statement) }
            .also { if (it.isNotEmpty()) runningStatements[context.statement] = it }
            .forEach { transaction.auditDisabled { it.beforeExecution() } }
    }

    override fun afterExecution(
        transaction: Transaction,
        contexts: List<StatementContext>,
        executedStatement: PreparedStatementApi
    ) {
        if (!enabled) return

        val statement = contexts.first().statement

        try {
            runningStatements[statement]?.forEach { transaction.auditDisabled { it.afterExecution() } }
        } finally {
            runningStatements -= statement
        }
    }

}

fun <R> Transaction.auditEnabled(body: Transaction.() -> R): R = enableAudit(true, body)
fun <R> Transaction.auditDisabled(body: Transaction.() -> R): R = enableAudit(false, body)

private fun <R> Transaction.enableAudit(enable: Boolean, body: Transaction.() -> R): R {
    val prevStatus = ExposedAuditInterceptor.enabled
    ExposedAuditInterceptor.enabled = enable

    try {
        return body()
    } finally {
        ExposedAuditInterceptor.enabled = prevStatus
    }
}