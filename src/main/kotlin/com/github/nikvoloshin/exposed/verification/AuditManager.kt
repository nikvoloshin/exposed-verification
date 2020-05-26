package com.github.nikvoloshin.exposed.verification

import com.github.nikvoloshin.exposed.verification.base.AuditTarget
import com.github.nikvoloshin.exposed.verification.base.Auditor
import com.github.nikvoloshin.exposed.verification.base.Signer
import com.github.nikvoloshin.exposed.verification.encoding.MacEncoder
import org.jetbrains.exposed.sql.Table
import javax.crypto.SecretKey

open class AuditManager internal constructor(
    open val target: AuditTarget,
    private val encoder: MacEncoder,
    internal open val auditor: Auditor,
    internal open val signer: Signer
) {

    fun auditTable(table: Table) = auditor.auditTable(table)

    fun auditAll() = auditor.auditAll()

    fun signTable(table: Table) = signer.signTable(table)

    fun signAll() = signer.signAll()

    fun replaceKey(newKey: SecretKey, resign: Boolean = true) {
        encoder.secretKey = newKey
        if (resign) signAll()
    }

}