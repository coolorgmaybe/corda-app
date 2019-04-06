package com.template.contracts

import com.template.states.AssetState
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
    }

    override fun verify(tx: LedgerTransaction) {

        logger.info("Get tx: $tx for validation")

        logger.info("Tx inputs: ${tx.inputs.size}: ${tx.inputs}")

        logger.info("tx outputs: ${tx.outputs.size}: ${tx.outputs}")

        logger.info("Tx commands: ${tx.commands.size}: ${tx.commands}")

        val groups = tx.groupStates(AssetState::withoutOwner)

        val command = tx.commands.requireSingleCommand<Commands>()

        logger.info("Groups: $groups")

        for((inputs, outputs) in groups) {
            when(command.value) {
                is Commands.Move -> {
                    val input = inputs.single()
                    requireThat {
                        "the transaction is signed by the owner of the CP" using (input.owner.owningKey in command.signers)
                        "the state is propagated" using (outputs.size == 1)
                    }
                }
                is Commands.Issue -> {
                    val output = outputs.single()
                    requireThat {
                        "Inputs should be empty" using inputs.isEmpty()
                        "Asset qty should be more than 0" using (output.assetQty > 0)
                        "Output states are issued by a command signer" using (output.owner.owningKey in command.signers)
                    }
                }
            }
        }
    }
}