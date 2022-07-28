package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.*
import net.corda.core.utilities.ProgressTracker
import net.corda.core.flows.FinalityFlow

import net.corda.core.flows.FlowSession

import net.corda.core.identity.Party

import com.template.contracts.TemplateContract

import net.corda.core.transactions.TransactionBuilder

import net.corda.core.contracts.Command;
import com.template.states.AssetState;


// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class IssueAssetFlow(val purity: Double,
                     val serialNumber: String,
                     val weight: Double,
                     val description: String,
                     val owner: Party
               ) : FlowLogic<Unit>() {

    /** The progress tracker provides checkpoints indicating the progress of
    the flow to observers. */
    override val progressTracker = ProgressTracker()

    /** The flow logic is encapsulated within the call() method. */
    @Suspendable
    override fun call() {
        // We retrieve the notary identity from the network map.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        // We create the transaction components.
        val outputState = AssetState(purity, serialNumber, weight, description, ourIdentity, owner)
        val command = Command(TemplateContract.Commands.Create(), ourIdentity.owningKey)

        // We create a transaction builder and add the components.
        val txBuilder = TransactionBuilder(notary = notary)
                .addOutputState(outputState, TemplateContract.ID)
                .addCommand(command)

        // We sign the transaction.
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        // Creating a session with the other party.
        val otherPartySession = initiateFlow(owner)

        // We finalise the transaction and then send it to the counterparty.
        subFlow(FinalityFlow(signedTx, otherPartySession))
    }
}

@InitiatedBy(IssueAssetFlow::class)
class AssetFlowResponder(private val otherPartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        subFlow(ReceiveFinalityFlow(otherPartySession))
    }
}

