package com.template.webserver

import com.template.flows.*
import com.template.states.AssetState
import net.corda.core.contracts.ContractState
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.toBase58String
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
class Controller(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @GetMapping(value = "/templateendpoint", produces = arrayOf("text/plain"))
    private fun templateendpoint(): String {
        return "Define an endpoint here."
    }

    @GetMapping(value = "/status", produces = arrayOf("text/plain"))
    private fun status() = "200"

    @GetMapping(value = "/servertime", produces = arrayOf("text/plain"))
    private fun serverTime() = LocalDateTime.ofInstant(proxy.currentNodeTime(), ZoneId.of("UTC")).toString()

    @GetMapping(value = "/addresses", produces = arrayOf("text/plain"))
    private fun addresses() = proxy.nodeInfo().addresses.toString()

    @GetMapping(value = "/identities", produces = arrayOf("text/plain"))
    private fun identities() = proxy.nodeInfo().legalIdentities.toString()

    @GetMapping(value = "/platformversion", produces = arrayOf("text/plain"))
    private fun platformVersion() = proxy.nodeInfo().platformVersion.toString()

    @GetMapping(value = "/peers", produces = arrayOf("text/plain"))
    private fun peers() = proxy.networkMapSnapshot().flatMap { it.legalIdentities }.toString()

    @GetMapping(value = "/notaries", produces = arrayOf("text/plain"))
    private fun notaries() = proxy.notaryIdentities().toString()

    @GetMapping(value = "/flows", produces = arrayOf("text/plain"))
    private fun flows() = proxy.registeredFlows().toString()

    @GetMapping(value = "/states", produces = arrayOf("text/plain"))
    private fun states(): String {
        val a = proxy.vaultQueryBy<AssetState>().states
        val b = a.map { k ->
            """{ "assetName":"${k.state.data.assetName}", "owner":"${k.state.data.owner.owningKey.toBase58String()}", "assetQty":"${k.state.data.amount.quantity}" }"""
        }
        return b.toString()
    }

    @GetMapping(value = "/assetBuyerFlow", produces = arrayOf("text/plain"))
    private fun assetBuyerFlow(): String {
        proxy.startFlowDynamic(AssetBuyerFlow::class.java, *arrayOf())
        return """{ "":"" } """
    }

    @GetMapping(value = "/assetIssueFlowBTC", produces = arrayOf("text/plain"))
    private fun assetIssueFlowWood(): String {
        val x500Name = CordaX500Name.parse("O=PartyC,L=Moscow,C=US")
        val party = proxy.wellKnownPartyFromX500Name(x500Name)
        proxy.startFlowDynamic(AssetIssueFlow::class.java, "BTC", 100, party)
        return """{ "":"" } """
    }

    @GetMapping(value = "/assetIssueFlowDollar", produces = arrayOf("text/plain"))
    private fun assetIssueFlowStone(): String {
        val x500Name = CordaX500Name.parse("O=PartyC,L=Moscow,C=US")
        val party = proxy.wellKnownPartyFromX500Name(x500Name)
        proxy.startFlowDynamic(AssetIssueFlow::class.java, "Dollar", 10, party)
        return """{ "":"" } """
    }

    @GetMapping(value = "/makeMatch", produces = arrayOf("text/plain"))
    private fun match(): String {
        val x500NameSeller = CordaX500Name.parse("O=PartyA, L=London, C=GB")
        val sellParty = proxy.wellKnownPartyFromX500Name(x500NameSeller)
        val x500NameBuyer = CordaX500Name.parse("O=PartyB,L=New York,C=US")
        val ownerParty = proxy.wellKnownPartyFromX500Name(x500NameBuyer)
        val sellOrderOwner = sellParty
        val sellOrderSellAssetName = "BTC"
        val sellOrderSellAssetQty = 100
        val sellOrderBuyAssetName = "Dollar"
        val sellOrderBuyAssetQty = 10
        val buyOrderOwner = ownerParty
        val buyOrderSellAssetName = "Dollar"
        val buyOrderSellAssetQty = 10
        val buyOrderBuyAssetName = "BTC"
        val buyOrderBuyAssetQty = 100
        proxy.startFlowDynamic(SellToBuyMatcherFlow::class.java,
                sellOrderOwner,
                sellOrderSellAssetName,
                sellOrderSellAssetQty,
                sellOrderBuyAssetName,
                sellOrderBuyAssetQty,
                buyOrderOwner,
                buyOrderSellAssetName,
                buyOrderSellAssetQty,
                buyOrderBuyAssetName,
                buyOrderBuyAssetQty)
        return """{ "":"" } """
    }

    @GetMapping(value = "/partyMatchFlow", produces = arrayOf("text/plain"))
    private fun partyMatchFlow(): String {
        proxy.startFlowDynamic(PartyMatchFlow::class.java, *arrayOf())
        return "partyMatchFlow"
    }

    @GetMapping(value = "/sellOrderIssueFlowBTC", produces = arrayOf("text/plain"))
    private fun sellOrderIssueFlowWood(): String {
        val x500Name = CordaX500Name.parse("O=PartyC,L=Moscow,C=US")
        val party = proxy.wellKnownPartyFromX500Name(x500Name)
        proxy.startFlowDynamic(SellOrderIssueFlow::class.java, "BTC", 100, "Dollar", 10, party)
        return """{ "":"" } """
    }

    @GetMapping(value = "/buyOrderIssueFlowDollar", produces = arrayOf("text/plain"))
    private fun sellOrderIssueFlowStone(): String {
        val x500Name = CordaX500Name.parse("O=PartyC,L=Moscow,C=US")
        val party = proxy.wellKnownPartyFromX500Name(x500Name)
        proxy.startFlowDynamic(BuyOrderIssueFlow::class.java, "Dollar", 10, "BTC", 100, party)
        return """{ "":"" } """
    }

    @GetMapping(value = "/sellOrderIssueMatcherFlow", produces = arrayOf("text/plain"))
    private fun sellOrderIssueMatcherFlow(): String {
        proxy.startFlowDynamic(SellOrderIssueMatcherFlow::class.java, *arrayOf())
        return "sellOrderIssueMatcherFlow"
    }

    @GetMapping(value = "/sellToBuyMatcherFlow", produces = arrayOf("text/plain"))
    private fun sellToBuyMatcherFlow(): String {
        val x500Name = CordaX500Name.parse("O=PartyC,L=Moscow,C=US")
        val x500Name1 = CordaX500Name.parse("O=PartyB,L=New York,C=US")
        val party = proxy.wellKnownPartyFromX500Name(x500Name)
        val party1 = proxy.wellKnownPartyFromX500Name(x500Name1)
        proxy.startFlowDynamic(SellToBuyMatcherFlow::class.java, party, "BTC", 100, "Dollar", 10, party1, "Dollar", 10)
        return """{ "":"" } """
    }

    @GetMapping(value = "/buyOrderIssueMatcherFlow", produces = arrayOf("text/plain"))
    private fun buyOrderIssueMatcherFlow(): String {
        proxy.startFlowDynamic(BuyOrderIssueMatcherFlow::class.java, *arrayOf())
        return "buyOrderIssueMatcherFlow"
    }

    @GetMapping(value = "/buyOrderIssueFlow", produces = arrayOf("text/plain"))
    private fun buyOrderIssueFlow(): String {
        proxy.startFlowDynamic(BuyOrderIssueFlow::class.java, *arrayOf())
        return "buyOrderIssueFlow"
    }

    @GetMapping(value = "/assetShareToMatcherFlow", produces = arrayOf("text/plain"))
    private fun assetShareToMatcherFlow(): String {
        proxy.startFlowDynamic(AssetShareToMatcherFlow::class.java, *arrayOf())
        return "assetShareToMatcherFlow"
    }

    @GetMapping(value = "/assetIssueMathcerFlow", produces = arrayOf("text/plain"))
    private fun assetIssueMathcerFlow(): String {
        proxy.startFlowDynamic(AssetIssueMathcerFlow::class.java, *arrayOf())
        return "assetIssueMathcerFlow"
    }

    @GetMapping(value = "/assetSellerFlow", produces = arrayOf("text/plain"))
    private fun assetSellerFlow(): String {
        proxy.startFlowDynamic(AssetSellerFlow::class.java, *arrayOf())
        return "assetSellerFlow"
    }

//    @GetMapping(value = "/getQuery", produces = arrayOf("text/plain"))
//    private fun queries() = proxy.startFlowDynamic(AssetIssueFlow.class,)
}