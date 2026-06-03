package com.famly.app.domain.iou

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IouNettingTest {

    @Test
    fun netIouBalances_singleDebt_unchanged() {
        val result = netIouBalances(listOf(IouBalance("a", "b", 1000)))
        assertEquals(1, result.size)
        assertEquals(IouBalance("a", "b", 1000), result.first())
    }

    @Test
    fun netIouBalances_oppositeDebts_cancelOut() {
        val result = netIouBalances(
            listOf(
                IouBalance("a", "b", 1000),
                IouBalance("b", "a", 600),
            ),
        )
        assertEquals(1, result.size)
        assertEquals(IouBalance("a", "b", 400), result.first())
    }

    @Test
    fun netIouBalances_equalOppositeDebts_produceEmpty() {
        val result = netIouBalances(
            listOf(
                IouBalance("a", "b", 500),
                IouBalance("b", "a", 500),
            ),
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun netIouBalances_multiplePairs_sortedByAmountDesc() {
        val result = netIouBalances(
            listOf(
                IouBalance("a", "b", 300),
                IouBalance("c", "d", 900),
            ),
        )
        assertEquals(2, result.size)
        assertEquals(900L, result[0].amountKopecks)
        assertEquals(300L, result[1].amountKopecks)
    }

    @Test
    fun netIouBalances_reverseNetDirection() {
        val result = netIouBalances(
            listOf(
                IouBalance("a", "b", 200),
                IouBalance("b", "a", 800),
            ),
        )
        assertEquals(1, result.size)
        assertEquals(IouBalance("b", "a", 600), result.first())
    }
}
