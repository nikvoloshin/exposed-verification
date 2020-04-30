package com.github.nikvoloshin.exposed.verification

import com.github.nikvoloshin.exposed.verification.encoding.MACEncoder
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

class Verification private constructor(private val config: VerificationConfiguration) {
    private val encoder = MACEncoder(config.secretKey)

    init {
        config.tablesToVerify.forEach {
            it.table.registerServiceColumns(config.serviceColumnsName, encoder.codeLength)
        }

        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                *config.tablesToVerify.map { it.table }.toTypedArray(),
                inBatch = true
            )
        }
    }

    fun verify(table: Table) {
        TODO()
    }

    fun verifyAll() {
        TODO()
    }

    companion object {
        fun setup(config: VerificationConfiguration) = Verification(config)
    }
}