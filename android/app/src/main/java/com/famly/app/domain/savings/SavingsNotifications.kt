package com.famly.app.domain.savings

import com.famly.app.data.local.entity.SavingsGoalEntity
import com.famly.app.data.local.entity.SavingsLedgerEntity
import com.famly.app.data.local.entity.TransactionEntity
import java.time.LocalDate

data class SavingsNotification(
    val id: String,
    val title: String,
    val message: String,
)

object SavingsNotifications {

    fun buildNotifications(
        goal: SavingsGoalEntity?,
        ledger: List<SavingsLedgerEntity>,
        transactions: List<TransactionEntity>,
        periodStartEpochDay: Long,
        periodEndEpochDay: Long,
        periodLabel: String,
        dismissedIds: Set<String>,
        today: LocalDate = LocalDate.now(),
    ): List<SavingsNotification> {
        if (goal == null || !goal.isActive) return emptyList()
        val name = savingsGoalDisplayName(goal.goalType, goal.customName)
        val periodKey = periodLabel.replace(" ", "_")
        val result = mutableListOf<SavingsNotification>()

        goal.monthlyPlanKopecks?.let { plan ->
            if (plan > 0) {
                val contributed = SavingsGoalProcessor.monthlyContributionsKopecks(
                    ledger,
                    periodStartEpochDay,
                    periodEndEpochDay,
                )
                if (contributed * 100 < plan * 80) {
                    val id = "savings_monthly_short_$periodKey"
                    if (id !in dismissedIds) {
                        result += SavingsNotification(
                            id = id,
                            title = "Копим · $periodLabel",
                            message = "На «$name» отложено ${formatRub(contributed)} из ${formatRub(plan)} в этом месяце",
                        )
                    }
                }
            }
        }

        val incomePercent = goal.incomePercent ?: 0
        if (incomePercent > 0) {
            val periodIncome = transactions
                .filter { it.type == "income" && it.dateEpochDay in periodStartEpochDay..periodEndEpochDay }
            val dayOfPeriod = LocalDate.ofEpochDay(periodStartEpochDay).until(today).days
            val periodLength = LocalDate.ofEpochDay(periodStartEpochDay)
                .until(LocalDate.ofEpochDay(periodEndEpochDay))
                .days
                .coerceAtLeast(1)
            if (periodIncome.isEmpty() && dayOfPeriod >= periodLength / 2) {
                val id = "savings_no_income_$periodKey"
                if (id !in dismissedIds) {
                    result += SavingsNotification(
                        id = id,
                        title = "Копим · $periodLabel",
                        message = "Не было доходов — авто-отложение на «$name» не сработало",
                    )
                }
            }
        }

        val threshold = SavingsGoalProcessor.milestoneThreshold(goal.savedKopecks, goal.targetKopecks)
        if (threshold != null && threshold in listOf(25, 50, 75)) {
            val id = "savings_milestone_$threshold"
            if (id !in dismissedIds) {
                result += SavingsNotification(
                    id = id,
                    title = "Копим",
                    message = "Цель на ${threshold}%! $threshold% к «$name»",
                )
            }
        }

        if (goal.targetKopecks > 0 && goal.savedKopecks >= goal.targetKopecks) {
            val id = "savings_reached"
            if (id !in dismissedIds) {
                result += SavingsNotification(
                    id = id,
                    title = "Копим",
                    message = "Цель достигнута! 🎉 «$name»",
                )
            }
        }

        val daysSince = SavingsGoalProcessor.daysSinceLastContribution(ledger, today)
        if (daysSince != null && daysSince >= 30) {
            val id = "savings_stalled"
            if (id !in dismissedIds) {
                result += SavingsNotification(
                    id = id,
                    title = "Копим",
                    message = "Давно не пополняли цель «$name»",
                )
            }
        }

        return result
    }

    private fun formatRub(kopecks: Long): String {
        val rub = kopecks / 100
        return "$rub ₽"
    }
}
