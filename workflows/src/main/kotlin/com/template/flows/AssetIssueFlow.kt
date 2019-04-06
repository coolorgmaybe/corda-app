package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.AssetContract
import com.template.states.AssetState
import net.corda.core.contracts.Command
import net.corda.core.contracts.CommandAndState
import net.corda.core.flows.*
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class AssetIssueFlow(val assetName: String,
                     val assetQty: Int) : FlowLogic<Unit>() {

    /** The progress tracker provides checkpoints indicating the progress of the flow to observers. */
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {

        val notary = serviceHub.networkMapCache.notaryIdentities[0]
        val outputState = AssetState(ourIdentity, assetName, assetQty)
        val command = Command(AssetContract.Commands.Issue(), ourIdentity.owningKey)
        val txBuilder = TransactionBuilder(notary = notary)
                .addOutputState(outputState)
                .addCommand(command)
        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        subFlow(FinalityFlow(signedTx, emptySet<FlowSession>()))
    }
}