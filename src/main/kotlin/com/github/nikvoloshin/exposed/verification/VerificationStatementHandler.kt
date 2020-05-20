package com.github.nikvoloshin.exposed.verification

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.Statement

abstract class VerificationStatementHandler<in S : Statement<*>>(
    private val transaction: Transaction,
    private val statement: S
) {
    abstract fun beforeExecution()

    abstract fun afterExecution()
}