package com.template.states

import com.template.contracts.TemplateContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party


// *********
// * State *
// *********
@BelongsToContract(TemplateContract::class)
class AssetState (val purity: Double,
                 val serialNumber: String,
                 val weight: Double,
                 val description: String,
                 val issuer: Party,
                 var owner: Party,
                 override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState {
    override val participants get() = listOf(issuer, owner)
}