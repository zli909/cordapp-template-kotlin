//package com.template.flows
//
//import co.paralleluniverse.fibers.Suspendable
//import net.corda.core.flows.*
//import net.corda.core.utilities.ProgressTracker
//import net.corda.core.flows.FinalityFlow
//
//import net.corda.core.flows.CollectSignaturesFlow
//
//import net.corda.core.transactions.SignedTransaction
//
//import java.util.stream.Collectors
//
//import net.corda.core.flows.FlowSession
//
//import net.corda.core.identity.Party
//
//import com.template.contracts.TemplateContract
//
//import net.corda.core.transactions.TransactionBuilder
//
//import net.corda.core.contracts.Command;
//import com.template.states.GoldState;
//import net.corda.core.contracts.UniqueIdentifier
//
//@InitiatingFlow
//@StartableByRPC
//class TransferFlow(val id : UniqueIdentifier, val newOwner: Party) : FlowLogic<Unit>() {
//    @Suspendable
//    override fun call() {
//        val notary = serviceHub.networkMapCache.notaryIdentities[0]
//
//        // We create the transaction components.
//        val inputState = serviceHub.vaultService
//        val outputState = GoldState(purity, serialNumber, weight, description, ourIdentity, newIdentity)
//    }
//}