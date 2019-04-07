package com.template.flows

import com.template.states.AssetState
import net.corda.core.flows.*
import net.corda.core.node.StatesToRecord
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.SignedTransaction

@InitiatedBy(SellToBuyMatcherFlow::class)
class PartyMatchFlow(private val otherPartySession: FlowSession): FlowLogic<SignedTransaction>() {

    override fun call(): SignedTransaction {

        logger.info("Vault before: ${serviceHub.vaultService.queryBy<AssetState>()}")

        return subFlow(ReceiveTransactionFlow(otherPartySession, true, StatesToRecord.ONLY_RELEVANT))
    }
}