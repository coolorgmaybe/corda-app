package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.states.AssetState
import com.template.states.BuyOrderState
import com.template.states.SellOrderState
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC
class SellToBuyMatcherFlow(val sellOrderOwner: Party,
                           val sellOrderSellAssetName: String,
                           val sellOrderSellAssetQty: Int,
                           val sellOrderBuyAssetName: String,
                           val sellOrderBuyAssetQty: Int,
                           val buyOrderOwner: Party,
                           val buyOrderSellAssetName: String,
                           val buyOrderSellAssetQty: Int,
                           val buyOrderBuyAssetName: String,
                           val buyOrderBuyAssetQty: Int): FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val notary = serviceHub.networkMapCache.notaryIdentities[0]
        val sellOrders = serviceHub.vaultService.queryBy<SellOrderState>()
        val buyOrders= serviceHub.vaultService.queryBy<BuyOrderState>()
        val assets = serviceHub.vaultService.queryBy<AssetState>()
        val possibleSellOrder = sellOrders.states.find { state ->
            state.state.data.owner == sellOrderOwner &&
            state.state.data.sellAssetName == sellOrderSellAssetName &&
            state.state.data.sellAssetQty == sellOrderSellAssetQty &&
            state.state.data.buyAssetName == sellOrderBuyAssetName &&
            state.state.data.buyAssetQty == sellOrderBuyAssetQty
        }
        logger.info("possibleSellOrder: $possibleSellOrder")
        val possibleBuyOrder = buyOrders.states.find { state ->
            state.state.data.owner == buyOrderOwner &&
            state.state.data.sellAssetName == buyOrderSellAssetName &&
            state.state.data.sellAssetQty == buyOrderSellAssetQty &&
            state.state.data.buyAssetName == buyOrderBuyAssetName &&
            state.state.data.buyAssetQty == buyOrderBuyAssetQty
        }
        logger.info("possibleBuyOrder: $possibleBuyOrder")
        if (possibleBuyOrder != null && possibleSellOrder != null){
            val possibleAssetToSellBySellOrderOwner = assets.states.find { state ->
                state.state.data.owner == sellOrderOwner &&
                state.state.data.assetName == sellOrderSellAssetName &&
                state.state.data.amount.quantity == sellOrderSellAssetQty.toLong()
            }
            logger.info("possibleAssetToSellBySellOrderOwner: $possibleAssetToSellBySellOrderOwner")
            val possibleAssetToSellByBuyOrderOwner = assets.states.find { state ->
                state.state.data.owner == buyOrderOwner &&
                state.state.data.assetName == buyOrderSellAssetName &&
                state.state.data.amount.quantity == buyOrderSellAssetQty.toLong()
            }
            logger.info("possibleAssetToSellByBuyOrderOwner: $possibleAssetToSellByBuyOrderOwner")
            if (possibleAssetToSellBySellOrderOwner != null && possibleAssetToSellByBuyOrderOwner != null) {
                val newOutput1 = possibleAssetToSellBySellOrderOwner
                        .state
                        .data
                        .withNewOwner(buyOrderOwner)
                logger.info("newOutput1: $newOutput1")
                val newOutput2 = possibleAssetToSellByBuyOrderOwner
                        .state
                        .data
                        .withNewOwner(sellOrderOwner)
                logger.info("newOutput2: $newOutput2")
                val txBuilder = TransactionBuilder(notary = notary)
                        .addInputState(possibleBuyOrder)
                        .addInputState(possibleSellOrder)
                        .addInputState(possibleAssetToSellBySellOrderOwner)
                        .addInputState(possibleAssetToSellByBuyOrderOwner)
                        .addCommand(newOutput1.command, ourIdentity.owningKey)
                        .addCommand(newOutput2.command, ourIdentity.owningKey)
                        .addOutputState(newOutput1.ownableState)
                        .addOutputState(newOutput2.ownableState)
                val signedTx = serviceHub.signInitialTransaction(txBuilder)
                logger.info("signedTx: $signedTx")
                val flowWithSeller = initiateFlow(sellOrderOwner)
                //subFlow(SendTransactionFlow(flowWithSeller, signedTx))
                val flowWithBuyer = initiateFlow(buyOrderOwner)
                //subFlow(SendTransactionFlow(flowWithBuyer, signedTx))
                subFlow(FinalityFlow(signedTx, setOf(flowWithSeller, flowWithBuyer)))
            }
        } else throw FlowException("Can't find correct possibleSellOrder or possibleSellOrder")
    }
}