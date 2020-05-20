package com.github.nikvoloshin.exposed.verification

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.Statement

abstract class VerificationManager(val database: Database, val schema: String) {

    abstract fun verify(row: ResultRow, table: Table)

    abstract fun verify(table: Table)

    abstract fun verify()

    abstract fun <S: Statement<*>> handleStatement(transaction: Transaction, statement: S): VerificationStatementHandler<S>?

    companion object {
        internal val managers = mutableListOf<VerificationManager>()
    }
}