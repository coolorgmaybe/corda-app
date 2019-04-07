package com.template.flows

import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.flows.ReceiveTransactionFlow
import net.corda.core.node.StatesToRecord

@InitiatedBy(SellOrderIssueFlow::class)
class SellOrderIssueMatcherFlow(private val otherPartySession: FlowSession): FlowLogic<Unit>() {

    override fun call(): Unit {

        subFlow(ReceiveTransactionFlow(otherPartySession, true, StatesToRecord.ALL_VISIBLE))
    }
}