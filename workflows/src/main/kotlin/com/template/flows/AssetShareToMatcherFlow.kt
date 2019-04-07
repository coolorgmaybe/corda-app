package com.template.flows

import com.template.contracts.AssetContract
import com.template.states.AssetState
import net.corda.confidential.IdentitySyncFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC
class AssetShareToMatcherFlow(val assetName: String): FlowLogic<Unit>() {

    override fun call() {
//        val notary = serviceHub.networkMapCache.notaryIdentities[0]
//        val assetStates = serviceHub.vaultService.queryBy<AssetState>()
//        val possibleAsset =
//                assetStates.states.find { assetState -> assetState.state.data.assetName == assetName }
//        logger.info("Find asset to share: $possibleAsset")
//
//        if (possibleAsset != null) {
//            val assetToShare = possibleAsset
//            val initFlow = initiateFlow(assetToShare.state.data.matcher)
//            val ptx = TransactionBuilder(notary)
//                    .addInputState(assetToShare)
//                    .addCommand(AssetContract.Commands.ShareAsset(), ourIdentity.owningKey)
//                    .addOutputState(assetToShare.state)
//
//            val signedTx = serviceHub.signInitialTransaction(ptx, ourIdentity.owningKey)
//
//            subFlow(IdentitySyncFlow.Send(initFlow, ptx.toWireTransaction(serviceHub)))
//        }
    }
}