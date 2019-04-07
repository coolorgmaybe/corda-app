package com.template.states

import com.template.contracts.AssetContract
import net.corda.core.contracts.OwnableState
import net.corda.core.crypto.NullKeys
import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.AnonymousParty
import net.corda.core.identity.Party

@BelongsToContract(AssetContract::class)
data class AssetState(override val owner: AbstractParty,
                      val assetName: String,
                      override val amount: Amount<Int>): OwnableState, FungibleState<Int> {

    override val participants: List<AbstractParty> get() = listOf(owner)

    fun withoutOwner() = copy(owner = AnonymousParty(NullKeys.NullPublicKey))

    override fun withNewOwner(newOwner: AbstractParty) =
            CommandAndState(AssetContract.Commands.Move(), copy(owner = newOwner))
}