package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.states.AssetState
import com.template.states.BuyOrderState
import com.template.states.SellOrderState
import net.corda.confidential.IdentitySyncFlow
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC
class AssetSellerFlow(val assetName: String,
                      val otherParty: Party): FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities[0]
        val assetStates = serviceHub.vaultService.queryBy<AssetState>()
        val sellOrderStates = serviceHub.vaultService.queryBy<SellOrderState>()
        val possibleAsset =
                assetStates.states.find { assetState -> assetState.state.data.assetName == assetName }
        logger.info("Find asset to sell: $possibleAsset")
        val possibleSellOrder =
                sellOrderStates.states.find { sellOrderState -> sellOrderState.state.data.sellAssetName == assetName }
        logger.info("Find possible sell order: $possibleSellOrder")
        if (possibleAsset != null && possibleSellOrder != null) {
            val initFlow = initiateFlow(otherParty)
            subFlow(SendStateAndRefFlow(initFlow, listOf(possibleSellOrder)))

            val possibleStateToBuy = subFlow(ReceiveStateAndRefFlow<AssetState>(initFlow)).single()

            val possibleBuyOrder = subFlow(ReceiveStateAndRefFlow<BuyOrderState>(initFlow)).single()

            logger.info("Get state to buy: $possibleStateToBuy")

            val newOutput1 =
                    possibleAsset.state.data.withNewOwner(possibleStateToBuy.state.data.owner)
            val newOutput2 =
                    possibleStateToBuy.state.data.withNewOwner(possibleAsset.state.data.owner)

            logger.info("Make new outputs")

            val ptx = TransactionBuilder(notary)
                    .addInputState(possibleStateToBuy)
                    .addInputState(possibleBuyOrder)
                    .addInputState(possibleSellOrder)
                    .addInputState(possibleAsset)
                    .addCommand(newOutput1.command, ourIdentity.owningKey)
                    .addOutputState(newOutput1.ownableState, possibleStateToBuy.state.contract, possibleStateToBuy.state.notary)
                    .addCommand(newOutput2.command, possibleStateToBuy.state.data.owner.owningKey)
                    .addOutputState(newOutput2.ownableState)

            val signedTx = serviceHub.signInitialTransaction(ptx, ourIdentity.owningKey)

            logger.info("Sign tx and send to another party!")

            subFlow(IdentitySyncFlow.Send(initFlow, ptx.toWireTransaction(serviceHub)))

            val sellerSignature = subFlow(
                    CollectSignatureFlow(
                            signedTx,
                            initFlow,
                            initFlow.counterparty.owningKey)
            )
            val twiceSignedTx = signedTx + sellerSignature
            logger.info("get tx")

            return subFlow(FinalityFlow(twiceSignedTx, initFlow))
        }
        throw FlowException("Exeption!")
    }
}