package com.github.nikvoloshin.exposed.verification

import com.github.nikvoloshin.exposed.verification.chain.ChainedMacAuditManager
import com.github.nikvoloshin.exposed.verification.chain.ChainedMacAuditTarget
import com.github.nikvoloshin.exposed.verification.chain.ChainedMacAuditor
import com.github.nikvoloshin.exposed.verification.chain.ChainedMacSigner
import com.github.nikvoloshin.exposed.verification.encoding.MacEncoder
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.TransactionManager
import javax.crypto.SecretKey

object ExposedAudit {
    fun setup(
        database: Database = TransactionManager.current().db,
        schema: String = TransactionManager.current().connection.schema,
        tables: List<Pair<Table, List<Column<*>>>>,
        secretKey: SecretKey,
        obscureNames: Boolean = true,
        verifyOnSelect: Boolean = true
    ): AuditManager {
        checkTarget(database, schema, tables)

        val target = ChainedMacAuditTarget(database, schema, tables, obscureNames).also { it.initializeSchema() }
        val auditor = ChainedMacAuditor(target, verifyOnSelect)
        val encoder = MacEncoder(secretKey)
        val signer = ChainedMacSigner(target, encoder)
        val manager = ChainedMacAuditManager(target, encoder, auditor, signer)

        return manager.also { managers += it }
    }

    private fun checkTarget(database: Database, schema: String, tables: List<Pair<Table, List<Column<*>>>>) {
        tables.forEach { (table, columns) ->
            val nonMatchingColumn = columns.find { it.table != table }
            require(nonMatchingColumn == null) {
                "Column ${nonMatchingColumn!!.name} doesn't registered in table ${table.tableName}"
            }
        }

        for (other in managers) {
            if (other.target.database == database && other.target.schema == schema) {
                for (table in tables.map { it.first }) {
                    require (table !in other.target.tables) {
                        "Table ${table.tableName} already registered in another manager"
                    }
                }
            }
        }
    }

    internal val managers = mutableListOf<AuditManager>()
}