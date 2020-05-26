package com.github.nikvoloshin.exposed.verification.chain.handlers

import com.github.nikvoloshin.exposed.verification.base.handlers.InsertHandler
import com.github.nikvoloshin.exposed.verification.chain.ChainedMacAuditManager
import com.github.nikvoloshin.exposed.verification.queries.pred
import com.github.nikvoloshin.exposed.verification.queries.succ
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.update

internal class ChainedMacInsertHandler(
    override val manager: ChainedMacAuditManager,
    transaction: Transaction,
    statement: InsertStatement<*>
) : InsertHandler(manager, transaction, statement) {
    override fun afterExecution(): Unit = with(manager.target) {
        super.afterExecution()

        val table = statement.targets.first()

        fetchedRows.forEach { row ->
            val index = row[table.index]!!
            val pred = pred(table, index)!!
            val succ = succ(table, index)!!

            table.update({ table.index eq index }) {
                it[cmac] = manager.signer.signAdjacent(pred, row, table)
            }

            table.update({ table.index eq succ[table.index]!! }) {
                it[cmac] = manager.signer.signAdjacent(row, succ, table)
            }
        }
    }
}