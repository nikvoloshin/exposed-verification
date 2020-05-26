package com.github.nikvoloshin.exposed.verification.chain

import com.github.nikvoloshin.exposed.verification.base.AuditTarget
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table

internal class ChainedMacAuditTarget(
    database: Database,
    schema: String,
    targets: List<Pair<Table, List<Column<*>>>>,
    obscureNames: Boolean
) : AuditTarget(database, schema, targets, obscureNames) {
    internal val Table.cmac by registerColumn { varchar(name("audit_cmac"), 128).nullable() }
}