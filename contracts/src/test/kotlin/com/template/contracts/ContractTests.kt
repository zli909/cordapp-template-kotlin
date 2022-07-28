package com.template.contracts

import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test
import com.template.states.AssetState
import net.corda.core.contracts.UniqueIdentifier

class ContractTests {
    var gilded = TestIdentity(CordaX500Name("Alice", "TestLand", "US"))
    var bob = TestIdentity(CordaX500Name("Alice", "TestLand", "US"))
    var charlie = TestIdentity(CordaX500Name("Alice", "TestLand", "US"))
    private val ledgerServices: MockServices = MockServices(listOf("com.template"), gilded, bob)


    @Test
    fun dummytest() {
//        val intialState = AssetState("Hello-World", alice.party, bob.party)
        val initialState = AssetState(99.99, "1234", 100.0, "a gold bar", gilded.party, charlie.party, UniqueIdentifier())
        val finalState = AssetState(99.99, "1234", 100.0, "a gold bar", gilded.party, bob.party, UniqueIdentifier())
        ledgerServices.ledger {
            // Create portion of the contract
            transaction {
                //passing transaction
                output(TemplateContract.ID, initialState)
                command(gilded.publicKey, TemplateContract.Commands.Create())
                verifies()
            }
            //Transfer command of the contract
            transaction {
                input(TemplateContract.ID, initialState)
                output(TemplateContract.ID, finalState)
                command(listOf(charlie.publicKey, bob.publicKey), TemplateContract.Commands.Transfer())
                verifies()
            }
            //pass
//            transaction {
//                //passing transaction
//                output(TemplateContract.ID, state)
//                command(alice.publicKey, TemplateContract.Commands.Create())
//                verifies()
//            }
        }
    }
}