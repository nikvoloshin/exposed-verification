package com.github.nikvoloshin.exposed.verification.chain

import com.github.nikvoloshin.exposed.verification.AuditManager
import com.github.nikvoloshin.exposed.verification.encoding.MacEncoder

internal class ChainedMacAuditManager(
    override val target: ChainedMacAuditTarget,
    encoder: MacEncoder,
    override val auditor: ChainedMacAuditor,
    override val signer: ChainedMacSigner
): AuditManager(target, encoder, auditor, signer)