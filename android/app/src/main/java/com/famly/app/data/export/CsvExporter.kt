package com.famly.app.data.export

import com.famly.app.data.local.entity.AccountEntity
import com.famly.app.data.local.entity.CategoryEntity
import com.famly.app.data.local.entity.TransactionEntity
import com.famly.app.domain.MoneyFormatter

object CsvExporter {

    private const val BOM = "\uFEFF"

    /**
     * Exports pre-filtered transactions. Apply [daysLimit] filtering in the repository for free tier.
     */
    fun export(
        transactions: List<TransactionEntity>,
        categories: List<CategoryEntity>,
        accounts: List<AccountEntity>,
    ): String {
        val categoryMap = categories.associateBy { it.id }
        val accountMap = accounts.associateBy { it.id }

        val filtered = transactions.sortedByDescending { it.dateEpochDay }

        return buildString {
            append(BOM)
            appendLine("date;type;category;account;amount_rubles;note")
            filtered.forEach { tx ->
                val cat = categoryMap[tx.categoryId]?.name ?: ""
                val acc = accountMap[tx.accountId]?.name ?: ""
                val note = (tx.note ?: "").replace("\"", "\"\"")
                val amount = tx.amountKopecks / 100.0
                val date = MoneyFormatter.formatShortDate(tx.dateEpochDay)
                appendLine("$date;${tx.type};$cat;$acc;$amount;\"$note\"")
            }
        }
    }
}
