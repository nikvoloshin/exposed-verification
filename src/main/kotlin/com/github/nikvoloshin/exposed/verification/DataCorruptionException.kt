package com.github.nikvoloshin.exposed.verification

import com.github.nikvoloshin.exposed.verification.base.AuditTarget
import org.jetbrains.exposed.sql.Table

class DataCorruptionException(val target: AuditTarget, val table: Table?) : RuntimeException()