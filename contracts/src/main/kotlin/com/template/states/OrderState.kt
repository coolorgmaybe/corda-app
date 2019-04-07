package com.template.states

import com.template.contracts.AssetContract
import com.template.contracts.OrderContract
import net.corda.core.contracts.*
import net.corda.core.crypto.NullKeys
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.AnonymousParty

@BelongsToContract(OrderContract::class)
data class SellOrderState(override val owner: AbstractParty,
                          val sellAssetName: String,
                          val sellAssetQty: Int,
                          val buyAssetName: String,
                          val buyAssetQty: Int): OwnableState, FungibleState<Int> {

    override val amount: Amount<Int> get() = Amount<Int>(0L, 0)

    override val participants: List<AbstractParty> get() = listOf(owner)

    fun withoutOwner() = copy(owner = AnonymousParty(NullKeys.NullPublicKey))

    override fun withNewOwner(newOwner: AbstractParty) =
            CommandAndState(AssetContract.Commands.Move(), copy(owner = newOwner))
}

@BelongsToContract(OrderContract::class)
data class BuyOrderState(override val owner: AbstractParty,
                          val sellAssetName: String,
                          val sellAssetQty: Int,
                          val buyAssetName: String,
                          val buyAssetQty: Int): OwnableState, FungibleState<Int> {

    override val amount: Amount<Int> get() = Amount<Int>(0L, 0)

    override val participants: List<AbstractParty> get() = listOf(owner)

    fun withoutOwner() = copy(owner = AnonymousParty(NullKeys.NullPublicKey))

    override fun withNewOwner(newOwner: AbstractParty) =
            CommandAndState(AssetContract.Commands.Move(), copy(owner = newOwner))
}