package com.template.contracts

import com.template.states.AssetState
import org.junit.Test
import kotlin.test.assertEquals

class StateTests {
    @Test
    fun checkSerialNumberField() {
        // Does the field exist?
        AssetState::class.java.getDeclaredField("serialNumber")
        // Is the field of the correct type?
        assertEquals(AssetState::class.java.getDeclaredField("serialNumber").type, String()::class.java)
        println("serial number field exists and is of type String")
    }
}