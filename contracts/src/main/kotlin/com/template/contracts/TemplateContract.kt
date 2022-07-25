package com.template.contracts

import com.template.states.AssetState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.Requirements.using
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.contracts.requireThat
// ************
// * Contract *
// ************
class TemplateContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.template.contracts.TemplateContract"
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Verification logic goes here.
        val command = tx.findCommand<Commands> { true }

        when (command.value) {
            is Commands.Create -> {
                val output = tx.outputsOfType<AssetState>().single()

                requireThat {
                    // tx.inputs.isEmpty == true ? continue : "No inputs should be consumed when issuing an asset"
                    "No inputs should be consumed when issuing an asset" using (tx.inputs.isEmpty())
                    "Only output state of type AssetState should be produced" using (tx.outputs.size == 1)
                    "The serial number string is an empty string" using (output.serialNumber != "")
                    "The weight of the asset is not positive" using (output.weight > 0)
                    "The purity of the asset is less than four 9s" using (output.purity > 99.98)
                    "The asset has no description" using (output.description != "")

                    val expectedSigners = listOf(output.issuer.owningKey)
                    "There must be one signer" using (command.signers.toSet().size == 1)
                    "The issuer must be the signer." using (command.signers.containsAll(expectedSigners))
                }
            }
            is Commands.Transfer -> {
                val output = tx.outputsOfType<AssetState>().single()
                val expectedSigners = listOf(output.issuer.owningKey, output.owner.owningKey);
                "There must be two signers" using (command.signers.toSet().size == 2)
                "The expected signers don't match the listed signers." using (command.signers.containsAll(expectedSigners))
                "Only input state of type AssetState should be produced" using (tx.inputs.size == 1)
                "Only output state of type AssetState should be produced" using (tx.outputs.size == 1)
            }
        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Create : Commands
        class Transfer : Commands
    }
}