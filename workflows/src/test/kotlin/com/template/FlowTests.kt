package com.template

import net.corda.testing.node.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.template.states.AssetState
import java.util.concurrent.Future;
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import com.template.flows.IssueAssetFlow
import com.template.flows.TransferFlow
import junit.framework.Assert.assertEquals
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.services.Vault.StateStatus


class FlowTests {
    private lateinit var network: MockNetwork
    private lateinit var gilded: StartedMockNode
    private lateinit var a: StartedMockNode
    private lateinit var b: StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(
                TestCordapp.findCordapp("com.template.contracts"),
                TestCordapp.findCordapp("com.template.flows")
        )))
        gilded = network.createPartyNode(CordaX500Name("Gilded", "London", "GB"))
        a = network.createPartyNode(CordaX500Name("PartyA", "London", "GB"))
        b = network.createPartyNode(CordaX500Name("PartyB", "London", "GB"))
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }
    @Test
    fun IssueFlowTest() {
        val flow = IssueAssetFlow(99.99, "0001", 100.0, "a gold bar", b.info.legalIdentitiesAndCerts[0].party)
        val future = gilded.startFlow(flow)
        network.runNetwork()
        network.waitQuiescent()

        //successful query means the state is stored at node b's vault. Flow went through.
        val inputCriteria: QueryCriteria = QueryCriteria.VaultQueryCriteria().withStatus(StateStatus.UNCONSUMED)
        val vaultStates = b.services.vaultService.queryBy(AssetState::class.java).states
        assertEquals(1, vaultStates.size)
        val stateSerialNumber = b.services.vaultService.queryBy(AssetState::class.java, inputCriteria).states[0].state.data.serialNumber
        assertEquals(stateSerialNumber, "0001")
    }

    @Test
    fun TransferFlowTest() {
        val issueFlow = IssueAssetFlow(99.99, "0001", 100.0, "a gold bar", b.info.legalIdentitiesAndCerts[0].party)
        val issueFuture = gilded.startFlow(issueFlow)
        network.runNetwork()
        network.waitQuiescent()
        val inputCriteria: QueryCriteria = QueryCriteria.VaultQueryCriteria().withStatus(StateStatus.UNCONSUMED)
        var bVaultState = b.services.vaultService.queryBy(AssetState::class.java, inputCriteria).states
        val bAssetStateId = bVaultState[0].state.data.linearId
        println("b asset id: $bAssetStateId")
        val transferFlow = TransferFlow(bAssetStateId, a.info.legalIdentitiesAndCerts[0].party)
        val transferFuture = b.startFlow(transferFlow)
        network.runNetwork()
        network.waitQuiescent()
        bVaultState = b.services.vaultService.queryBy(AssetState::class.java, inputCriteria).states
        assertEquals(0, bVaultState.size)
        val aVaultState = a.services.vaultService.queryBy(AssetState::class.java, inputCriteria).states
        assertEquals(1, aVaultState.size)
    }
}