package com.template.states

import com.template.contracts.TemplateContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

// *********
// * State *
// *********
@BelongsToContract(TemplateContract::class)
class GoldState(val purity: Double,
               val serialNumber: String,
               val weight: Double,
                val description: String,
                val buyer: Party,
                val seller: Party) : ContractState {
    override val participants get() = listOf(buyer, seller)
}