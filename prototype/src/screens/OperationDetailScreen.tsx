import { useNavigate, useParams } from 'react-router-dom'
import { CategoryIcon } from '../components/CategoryIcon'
import { useSetPageHeader } from '../context/HeaderContext'
import { useApp } from '../context/AppContext'
import { formatMoney } from '../data/mockData'
import { getTheme } from '../theme'

export function OperationDetailScreen() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { transactions, categories, accounts, deleteTransaction, settings } = useApp()
  const theme = getTheme(settings.theme)

  useSetPageHeader({ showBack: true, backTo: '/operations' })

  const tx = transactions.find((t) => t.id === id)
  if (!tx) {
    return <p style={{ padding: 16, color: theme.textMuted }}>Операция не найдена</p>
  }

  const cat = categories.find((c) => c.id === tx.categoryId)
  const acc = accounts.find((a) => a.id === tx.accountId)

  const handleDelete = () => {
    deleteTransaction(tx.id)
    navigate('/operations')
  }

  return (
    <div>
      <div style={{ padding: 24, textAlign: 'center' }}>
        {cat && <CategoryIcon iconId={cat.iconId} size={32} />}
        <p
          style={{
            margin: '16px 0 4px',
            fontSize: 36,
            fontWeight: 700,
            color: tx.type === 'expense' ? theme.expense : theme.income,
          }}
        >
          {tx.type === 'expense' ? '−' : '+'}
          {formatMoney(tx.amount)}
        </p>
        <p style={{ margin: 0, fontSize: 18, color: theme.text }}>{cat?.name}</p>
        {tx.note && <p style={{ margin: '8px 0 0', color: theme.textSecondary }}>{tx.note}</p>}
      </div>

      <div style={{ padding: '0 16px' }}>
        {[
          ['Дата', tx.date],
          ['Счёт', `${acc?.icon} ${acc?.name}`],
          ['Тип', tx.type === 'expense' ? 'Расход' : 'Доход'],
          ['Повтор', tx.isRecurring ? 'Каждый месяц' : 'Нет'],
        ].map(([label, value]) => (
          <div
            key={label}
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              padding: '14px 0',
              borderBottom: `1px solid ${theme.border}`,
            }}
          >
            <span style={{ color: theme.textMuted }}>{label}</span>
            <span style={{ color: theme.text, fontWeight: 500 }}>{value}</span>
          </div>
        ))}
      </div>

      <div style={{ padding: 16, display: 'flex', flexDirection: 'column', gap: 8 }}>
        <button
          type="button"
          onClick={() => navigate('/split/' + tx.id)}
          style={{
            padding: 14,
            borderRadius: 12,
            border: `1px solid ${theme.border}`,
            background: theme.surface,
            color: theme.text,
            fontWeight: 600,
            cursor: 'pointer',
          }}
        >
          Разделить с семьёй (Premium)
        </button>
        <button
          type="button"
          onClick={handleDelete}
          style={{
            padding: 14,
            borderRadius: 12,
            border: 'none',
            background: `${theme.expense}15`,
            color: theme.expense,
            fontWeight: 600,
            cursor: 'pointer',
          }}
        >
          Удалить
        </button>
      </div>
    </div>
  )
}
