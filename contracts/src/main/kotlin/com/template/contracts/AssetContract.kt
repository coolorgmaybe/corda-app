package com.template.contracts

import com.template.states.AssetState
import com.template.states.BuyOrderState
import com.template.states.SellOrderState
import net.corda.core.contracts.*
import net.corda.core.internal.WaitForStateConsumption.Companion.logger
import net.corda.core.transactions.LedgerTransaction

class AssetContract : Contract {

    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.template.contracts.AssetContract"
    }

    interface Commands : CommandData {
        class Issue : TypeOnlyCommandData(), Commands
        class Move : TypeOnlyCommandData(), Commands
        class ShareAsset : TypeOnlyCommandData(), Commands
    }

    override fun verify(tx: LedgerTransaction) {

        logger.info("Get tx: $tx for validation")

        logger.info("Tx inputs: ${tx.inputs.size}: ${tx.inputs}")

        logger.info("tx outputs: ${tx.outputs.size}: ${tx.outputs}")

        logger.info("Tx commands: ${tx.commands.size}: ${tx.commands}")

        val groups = tx.groupStates(OwnableState::owner)

        val commands = tx.commands

        logger.info("Groups: $groups")

        for((inputs, outputs) in groups) {
            for (command in commands) {
                when (command.value) {
                    is Commands.Move -> {
                        requireThat {
                            "there are should be only 2 inputs!" using (inputs.size == 2)
                            "Should be only 1 output!" using (outputs.size == 1)
                        }
                        val possibleSellOrder = inputs.find { state -> state is SellOrderState }
                        val possibleBuyOrder = inputs.find { state -> state is BuyOrderState }
                        val possibleInput = inputs.find { state -> state is AssetState }
                        val possibleOutput = outputs.find { state -> state is AssetState }
                        if (possibleSellOrder != null) {
                            val sellOrder = possibleSellOrder as SellOrderState
                            val inputAsset = possibleInput as AssetState
                            val outputAsset = possibleOutput as AssetState
                            requireThat {
                                """sell asset should equal to "sell asset" in order """ using
                                        ((inputAsset.assetName == sellOrder.sellAssetName) &&
                                                inputAsset.amount.quantity == sellOrder.sellAssetQty.toLong())
                                """buy asset should equal to "buy asset" in order""" using
                                        ((outputAsset.assetName == sellOrder.buyAssetName) &&
                                                outputAsset.amount.quantity == sellOrder.buyAssetQty.toLong())
                                "owner of input state and output state and order should be same!" using
                                        ((outputAsset.owner == sellOrder.owner) &&
                                                (inputAsset.owner == sellOrder.owner))
                            }
                        } else if (possibleBuyOrder != null) {
                            val buyOrder = possibleBuyOrder as BuyOrderState
                            logger.info("Buy order: $buyOrder")
                            val inputAsset = possibleInput as AssetState
                            logger.info("inputAsset: $inputAsset")
                            val outputAsset = possibleOutput as AssetState
                            logger.info("outputAsset: $outputAsset")
                            requireThat {
                                """sell asset should equal to "sell asset" in order """ using
                                        ((inputAsset.assetName == buyOrder.sellAssetName) &&
                                                inputAsset.amount.quantity == buyOrder.sellAssetQty.toLong())
                                """buy asset should equal to "buy asset" in order""" using
                                        ((outputAsset.assetName == buyOrder.buyAssetName) &&
                                                outputAsset.amount.quantity == buyOrder.buyAssetQty.toLong())
                                "owner of input state and output state and order should be same!" using
                                        ((outputAsset.owner == buyOrder.owner) &&
                                                (inputAsset.owner == buyOrder.owner))
                            }
                        } else throw Exception("SellOrderState should exist in input!")
                    }
                    is Commands.Issue -> {
                        val output = outputs.single()
                        if (output is AssetState) {
                            requireThat {
                                "Inputs should be empty" using inputs.isEmpty()
                                "Asset qty should be more than 0" using (output.amount.quantity > 0)
                                "Output states are issued by a command signer" using (output.owner.owningKey in command.signers)
                            }
                        } else throw Exception("Output state should be asset state!")
                    }
                    is Commands.ShareAsset -> {
                        val input = inputs.find { state -> state is AssetState }
                        val output = outputs.find { state -> state is AssetState }
                        requireThat {
                            "input size should equal 1" using (inputs.size == 1)
                            "output size should equal 1" using (outputs.size == 1)
                        }
                        if (input is AssetState && output is AssetState) {
                            requireThat {
                                "input and output owner should be same party" using (input.owner == output.owner)
                                "input and output assets should bew equal" using (
                                        (input.assetName == output.assetName) && (input.amount == output.amount))
                            }
                        } else throw Exception("input & ouput should be AssetState")
                    }
                }
            }
        }
    }
}