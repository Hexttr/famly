package com.famly.backend.admin

import com.famly.backend.FamlyConfig
import com.famly.backend.models.AdminAuditDto
import com.famly.backend.models.AdminHouseholdDto
import com.famly.backend.models.AdminHouseholdListDto
import com.famly.backend.models.AdminStatsResponse
import com.famly.backend.models.AdminSyncLogSummaryDto
import com.famly.backend.models.AdminUserDto
import com.famly.backend.models.RegistrationsDayDto

object AdminTemplates {
    data class NavCounts(val users: Int, val households: Int)

    private data class NavItem(val id: String, val label: String, val path: String, val icon: String)

    private val navItems = listOf(
        NavItem("dashboard", "Обзор", "/admin/dashboard", iconDashboard),
        NavItem("users", "Пользователи", "/admin/users", iconUsers),
        NavItem("households", "Семьи", "/admin/households", iconHouseholds),
        NavItem("sync", "Sync log", "/admin/sync", iconSync),
        NavItem("audit", "Аудит", "/admin/audit", iconAudit),
        NavItem("health", "Сервер", "/admin/health", iconHealth),
    )

    private const val iconDashboard = """<svg viewBox="0 0 24 24"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>"""
    private const val iconUsers = """<svg viewBox="0 0 24 24"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>"""
    private const val iconHouseholds = """<svg viewBox="0 0 24 24"><path d="M3 10.5 12 3l9 7.5"/><path d="M5 9.5V20a1 1 0 0 0 1 1h4v-6h4v6h4a1 1 0 0 0 1-1V9.5"/></svg>"""
    private const val iconSync = """<svg viewBox="0 0 24 24"><path d="M21 12a9 9 0 0 0-9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/><path d="M3 12a9 9 0 0 0 9 9 9.75 9.75 0 0 0 6.74-2.74L21 16"/><path d="M16 16h5v5"/></svg>"""
    private const val iconAudit = """<svg viewBox="0 0 24 24"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><path d="M14 2v6h6"/><path d="M8 13h8"/><path d="M8 17h5"/></svg>"""
    private const val iconHealth = """<svg viewBox="0 0 24 24"><rect x="2" y="3" width="20" height="6" rx="1"/><rect x="2" y="15" width="20" height="6" rx="1"/><path d="M6 6h.01"/><path d="M6 18h.01"/><path d="M10 6h8"/><path d="M10 18h8"/></svg>"""
    private const val iconCollapse = """<svg viewBox="0 0 24 24"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M9 3v18"/><path d="M14 12H20"/></svg>"""
    private const val iconLogout = """<svg viewBox="0 0 24 24"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="M16 17l5-5-5-5"/><path d="M21 12H9"/></svg>"""
    private const val iconSearch = """<svg viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>"""
    private const val iconPlus = """<svg viewBox="0 0 24 24"><path d="M12 5v14"/><path d="M5 12h14"/></svg>"""
    private const val iconEdit = """<svg viewBox="0 0 24 24"><path d="M12 20h9"/><path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z"/></svg>"""
    private const val iconTrash = """<svg viewBox="0 0 24 24"><path d="M3 6h18"/><path d="M8 6V4h8v2"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6"/><path d="M10 11v6"/><path d="M14 11v6"/></svg>"""
    private const val iconMail = """<svg viewBox="0 0 24 24"><rect x="2" y="4" width="20" height="16" rx="2"/><path d="m22 7-10 7L2 7"/></svg>"""
    private const val iconUser = """<svg viewBox="0 0 24 24"><circle cx="12" cy="8" r="4"/><path d="M4 20a8 8 0 0 1 16 0"/></svg>"""
    private const val iconCalendar = """<svg viewBox="0 0 24 24"><rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4"/><path d="M8 2v4"/><path d="M3 10h18"/></svg>"""
    private const val iconHash = """<svg viewBox="0 0 24 24"><path d="M4 9h16"/><path d="M4 15h16"/><path d="M10 3 8 21"/><path d="M16 3l-2 18"/></svg>"""
    private const val iconChart = """<svg viewBox="0 0 24 24"><path d="M3 3v18h18"/><path d="M7 16V9"/><path d="M12 16V5"/><path d="M17 16v-6"/></svg>"""
    private const val iconList = """<svg viewBox="0 0 24 24"><path d="M8 6h13"/><path d="M8 12h13"/><path d="M8 18h13"/><path d="M3 6h.01"/><path d="M3 12h.01"/><path d="M3 18h.01"/></svg>"""
    private const val iconClock = """<svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>"""
    private const val iconTag = """<svg viewBox="0 0 24 24"><path d="M12 2H2v10l9.29 9.29a1 1 0 0 0 1.41 0l6.59-6.59a1 1 0 0 0 0-1.41Z"/><path d="M7 7h.01"/></svg>"""
    private const val iconLink = """<svg viewBox="0 0 24 24"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg>"""
    private const val iconActivity = """<svg viewBox="0 0 24 24"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>"""
    private const val iconGlobe = """<svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><path d="M2 12h20"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>"""
    private const val iconShield = """<svg viewBox="0 0 24 24"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>"""

    private val sidebarScript = """
        <script>
        (function () {
          var shell = document.getElementById('adminShell');
          var btn = document.getElementById('sidebarCollapse');
          var key = 'famly-admin-sidebar';
          if (localStorage.getItem(key) === 'collapsed') shell.classList.add('sidebar-collapsed');
          btn.addEventListener('click', function () {
            shell.classList.toggle('sidebar-collapsed');
            localStorage.setItem(key, shell.classList.contains('sidebar-collapsed') ? 'collapsed' : 'expanded');
          });
        })();
        </script>
    """.trimIndent()

    private val dateTimeFmt = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        .withZone(java.time.ZoneId.systemDefault())
    private val dateFmt = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")
        .withZone(java.time.ZoneId.systemDefault())

    fun escapeHtml(raw: String): String = raw
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    fun formatTs(ms: Long): String = dateTimeFmt.format(java.time.Instant.ofEpochMilli(ms))

    fun formatDay(ms: Long): String = dateFmt.format(java.time.Instant.ofEpochMilli(ms))

    private fun formatChartDay(isoDay: String): String {
        val parts = isoDay.split("-")
        return if (parts.size == 3) "${parts[2]}-${parts[1]}" else isoDay
    }

    private fun iconFor(activeNav: String): String =
        navItems.find { it.id == activeNav }?.icon ?: iconDashboard

    private fun th(icon: String, label: String): String =
        """<th><span class="th-content"><span class="th-icon">$icon</span>${escapeHtml(label)}</span></th>"""

    private fun statCard(icon: String, value: String, label: String, variant: String): String = """
        <div class="stat-card stat-card-$variant">
          <div class="stat-card-icon">$icon</div>
          <div class="stat-card-body">
            <div class="stat-card-value">${escapeHtml(value)}</div>
            <div class="stat-card-label">${escapeHtml(label)}</div>
          </div>
        </div>
    """.trimIndent()

    private fun searchForm(action: String, placeholder: String, query: String?, extraFields: String = ""): String = """
        <form class="search-form" method="get" action="$action">
          <div class="search-input-wrap">
            <span class="search-icon">$iconSearch</span>
            <input type="text" name="q" placeholder="${escapeHtml(placeholder)}" value="${query?.let { escapeHtml(it) } ?: ""}"/>
          </div>
          $extraFields
          <button type="submit" class="btn btn-primary"><span class="btn-icon">$iconSearch</span> Найти</button>
        </form>
    """.trimIndent()

    private fun paginationBlock(basePath: String, page: Int, total: Int, pageSize: Int, extraQuery: String? = null): String {
        val totalPages = ((total + pageSize - 1) / pageSize).coerceAtLeast(1)
        val extra = extraQuery?.let { "&$it" } ?: ""
        val prev = if (page > 1) """<a href="$basePath?page=${page - 1}$extra">←</a>""" else ""
        val next = if (page < totalPages) """<a href="$basePath?page=${page + 1}$extra">→</a>""" else ""
        val pages = if (totalPages <= 1) "" else """<div class="pagination">$prev <span class="current">$page / $totalPages</span> $next</div>"""
        return """
            <div class="list-footer">
              <div class="total-count"><span class="total-icon">$iconList</span><strong>Всего: $total</strong></div>
              $pages
            </div>
        """.trimIndent()
    }

    fun loginPage(error: String? = null): String = """
        <!DOCTYPE html>
        <html lang="ru">
        <head>
          <meta charset="utf-8"/>
          <meta name="viewport" content="width=device-width, initial-scale=1"/>
          <title>Вход — Famly Admin</title>
          <link rel="stylesheet" href="/admin/static/admin.css"/>
        </head>
        <body>
          <div class="login-page">
            <div class="login-card">
              <div class="login-brand">
                <img class="login-logo" src="/admin/static/logo.png" alt="Famly"/>
              </div>
              <h1>Админ-панель</h1>
              <p class="subtitle">Панель управления «Мой (Наш) Бюджет»</p>
              ${if (error != null) """<div class="alert alert-error">${escapeHtml(error)}</div>""" else ""}
              <form method="post" action="/admin/login">
                <label for="email">Email</label>
                <input id="email" name="email" type="email" required autocomplete="username"/>
                <label for="password">Пароль</label>
                <input id="password" name="password" type="password" required autocomplete="current-password"/>
                <button type="submit">Войти</button>
              </form>
            </div>
          </div>
        </body>
        </html>
    """.trimIndent()

    fun layout(
        title: String,
        activeNav: String,
        adminEmail: String,
        csrfToken: String,
        content: String,
        navCounts: NavCounts = NavCounts(0, 0),
        flash: String? = null,
        flashError: Boolean = false,
    ): String {
        val navHtml = navItems.joinToString("\n") { item ->
            val cls = if (item.id == activeNav) "active" else ""
            val badge = when (item.id) {
                "users" -> navCounts.users
                "households" -> navCounts.households
                else -> null
            }
            val badgeHtml = badge?.let { """<span class="nav-badge">$it</span>""" } ?: ""
            """<a class="$cls" href="${item.path}" title="${escapeHtml(item.label)}">
              <span class="nav-icon">${item.icon}</span>
              <span class="nav-label">${escapeHtml(item.label)}</span>
              $badgeHtml
            </a>"""
        }
        val flashHtml = when {
            flash == null -> ""
            flashError -> """<div class="alert alert-error">${escapeHtml(flash)}</div>"""
            else -> """<div class="alert alert-success">${escapeHtml(flash)}</div>"""
        }
        val pageIcon = iconFor(activeNav)
        return """
            <!DOCTYPE html>
            <html lang="ru">
            <head>
              <meta charset="utf-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1"/>
              <title>${escapeHtml(title)} — Famly Admin</title>
              <link rel="stylesheet" href="/admin/static/admin.css"/>
            </head>
            <body>
              <div class="admin-shell" id="adminShell">
                <aside class="admin-sidebar" id="sidebar">
                  <div class="sidebar-brand">
                    <img class="sidebar-logo" src="/admin/static/logo.png" alt="Famly"/>
                  </div>
                  <nav class="sidebar-nav">$navHtml</nav>
                  <div class="sidebar-footer">
                    <button type="button" class="sidebar-action" id="sidebarCollapse" title="Свернуть">
                      <span class="nav-icon">$iconCollapse</span>
                      <span class="sidebar-action-label nav-label">Свернуть</span>
                    </button>
                    <form class="sidebar-logout-form" method="post" action="/admin/logout">
                      <input type="hidden" name="csrf" value="${escapeHtml(csrfToken)}"/>
                      <button type="submit" class="sidebar-logout-btn" title="Выйти">
                        <span class="nav-icon">$iconLogout</span>
                        <span class="sidebar-action-label nav-label">Выйти</span>
                      </button>
                    </form>
                  </div>
                </aside>
                <div class="admin-main">
                  <header class="page-header">
                    <h1 class="page-title">
                      <span class="page-title-icon">$pageIcon</span>
                      ${escapeHtml(title)}
                    </h1>
                  </header>
                  <main class="admin-content">
                    $flashHtml
                    $content
                  </main>
                </div>
              </div>
              $sidebarScript
            </body>
            </html>
        """.trimIndent()
    }

    fun dashboard(
        adminEmail: String,
        csrfToken: String,
        stats: AdminStatsResponse,
        chart: List<RegistrationsDayDto>,
        navCounts: NavCounts,
    ): String {
        val maxBar = (chart.maxOfOrNull { it.count } ?: 1).coerceAtLeast(1)
        val bars = chart.joinToString("") { day ->
            val h = ((day.count.toFloat() / maxBar) * 100).toInt().coerceAtLeast(if (day.count > 0) 8 else 4)
            val label = formatChartDay(day.day)
            """<div class="chart-bar" style="height:${h}%" title="${day.count}"><span>$label</span></div>"""
        }
        val content = """
            ${if (!FamlyConfig.monetizationEnabled) """<div class="note">Монетизация отключена — весь функционал бесплатный для пользователей.</div>""" else ""}
            <div class="stat-grid">
              ${statCard(iconUsers, stats.usersCount.toString(), "Пользователей", "users")}
              ${statCard(iconHouseholds, stats.householdsCount.toString(), "Семей", "households")}
              ${statCard(iconSync, stats.syncEvents24h.toString(), "Sync за 24ч", "sync")}
              ${if (FamlyConfig.monetizationEnabled) statCard(iconShield, stats.activeSubscriptions.toString(), "Активных подписок", "subs") else ""}
            </div>
            <div class="panel-card">
              <div class="panel-card-header">
                <span class="panel-card-icon">$iconChart</span>
                <h2>Регистрации за 7 дней</h2>
              </div>
              <div class="chart-bars">$bars</div>
            </div>
        """.trimIndent()
        return layout("Обзор", "dashboard", adminEmail, csrfToken, content, navCounts)
    }

    fun usersPage(
        adminEmail: String,
        csrfToken: String,
        users: List<AdminUserDto>,
        page: Int,
        total: Int,
        pageSize: Int,
        query: String?,
        navCounts: NavCounts,
        flash: String? = null,
        flashError: String? = null,
    ): String {
        val rows = if (users.isEmpty()) {
            """<tr><td colspan="6" class="empty-row">Нет пользователей</td></tr>"""
        } else {
            users.joinToString("") { u ->
                val adminBadge = if (u.isAdmin) """ <span class="badge badge-admin">admin</span>""" else ""
                """<tr>
                  <td>${escapeHtml(u.email)}$adminBadge</td>
                  <td>${escapeHtml(u.displayName)}</td>
                  <td>${formatDay(u.createdAt)}</td>
                  <td class="mono">${u.householdId?.let { escapeHtml(it.take(8) + "…") } ?: "—"}</td>
                  <td class="mono">${escapeHtml(u.id.take(8))}…</td>
                  <td class="actions-cell">
                    <a href="/admin/users/${escapeHtml(u.id)}/edit" class="btn-icon-action" title="Редактировать">$iconEdit</a>
                    <form method="post" action="/admin/users/${escapeHtml(u.id)}/delete" class="inline-form" onsubmit="return confirm('Удалить пользователя ${escapeHtml(u.email)}?')">
                      <input type="hidden" name="csrf" value="${escapeHtml(csrfToken)}"/>
                      <button type="submit" class="btn-icon-action btn-icon-danger" title="Удалить">$iconTrash</button>
                    </form>
                  </td>
                </tr>"""
            }
        }
        val content = """
            <div class="panel-card form-panel">
              <div class="panel-card-header">
                <span class="panel-card-icon">$iconPlus</span>
                <h2>Добавить пользователя</h2>
              </div>
              <form method="post" action="/admin/users/create" class="user-form">
                <input type="hidden" name="csrf" value="${escapeHtml(csrfToken)}"/>
                <div class="form-row">
                  <label>Email<input type="email" name="email" required/></label>
                  <label>Имя<input type="text" name="displayName" required/></label>
                  <label>Пароль<input type="password" name="password" required minlength="6"/></label>
                  <label class="checkbox-label"><input type="checkbox" name="isAdmin"/> Админ</label>
                </div>
                <button type="submit" class="btn btn-primary"><span class="btn-icon">$iconPlus</span> Создать</button>
              </form>
            </div>
            ${searchForm("/admin/users", "Поиск по email", query)}
            <div class="table-wrap">
              <table class="data-table">
                <thead><tr>
                  ${th(iconMail, "Email")}
                  ${th(iconUser, "Имя")}
                  ${th(iconCalendar, "Регистрация")}
                  ${th(iconHouseholds, "Семья")}
                  ${th(iconHash, "ID")}
                  ${th(iconEdit, "Действия")}
                </tr></thead>
                <tbody>$rows</tbody>
              </table>
            </div>
            ${paginationBlock("/admin/users", page, total, pageSize, extraQuery = query?.let { "q=${escapeHtml(it)}" })}
        """.trimIndent()
        return layout(
            "Пользователи", "users", adminEmail, csrfToken, content, navCounts,
            flash = flash ?: flashError,
            flashError = flashError != null,
        )
    }

    fun userEditPage(
        adminEmail: String,
        csrfToken: String,
        user: AdminUserDto,
        navCounts: NavCounts,
        flashError: String? = null,
    ): String {
        val content = """
            <p class="back-link"><a href="/admin/users">← К списку пользователей</a></p>
            <div class="panel-card form-panel">
              <div class="panel-card-header">
                <span class="panel-card-icon">$iconEdit</span>
                <h2>Редактировать: ${escapeHtml(user.email)}</h2>
              </div>
              <form method="post" action="/admin/users/${escapeHtml(user.id)}/update" class="user-form">
                <input type="hidden" name="csrf" value="${escapeHtml(csrfToken)}"/>
                <div class="form-row">
                  <label>Email<input type="email" name="email" value="${escapeHtml(user.email)}" required/></label>
                  <label>Имя<input type="text" name="displayName" value="${escapeHtml(user.displayName)}" required/></label>
                  <label>Новый пароль <span class="hint">(оставьте пустым, чтобы не менять)</span><input type="password" name="password" minlength="6"/></label>
                  <label class="checkbox-label"><input type="checkbox" name="isAdmin"${if (user.isAdmin) " checked" else ""}/> Админ</label>
                </div>
                <button type="submit" class="btn btn-primary"><span class="btn-icon">$iconEdit</span> Сохранить</button>
              </form>
            </div>
        """.trimIndent()
        return layout(
            "Редактирование", "users", adminEmail, csrfToken, content, navCounts,
            flash = flashError, flashError = flashError != null,
        )
    }

    fun householdsPage(
        adminEmail: String,
        csrfToken: String,
        households: List<AdminHouseholdListDto>,
        page: Int,
        total: Int,
        pageSize: Int,
        query: String?,
        navCounts: NavCounts,
    ): String {
        val rows = households.joinToString("") { h ->
            """<tr>
              <td><a href="/admin/households/${escapeHtml(h.id)}">${escapeHtml(h.name)}</a></td>
              <td>${h.memberCount}</td>
              <td class="mono">${escapeHtml(h.inviteCode)}</td>
              <td>${formatDay(h.createdAt)}</td>
              <td>
                <a class="btn btn-ghost btn-sm" href="/admin/households/${escapeHtml(h.id)}">Открыть</a>
              </td>
            </tr>"""
        }
        val content = """
            ${searchForm("/admin/households", "Поиск по названию или invite-коду", query)}
            <div class="table-wrap">
              <table class="data-table">
                <thead><tr>
                  ${th(iconHouseholds, "Название")}
                  ${th(iconUsers, "Участники")}
                  ${th(iconLink, "Invite")}
                  ${th(iconCalendar, "Создана")}
                  ${th(iconEdit, "")}
                </tr></thead>
                <tbody>${rows.ifBlank { """<tr><td colspan="5" class="empty-row">Нет семей</td></tr>""" }}</tbody>
              </table>
            </div>
            ${paginationBlock("/admin/households", page, total, pageSize, extraQuery = query?.let { "q=${escapeHtml(it)}" })}
        """.trimIndent()
        return layout("Семьи", "households", adminEmail, csrfToken, content, navCounts)
    }

    fun householdDetailPage(
        adminEmail: String,
        csrfToken: String,
        household: AdminHouseholdDto,
        transactions: List<AdminSyncLogSummaryDto>,
        flash: String? = null,
        navCounts: NavCounts = NavCounts(0, 0),
    ): String {
        val members = household.members.joinToString("") { m ->
            """<tr>
              <td>${escapeHtml(m.displayName)}</td>
              <td>${escapeHtml(m.role)}</td>
              <td>${escapeHtml(m.visibility)}</td>
              <td class="mono">${escapeHtml(m.userId.take(8))}…</td>
            </tr>"""
        }
        val txRows = transactions.joinToString("") { t ->
            """<tr>
              <td>${formatTs(t.updatedAt)}</td>
              <td class="mono">${escapeHtml(t.entityId.take(12))}…</td>
              <td>${if (t.deleted) "удалено" else "активно"}</td>
              <td><a href="/admin/sync/${escapeHtml(t.id)}">JSON</a></td>
            </tr>"""
        }
        val inviteUrl = "${FamlyConfig.publicBaseUrl}/join?code=${household.inviteCode}"
        val content = """
            <p class="back-link"><a href="/admin/households">← К списку семей</a></p>
            <div class="panel-card" style="margin-bottom:20px">
              <h2 class="panel-subtitle">${escapeHtml(household.name)}</h2>
              <p class="mono">ID: ${escapeHtml(household.id)}</p>
              <p>Invite: <strong>${escapeHtml(household.inviteCode)}</strong></p>
              <p class="mono">${escapeHtml(inviteUrl)}</p>
              <form method="post" action="/admin/households/${escapeHtml(household.id)}/regenerate-invite" style="margin-top:12px">
                <input type="hidden" name="csrf" value="${escapeHtml(csrfToken)}"/>
                <button type="submit" class="btn btn-primary btn-sm">Перевыпустить invite</button>
              </form>
            </div>
            <div class="table-wrap" style="margin-bottom:24px">
              <table class="data-table">
                <thead><tr>${th(iconUser, "Имя")}${th(iconShield, "Роль")}${th(iconTag, "Видимость")}${th(iconHash, "User ID")}</tr></thead>
                <tbody>$members</tbody>
              </table>
            </div>
            <div class="table-wrap">
              <table class="data-table">
                <thead><tr>${th(iconCalendar, "Дата")}${th(iconHash, "ID")}${th(iconTag, "Статус")}${th(iconLink, "")}</tr></thead>
                <tbody>${txRows.ifBlank { """<tr><td colspan="4" class="empty-row">Нет операций</td></tr>""" }}</tbody>
              </table>
            </div>
        """.trimIndent()
        return layout("Семья: ${household.name}", "households", adminEmail, csrfToken, content, navCounts, flash)
    }

    fun syncPage(
        adminEmail: String,
        csrfToken: String,
        entries: List<AdminSyncLogSummaryDto>,
        page: Int,
        total: Int,
        pageSize: Int,
        entityType: String?,
        navCounts: NavCounts,
    ): String {
        val rows = entries.joinToString("") { e ->
            """<tr>
              <td>${formatTs(e.updatedAt)}</td>
              <td>${escapeHtml(e.entityType)}</td>
              <td class="mono">${escapeHtml(e.entityId.take(16))}</td>
              <td class="mono">${escapeHtml(e.householdId.take(8))}…</td>
              <td>${if (e.deleted) "да" else ""}</td>
              <td><a href="/admin/sync/${escapeHtml(e.id)}">Детали</a></td>
            </tr>"""
        }
        val filterFields = """
          <select name="type">
            <option value="">Все типы</option>
            <option value="transaction" ${if (entityType == "transaction") "selected" else ""}>transaction</option>
            <option value="category" ${if (entityType == "category") "selected" else ""}>category</option>
            <option value="account" ${if (entityType == "account") "selected" else ""}>account</option>
          </select>
        """.trimIndent()
        val content = """
            <form class="search-form" method="get" action="/admin/sync">
              $filterFields
              <button type="submit" class="btn btn-primary"><span class="btn-icon">$iconSearch</span> Фильтр</button>
            </form>
            <div class="table-wrap">
              <table class="data-table">
                <thead><tr>
                  ${th(iconClock, "Время")}
                  ${th(iconTag, "Тип")}
                  ${th(iconHash, "Entity")}
                  ${th(iconHouseholds, "Семья")}
                  ${th(iconTrash, "Del")}
                  ${th(iconLink, "")}
                </tr></thead>
                <tbody>${rows.ifBlank { """<tr><td colspan="6" class="empty-row">Пусто</td></tr>""" }}</tbody>
              </table>
            </div>
            ${paginationBlock("/admin/sync", page, total, pageSize, extraQuery = entityType?.let { "type=${escapeHtml(it)}" })}
        """.trimIndent()
        return layout("Sync log", "sync", adminEmail, csrfToken, content, navCounts)
    }

    fun syncDetailPage(
        adminEmail: String,
        csrfToken: String,
        id: String,
        payload: String,
        meta: AdminSyncLogSummaryDto,
        navCounts: NavCounts = NavCounts(0, 0),
    ): String {
        val content = """
            <p class="back-link"><a href="/admin/sync">← К sync log</a></p>
            <div class="panel-card">
              <p><strong>Тип:</strong> ${escapeHtml(meta.entityType)}</p>
              <p><strong>Entity:</strong> <span class="mono">${escapeHtml(meta.entityId)}</span></p>
              <p><strong>Семья:</strong> <span class="mono">${escapeHtml(meta.householdId)}</span></p>
              <p><strong>Время:</strong> ${formatTs(meta.updatedAt)}</p>
              <pre class="mono payload-pre">${escapeHtml(payload)}</pre>
            </div>
        """.trimIndent()
        return layout("Sync: $id", "sync", adminEmail, csrfToken, content, navCounts)
    }

    fun auditPage(
        adminEmail: String,
        csrfToken: String,
        entries: List<AdminAuditDto>,
        page: Int,
        total: Int,
        pageSize: Int,
        navCounts: NavCounts,
    ): String {
        val rows = entries.joinToString("") { a ->
            """<tr>
              <td>${formatTs(a.createdAt)}</td>
              <td>${escapeHtml(a.adminEmail ?: a.adminUserId.take(8))}</td>
              <td>${escapeHtml(a.action)}</td>
              <td>${a.targetType?.let { escapeHtml(it) } ?: "—"}</td>
              <td class="mono">${a.targetId?.let { escapeHtml(it.take(12) + "…") } ?: "—"}</td>
              <td>${a.details?.let { escapeHtml(it) } ?: ""}</td>
            </tr>"""
        }
        val content = """
            <div class="table-wrap">
              <table class="data-table">
                <thead><tr>
                  ${th(iconClock, "Время")}
                  ${th(iconUser, "Админ")}
                  ${th(iconActivity, "Действие")}
                  ${th(iconTag, "Объект")}
                  ${th(iconHash, "ID")}
                  ${th(iconList, "Детали")}
                </tr></thead>
                <tbody>${rows.ifBlank { """<tr><td colspan="6" class="empty-row">Пусто</td></tr>""" }}</tbody>
              </table>
            </div>
            ${paginationBlock("/admin/audit", page, total, pageSize)}
        """.trimIndent()
        return layout("Аудит", "audit", adminEmail, csrfToken, content, navCounts)
    }

    fun healthPage(
        adminEmail: String,
        csrfToken: String,
        serviceVersion: String,
        uptimeMs: Long,
        stats: AdminStatsResponse,
        navCounts: NavCounts,
    ): String {
        val uptimeMin = uptimeMs / 60_000
        val uptimeHours = uptimeMin / 60
        val uptimeDisplay = if (uptimeHours > 0) "${uptimeHours}ч ${uptimeMin % 60}м" else "${uptimeMin}м"
        val content = """
            <div class="stat-grid">
              ${statCard(iconGlobe, serviceVersion, "Версия backend", "version")}
              ${statCard(iconClock, uptimeDisplay, "Uptime JVM", "uptime")}
              ${statCard(iconUsers, stats.usersCount.toString(), "Пользователей", "users")}
              ${statCard(iconHouseholds, stats.householdsCount.toString(), "Семей", "households")}
            </div>
            <div class="panel-card">
              <div class="panel-card-header">
                <span class="panel-card-icon">$iconHealth</span>
                <h2>Конфигурация</h2>
              </div>
              <p>API: <span class="mono">${escapeHtml(FamlyConfig.publicBaseUrl)}</span></p>
              <p>Монетизация: ${if (FamlyConfig.monetizationEnabled) "включена" else "выключена"}</p>
            </div>
        """.trimIndent()
        return layout("Сервер", "health", adminEmail, csrfToken, content, navCounts)
    }
}
