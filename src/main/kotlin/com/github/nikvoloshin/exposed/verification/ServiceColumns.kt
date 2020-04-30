package com.github.nikvoloshin.exposed.verification

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

internal val Table.mac: Column<String?> get() = serviceColumnsMap[this]!!.mac
internal val Table.cmac: Column<String?> get() = serviceColumnsMap[this]!!.cmac
internal val Table.salt: Column<String?> get() = serviceColumnsMap[this]!!.salt
internal val Table.order: Column<Int?> get() = serviceColumnsMap[this]!!.order

internal fun Table.registerServiceColumns(serviceColumnName: (String) -> String, codeLength: Int) {
    serviceColumnsMap[this] = ServiceColumns(
        varchar(serviceColumnName("mac"), codeLength).nullable(),
        varchar(serviceColumnName("cmac"), codeLength).nullable(),
        varchar(serviceColumnName("salt"), 64).nullable(),
        integer(serviceColumnName("order")).nullable()
    )
}

internal val serviceColumnsMap = mutableMapOf<Table, ServiceColumns>()

internal data class ServiceColumns(
    val mac: Column<String?>,
    val cmac: Column<String?>,
    val salt: Column<String?>,
    val order: Column<Int?>
)
