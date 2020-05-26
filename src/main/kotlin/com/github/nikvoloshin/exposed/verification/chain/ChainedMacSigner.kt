package com.github.nikvoloshin.exposed.verification.chain

import com.github.nikvoloshin.exposed.verification.AuditStatementHandler
import com.github.nikvoloshin.exposed.verification.auditDisabled
import com.github.nikvoloshin.exposed.verification.base.Signer
import com.github.nikvoloshin.exposed.verification.base.requireTable
import com.github.nikvoloshin.exposed.verification.base.requireTransaction
import com.github.nikvoloshin.exposed.verification.chain.handlers.ChainedMacDeleteHandler
import com.github.nikvoloshin.exposed.verification.chain.handlers.ChainedMacInsertHandler
import com.github.nikvoloshin.exposed.verification.chain.handlers.ChainedMacUpdateHandler
import com.github.nikvoloshin.exposed.verification.encoding.MacEncoder
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.util.*

internal class ChainedMacSigner(
    override val target: ChainedMacAuditTarget,
    encoder: MacEncoder
) : Signer(target, encoder) {

    internal fun signAdjacent(prev: ResultRow, cur: ResultRow, table: Table): String = with(target) {
        encoder.encode(prev[table.sign]!!, cur[table.sign]!!)
    }

    override fun signTable(table: Table) = with(target) {
        requireTable(table = table)

        fun update(index: UUID, sign: String, chain: String? = null) {
            table.update({ table.index eq index }) { stmt ->
                stmt[table.sign] = sign
                chain?.let { stmt[table.cmac] = it }
            }
        }
        TransactionManager.current().auditDisabled {
            val rows = table.slice(table.auditColumns).selectAll().orderBy(table.index).toMutableList()
            if (rows.isNotEmpty()) {
                rows.first().let { row ->
                    row[table.sign] = signRow(row, table)
                    update(row[table.index]!!, row[table.sign]!!)
                }

                rows.apply { add(rows.first()) }.zipWithNext().forEach { (prev, cur) ->
                    cur[table.sign] = signRow(cur, table)
                    cur[table.cmac] = signAdjacent(prev, cur, table)
                    update(cur[table.index]!!, cur[table.sign]!!, cur[table.cmac]!!)
                }
            }
        }
    }

    override fun signAll() = with(target) {
        requireTransaction()
        TransactionManager.current().auditDisabled { tables.forEach { signTable(it) } }
    }

    override fun insertHandler(transaction: Transaction, statement: Statement<*>): AuditStatementHandler? {
        if (statement is BatchInsertStatement || statement::class == InsertStatement::class) {
            return ChainedMacInsertHandler(manager as ChainedMacAuditManager, transaction, statement as InsertStatement<*>)
        }
        return null
    }

    override fun updateHandler(transaction: Transaction, statement: Statement<*>): AuditStatementHandler? {
        if (statement is BatchUpdateStatement || statement is UpdateStatement) {
            return ChainedMacUpdateHandler(manager as ChainedMacAuditManager, transaction, statement as UpdateStatement)
        }
        return null
    }

    override fun deleteHandler(transaction: Transaction, statement: Statement<*>): AuditStatementHandler? {
        if (statement is DeleteStatement) {
            return ChainedMacDeleteHandler(manager as ChainedMacAuditManager, transaction, statement)
        }
        return null
    }
}