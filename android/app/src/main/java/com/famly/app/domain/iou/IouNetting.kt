package com.famly.app.domain.iou

data class IouBalance(
    val fromId: String,
    val toId: String,
    val amountKopecks: Long,
)

fun netIouBalances(balances: List<IouBalance>): List<IouBalance> {
    val debt = mutableMapOf<String, MutableMap<String, Long>>()

    for (bal in balances) {
        debt.getOrPut(bal.fromId) { mutableMapOf() }
        debt[bal.fromId]!![bal.toId] = (debt[bal.fromId]!![bal.toId] ?: 0L) + bal.amountKopecks
    }

    val result = mutableListOf<IouBalance>()
    val processed = mutableSetOf<String>()

    for (fromId in debt.keys) {
        for (toId in debt[fromId]!!.keys) {
            val pairKey = listOf(fromId, toId).sorted().joinToString("|")
            if (pairKey in processed) continue
            processed.add(pairKey)

            val forward = debt[fromId]?.get(toId) ?: 0L
            val backward = debt[toId]?.get(fromId) ?: 0L
            val net = forward - backward

            when {
                net > 0 -> result.add(IouBalance(fromId, toId, net))
                net < 0 -> result.add(IouBalance(toId, fromId, -net))
            }
        }
    }

    return result.sortedByDescending { it.amountKopecks }
}
