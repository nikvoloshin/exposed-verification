package com.github.nikvoloshin.exposed.verification.queries

import com.github.nikvoloshin.exposed.verification.base.AuditTarget
import com.github.nikvoloshin.exposed.verification.base.matchingTables
import org.jetbrains.exposed.sql.*
import java.util.*

internal class ForceAuditQuery(query: Query) : Query(query.set, query.where) {
    init {
        orderBy(*query.orderByExpressions.toTypedArray())
        query.limit?.let { limit(it, offset) }
    }
}

internal fun Query.forced() = ForceAuditQuery(this)

internal fun AuditTarget.auditQuery(query: Query): Query {
    val neededColumns = matchingTables(tables = query.targets).flatMap { it.auditColumns }
    val newQuery = Query(query.set, query.where).apply {
        orderBy(*query.orderByExpressions.toTypedArray())
        query.limit?.let { limit(it, query.offset) }
    }
    return newQuery.adjustSlice { slice(fields + neededColumns) }
}

internal fun AuditTarget.pred(table: Table, index: UUID) =
    table.select { table.index less index }.orderBy(table.index, SortOrder.DESC).limit(1).firstOrNull()
        ?: last(table)

internal fun AuditTarget.succ(table: Table, index: UUID) =
    table.select { table.index greater index }.orderBy(table.index, SortOrder.ASC).limit(1).firstOrNull()
        ?: first(table)

internal fun AuditTarget.first(table: Table) =
    table.selectAll().orderBy(table.index, SortOrder.ASC).limit(1).firstOrNull()

internal fun AuditTarget.last(table: Table) =
    table.selectAll().orderBy(table.index, SortOrder.DESC).limit(1).firstOrNull()
