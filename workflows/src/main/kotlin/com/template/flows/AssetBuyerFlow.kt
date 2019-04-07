package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.states.AssetState
import com.template.states.BuyOrderState
import com.template.states.SellOrderState
import net.corda.confidential.IdentitySyncFlow
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.SignedTransaction

@InitiatedBy(AssetSellerFlow::class)

class AssetBuyerFlow(private val otherPartySession: FlowSession): FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities[0]
        val sellOrder = subFlow(ReceiveStateAndRefFlow<SellOrderState>(otherPartySession)).single()
        logger.info("get sellOrder: $sellOrder")
        val buyOrders =
                serviceHub.vaultService.queryBy<BuyOrderState>()
        val possibleBuyOrder = buyOrders.states.find { buyOrder ->
            buyOrder.state.data.owner == ourIdentity &&
            buyOrder.state.data.buyAssetName == sellOrder.state.data.sellAssetName &&
            buyOrder.state.data.buyAssetQty >= sellOrder.state.data.sellAssetQty
        }
        logger.info("Buy order: $possibleBuyOrder")
        if (possibleBuyOrder != null) {
            val possibleAssetToSell =
                    serviceHub.vaultService.queryBy<AssetState>().states.find { assetState ->
                        logger.info("Check assst: $assetState. (${assetState.state.data.amount.quantity}|${possibleBuyOrder.state.data.sellAssetQty.toLong()})")
                        assetState.state.data.assetName == possibleBuyOrder.state.data.sellAssetName &&
                        assetState.state.data.amount.quantity == possibleBuyOrder.state.data.sellAssetQty.toLong() &&
                        assetState.state.data.owner == ourIdentity
                    }
            logger.info("possibleAssetToSell: $possibleAssetToSell")
            if (possibleAssetToSell != null) {
                logger.info("Send to recepient!")
                subFlow(SendStateAndRefFlow(otherPartySession, listOf(possibleAssetToSell)))

                subFlow(SendStateAndRefFlow(otherPartySession, listOf(possibleBuyOrder)))

                subFlow(IdentitySyncFlow.Receive(otherPartySession))

                logger.info("Receive tx and sign")

                val signTransactionFlow =
                        object : SignTransactionFlow(otherPartySession) {
                            override fun checkTransaction(stx: SignedTransaction) {
                            }
                        }
                val txId = subFlow(signTransactionFlow).id
                logger.info("Return to prev owner")

                return subFlow(ReceiveFinalityFlow(otherPartySession, expectedTxId = txId))
            }
        }
        throw FlowException("Exception!")
    }
}