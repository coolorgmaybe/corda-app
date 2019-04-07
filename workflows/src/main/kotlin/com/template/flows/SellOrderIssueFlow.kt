package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.OrderContract
import com.template.states.SellOrderState
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class SellOrderIssueFlow(val sellAssetName: String,
                         val sellAssetQty: Int,
                         val buyAssetName: String,
                         val buyAssetQty: Int,
                         val matcherParty: Party) : FlowLogic<Unit>() {

    /** The progress tracker provides checkpoints indicating the progress of the flow to observers. */
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {

        val notary = serviceHub.networkMapCache.notaryIdentities[0]
        val initFlow = initiateFlow(matcherParty)
        val outputState = SellOrderState(
                ourIdentity,
                sellAssetName,
                sellAssetQty,
                buyAssetName,
                buyAssetQty)
        val command = Command(OrderContract.Commands.Issue(), ourIdentity.owningKey)
        val txBuilder = TransactionBuilder(notary = notary)
                .addOutputState(outputState)
                .addCommand(command)
        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        subFlow(SendTransactionFlow(initFlow, signedTx))
        subFlow(FinalityFlow(signedTx, emptySet<FlowSession>()))
    }
}