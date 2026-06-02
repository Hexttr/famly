import { PremiumGate } from '../components/PremiumGate'
import { useSetPageHeader } from '../context/HeaderContext'
import { useApp } from '../context/AppContext'
import { formatMoney, getCategorySpent } from '../data/mockData'

export function AdvancedReportsScreen() {
  const { categories, transactions } = useApp()

  useSetPageHeader({ showBack: true, backTo: '/more' })

  const currentSpent = categories
    .filter((c) => c.type === 'expense')
    .reduce((s, c) => s + getCategorySpent(c.id, transactions), 0)

  return (
    <PremiumGate feature="Расширенная аналитика">
      <div style={{ padding: 16 }}>
        <p>Тренд 3 месяцев</p>
        <p>Текущий период: {formatMoney(currentSpent)}</p>
        <p style={{ color: '#E63946' }}>+8% к прошлому периоду</p>
      </div>
    </PremiumGate>
  )
}
