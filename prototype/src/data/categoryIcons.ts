/** База иконок категорий Famly — единый stroke-стиль, 24×24 */

export type CategoryIconId =
  | 'groceries'
  | 'transport'
  | 'cafe'
  | 'home'
  | 'entertainment'
  | 'health'
  | 'clothes'
  | 'gifts'
  | 'education'
  | 'kids'
  | 'pets'
  | 'sport'
  | 'travel'
  | 'beauty'
  | 'phone'
  | 'subscriptions'
  | 'car'
  | 'fuel'
  | 'salary'
  | 'freelance'
  | 'investment'
  | 'rent'
  | 'utilities'
  | 'tax'
  | 'other'
  | 'savings'
  | 'family'
  | 'restaurant'

export interface CategoryIconDef {
  id: CategoryIconId
  label: string
  color: string
  /** SVG path(s), viewBox 0 0 24 24 */
  paths: string[]
  types: ('expense' | 'income')[]
}

export const CATEGORY_ICONS: CategoryIconDef[] = [
  { id: 'groceries', label: 'Продукты', color: '#E63946', types: ['expense'], paths: ['M6 6h12l-1 12H7L6 6z', 'M9 6V4a3 3 0 016 0v2'] },
  { id: 'transport', label: 'Транспорт', color: '#457B9D', types: ['expense'], paths: ['M4 16h16v2H4z', 'M6 16l-1-6h14l-1 6', 'M8 10h8', 'M7 16a1.5 1.5 0 103 0', 'M14 16a1.5 1.5 0 103 0'] },
  { id: 'cafe', label: 'Кафе', color: '#F4A261', types: ['expense'], paths: ['M6 8h10v6a4 4 0 01-4 4H8a4 4 0 01-4-4V8z', 'M16 10h2a2 2 0 010 4h-2', 'M8 20h8'] },
  { id: 'restaurant', label: 'Ресторан', color: '#E76F51', types: ['expense'], paths: ['M8 4v8H6V4', 'M10 4v8', 'M12 4v8h2v8', 'M6 20h12'] },
  { id: 'home', label: 'ЖКХ', color: '#6D597A', types: ['expense'], paths: ['M4 10l8-6 8 6v10H4V10z', 'M10 20v-6h4v6'] },
  { id: 'rent', label: 'Аренда', color: '#5C4D7D', types: ['expense'], paths: ['M3 11l9-7 9 7v9H3v-9z', 'M9 20v-5h6v5'] },
  { id: 'utilities', label: 'Коммуналка', color: '#7B6CF6', types: ['expense'], paths: ['M13 2L4 14h7l-1 8 10-14h-7l0-6z'] },
  { id: 'entertainment', label: 'Развлечения', color: '#E76F51', types: ['expense'], paths: ['M4 8h16v10H4z', 'M8 8l4 4 4-4'] },
  { id: 'health', label: 'Здоровье', color: '#2A9D8F', types: ['expense'], paths: ['M12 21s-6-4.5-6-9a4 4 0 017-2 4 4 0 017 2c0 4.5-6 9-6 9z'] },
  { id: 'clothes', label: 'Одежда', color: '#9B5DE5', types: ['expense'], paths: ['M8 4l-4 4v2h16v-2l-4-4', 'M6 10v10h12V10'] },
  { id: 'gifts', label: 'Подарки', color: '#F72585', types: ['expense'], paths: ['M4 10h16v10H4z', 'M12 10v10', 'M12 10c-2-3-4-3-4-1s2-2 4 1', 'M12 10c2-3 4-3 4-1s-2-2-4 1', 'M12 7V4'] },
  { id: 'education', label: 'Обучение', color: '#4361EE', types: ['expense', 'income'], paths: ['M12 3L2 8l10 5 10-5-10-5z', 'M6 10v5c0 2 3 4 6 4s6-2 6-4v-5'] },
  { id: 'kids', label: 'Дети', color: '#FF6B9D', types: ['expense'], paths: ['M12 12a4 4 0 100-8 4 4 0 000 8z', 'M6 20v-1a6 6 0 0112 0v1'] },
  { id: 'family', label: 'Семья', color: '#2D6A4F', types: ['expense', 'income'], paths: ['M9 11a2.5 2.5 0 100-5 2.5 2.5 0 000 5z', 'M15 11a2 2 0 100-4 2 2 0 000 4z', 'M4 20v-1a4 4 0 014-4h0', 'M16 15a3 3 0 013 3v2', 'M7 20v-1a3 3 0 013-3h2a3 3 0 013 3v1'] },
  { id: 'pets', label: 'Питомцы', color: '#BC6C25', types: ['expense'], paths: ['M8 8a2 2 0 100-4 2 2 0 000 4z', 'M16 8a2 2 0 100-4 2 2 0 000 4z', 'M6 14a2 2 0 100-4 2 2 0 000 4z', 'M18 14a2 2 0 100-4 2 2 0 000 4z', 'M12 18c-3 0-5 2-5 4h10c0-2-2-4-5-4z'] },
  { id: 'sport', label: 'Спорт', color: '#06AED5', types: ['expense'], paths: ['M12 21a9 9 0 100-18 9 9 0 000 18z', 'M12 3v18', 'M3 12h18', 'M5 5l14 14', 'M19 5L5 19'] },
  { id: 'travel', label: 'Путешествия', color: '#118AB2', types: ['expense'], paths: ['M4 18l8-14 8 14H4z', 'M12 9v4', 'M10 13h4'] },
  { id: 'beauty', label: 'Красота', color: '#FF85A1', types: ['expense'], paths: ['M12 3c-2 4-6 5-6 9a6 6 0 0012 0c0-4-4-5-6-9z', 'M9 20h6'] },
  { id: 'phone', label: 'Связь', color: '#4CC9F0', types: ['expense'], paths: ['M8 2h8v20H8z', 'M10 18h4'] },
  { id: 'subscriptions', label: 'Подписки', color: '#7209B7', types: ['expense'], paths: ['M4 6h16v12H4z', 'M4 10h16', 'M8 14h4'] },
  { id: 'car', label: 'Авто', color: '#3A5A40', types: ['expense'], paths: ['M4 14h16l-2-6H6l-2 6z', 'M6 14v3h12v-3', 'M7 17a1 1 0 102 0', 'M15 17a1 1 0 102 0'] },
  { id: 'fuel', label: 'Бензин', color: '#D4A373', types: ['expense'], paths: ['M6 4h8v16H6z', 'M14 8h4l-2 4v8h-2', 'M8 8h4'] },
  { id: 'tax', label: 'Налоги', color: '#6C757D', types: ['expense'], paths: ['M5 4h14v16H5z', 'M8 8h8', 'M8 12h8', 'M8 16h5'] },
  { id: 'other', label: 'Другое', color: '#8A9390', types: ['expense', 'income'], paths: ['M12 6a2 2 0 100-4 2 2 0 000 4z', 'M6 20v-1a6 6 0 0112 0v1'] },
  { id: 'salary', label: 'Зарплата', color: '#2D6A4F', types: ['income'], paths: ['M4 8h16v12H4z', 'M8 8V6a4 4 0 018 0v2', 'M12 13v3', 'M10 15h4'] },
  { id: 'freelance', label: 'Фриланс', color: '#40916C', types: ['income'], paths: ['M4 6h16v12H4z', 'M8 6V4h8v2', 'M8 12h8'] },
  { id: 'investment', label: 'Инвестиции', color: '#1B4332', types: ['income'], paths: ['M4 18V6l8-2 8 2v12', 'M8 14l3-3 3 2 4-5'] },
  { id: 'savings', label: 'Накопления', color: '#52B788', types: ['income', 'expense'], paths: ['M12 3C8 3 5 6 5 9c0 4 3 5 7 8 4-3 7-4 7-8 0-3-3-6-7-6z', 'M12 11v6'] },
]

export function getCategoryIcon(id: string): CategoryIconDef {
  return CATEGORY_ICONS.find((i) => i.id === id) ?? CATEGORY_ICONS.find((i) => i.id === 'other')!
}

export function iconsForType(type: 'expense' | 'income'): CategoryIconDef[] {
  return CATEGORY_ICONS.filter((i) => i.types.includes(type))
}

export const DEFAULT_EXPENSE_ICON: CategoryIconId = 'other'
export const DEFAULT_INCOME_ICON: CategoryIconId = 'salary'
