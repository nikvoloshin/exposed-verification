package com.github.nikvoloshin.exposed.verification.interceptors

import com.github.nikvoloshin.exposed.verification.VerificationConfiguration
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.GlobalStatementInterceptor
import org.jetbrains.exposed.sql.statements.Statement
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.jetbrains.exposed.sql.transactions.transactionScope
import kotlin.reflect.KClass

internal abstract class VerificationInterceptor<S: Statement<*>>(
    val config: VerificationConfiguration,
    val clazz: KClass<S>
) : GlobalStatementInterceptor {
    protected var isIntercepting: Boolean by transactionScope { false }

    override fun beforeExecution(transaction: Transaction, context: StatementContext) {
        val stmt = context.statement
        if (stmt::class != clazz) return

        isIntercepting = true
        doBeforeExecution(transaction, stmt as S)
    }

    override fun afterExecution(
        transaction: Transaction,
        contexts: List<StatementContext>,
        executedStatement: PreparedStatementApi
    ) {
        val stmt = contexts.first().statement
        if (stmt::class != clazz) return

        doAfterExecution(transaction, stmt as S)
        isIntercepting = false
    }

    abstract fun doBeforeExecution(transaction: Transaction, statement: S)

    abstract fun doAfterExecution(transaction: Transaction, statement: S)

}