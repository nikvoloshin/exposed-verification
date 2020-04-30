package com.github.nikvoloshin.exposed.verification

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Schema
import org.jetbrains.exposed.sql.Table
import javax.crypto.SecretKey

data class VerificationConfiguration(
    val tablesToVerify: List<TableDescription>,
    val serviceColumnsName: (String) -> String = { "verification_$it" },
    val verifyOnSelect: Boolean = false,
    val secretKey: SecretKey
)

data class TableDescription(
    val table: Table,
    val columns: Collection<Column<*>> = table.columns
)