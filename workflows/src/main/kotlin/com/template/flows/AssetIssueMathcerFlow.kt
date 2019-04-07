package com.template.flows

import net.corda.core.flows.*
import net.corda.core.node.StatesToRecord
import net.corda.core.transactions.SignedTransaction

@InitiatedBy(AssetIssueFlow::class)
class AssetIssueMathcerFlow(private val otherPartySession: FlowSession): FlowLogic<Unit>() {

    override fun call(): Unit {

        subFlow(ReceiveTransactionFlow(otherPartySession, true, StatesToRecord.ALL_VISIBLE))
    }
}