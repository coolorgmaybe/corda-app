package com.template.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction

class OrderContract : Contract {

    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.template.contracts.OrderContract"
    }

    interface Commands : CommandData {
        class Issue : TypeOnlyCommandData(), Commands
        class Execute : TypeOnlyCommandData(), Commands
    }

    override fun verify(tx: LedgerTransaction) {

    }
}