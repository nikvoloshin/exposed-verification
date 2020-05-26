package com.github.nikvoloshin.exposed.verification.base

import com.github.nikvoloshin.exposed.verification.AuditStatementHandlerProvider
import com.github.nikvoloshin.exposed.verification.ExposedAudit
import com.github.nikvoloshin.exposed.verification.encoding.MacEncoder
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

internal abstract class Signer(
    protected open val target: AuditTarget,
    protected val encoder: MacEncoder
) : AuditStatementHandlerProvider(target) {
    protected val manager get() = ExposedAudit.managers.first { it.signer === this }

    internal open fun signRow(row: ResultRow, table: Table): String = with(target) {
        val content = table.contentColumns.map { it.columnType.valueToString(row[it]) }
        encoder.encode(content)
    }

    abstract fun signTable(table: Table)

    abstract fun signAll()
}