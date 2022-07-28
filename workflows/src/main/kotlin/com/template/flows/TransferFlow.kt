package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.TemplateContract
import com.template.states.AssetState
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.lang.IllegalArgumentException
import java.security.PublicKey

@InitiatingFlow
@StartableByRPC
class TransferFlow(val id: UniqueIdentifier, val newOwner: Party) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        print("Starting transfer flow\n");
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        // We create the transaction components.

        //list of linearStates on the vault
        //use vaultService to find appropriate state and ref w/ linearID
        val assetStatesList = serviceHub.vaultService.queryBy(AssetState::class.java, QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)).states
        var stateAndRefToTransfer: StateAndRef<AssetState>? = null;

        print("starting id search\n")
        for (stateAndRef in assetStatesList) {
            print("current state id: ${stateAndRef.state.data.linearId}\n")
            if (stateAndRef.state.data.linearId == id) {
                stateAndRefToTransfer = stateAndRef;
                print("successfully found state and ref of linear id $id\n");
                break;
            }
        }

        if (stateAndRefToTransfer == null) {
            print("Did not find state and ref of linear id $id\n");
            throw IllegalArgumentException();
        } else {
            var outputTransactionState = stateAndRefToTransfer.state.copy();
            outputTransactionState.data.owner = newOwner;
            val requiredSigners: List<PublicKey> = listOf(ourIdentity.owningKey, outputTransactionState.data.issuer.owningKey, newOwner.owningKey)
            val command = Command(TemplateContract.Commands.Transfer(), requiredSigners)

            val txBuilder: TransactionBuilder = TransactionBuilder(notary);

            txBuilder.withItems(
                // our input state and ref
                stateAndRefToTransfer,
                // our output transaction state
                outputTransactionState,
                // our command
                command
            )

            txBuilder.verify(serviceHub)
            print("passed serviceHub verification\n");
            val senderSignedTx: SignedTransaction = serviceHub.signInitialTransaction(txBuilder)
            val issuerSession = initiateFlow(outputTransactionState.data.issuer)
            val receiverSession = initiateFlow(newOwner);
            print("sending out for counterparty signature\n");
            val fullySignedTx = subFlow(CollectSignaturesFlow(senderSignedTx, listOf(issuerSession, receiverSession), CollectSignaturesFlow.tracker()))
            print("received counterparty signature $id\n");
            subFlow(FinalityFlow(fullySignedTx, receiverSession, issuerSession))
        }
    }
}


@InitiatedBy(TransferFlow::class)
class TransferFlowResponder(private val otherPartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val signTransactionFlow = object : SignTransactionFlow(otherPartySession) {
            override fun checkTransaction(stx: SignedTransaction) {
                print("checking transaction\n");
            }
        }
        subFlow(signTransactionFlow)
        print("finished calling signTransactionFlow in responder\n");
        subFlow(ReceiveFinalityFlow(otherPartySession))
    }
}