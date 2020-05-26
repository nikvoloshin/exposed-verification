package com.github.nikvoloshin.exposed.verification.chain

import com.github.nikvoloshin.exposed.verification.DataCorruptionException
import com.github.nikvoloshin.exposed.verification.auditDisabled
import com.github.nikvoloshin.exposed.verification.base.Auditor
import com.github.nikvoloshin.exposed.verification.base.requireTable
import com.github.nikvoloshin.exposed.verification.base.requireTransaction
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.TransactionManager

internal class ChainedMacAuditor(
    override val target: ChainedMacAuditTarget,
    verifyOnSelect: Boolean
) : Auditor(target, verifyOnSelect) {
    private fun auditAdjacent(prev: ResultRow, cur: ResultRow, table: Table) = with(target) {
        val matches = (manager.signer as ChainedMacSigner).signAdjacent(prev, cur, table) == cur[table.cmac]
        if (!matches) throw DataCorruptionException(target, table)
    }

    override fun auditTable(table: Table) = with(target) {
        requireTable(table = table)

        TransactionManager.current().auditDisabled {
            val rows = table.slice(table.auditColumns).selectAll().orderBy(table.index).toMutableList()
            if (rows.isNotEmpty()) {
                rows.apply { add(rows.first()) }.zipWithNext().forEach { (prev, cur) ->
                    auditRow(cur, table)
                    auditAdjacent(prev, cur, table)
                }
            }
        }
    }

    override fun auditAll() = with(target) {
        requireTransaction()
        TransactionManager.current().auditDisabled { tables.forEach { auditTable(it) } }
    }
}