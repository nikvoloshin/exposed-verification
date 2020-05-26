package com.github.nikvoloshin.exposed.verification

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.Statement

internal open class AuditStatementHandler(
    protected open val manager: AuditManager,
    protected val transaction: Transaction,
    protected open val statement: Statement<*>
) {

    open fun beforeExecution() {}

    open fun afterExecution() {}

}