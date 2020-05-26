package com.github.nikvoloshin.exposed.verification.chain.handlers

import com.github.nikvoloshin.exposed.verification.base.handlers.DeleteHandler
import com.github.nikvoloshin.exposed.verification.chain.ChainedMacAuditManager
import com.github.nikvoloshin.exposed.verification.queries.pred
import com.github.nikvoloshin.exposed.verification.queries.succ
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.DeleteStatement
import org.jetbrains.exposed.sql.update

internal class ChainedMacDeleteHandler(
    override val manager: ChainedMacAuditManager,
    transaction: Transaction,
    override val statement: DeleteStatement
) : DeleteHandler(manager, transaction, statement) {
    override fun afterExecution(): Unit = with(manager.target) {
        val table = statement.table
        deletedIndices.forEach { index ->
            val pred = pred(table, index) ?: return
            val succ = succ(table, index) ?: return

            table.update({ table.index eq succ[table.index]!! }) {
                it[cmac] = manager.signer.signAdjacent(pred, succ, table)
            }
        }
    }
}