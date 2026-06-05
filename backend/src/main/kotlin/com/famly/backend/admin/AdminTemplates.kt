package com.famly.backend.admin

import com.famly.backend.FamlyConfig
import com.famly.backend.models.AdminAuditDto
import com.famly.backend.models.AdminHouseholdDto
import com.famly.backend.models.AdminHouseholdListDto
import com.famly.backend.models.AdminStatsResponse
import com.famly.backend.models.AdminSyncLogSummaryDto
import com.famly.backend.models.AdminUserDto
import com.famly.backend.models.RegistrationsDayDto
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object AdminTemplates {
    private val dateTimeFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        .withZone(ZoneId.systemDefault())
    private val dateFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        .withZone(ZoneId.systemDefault())

    fun escapeHtml(raw: String): String = raw
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    fun formatTs(ms: Long): String = dateTimeFmt.format(Instant.ofEpochMilli(ms))

    fun formatDay(ms: Long): String = dateFmt.format(Instant.ofEpochMilli(ms))

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
              <h1>Famly Admin</h1>
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
        flash: String? = null,
        flashError: Boolean = false,
    ): String {
        val nav = listOf(
            "dashboard" to ("Обзор" to "/admin/dashboard"),
            "users" to ("Пользователи" to "/admin/users"),
            "households" to ("Семьи" to "/admin/households"),
            "sync" to ("Sync log" to "/admin/sync"),
            "audit" to ("Аудит" to "/admin/audit"),
            "health" to ("Сервер" to "/admin/health"),
        )
        val navHtml = nav.joinToString("\n") { (id, pair) ->
            val cls = if (id == activeNav) "active" else ""
            """<a class="$cls" href="${pair.second}">${pair.first}</a>"""
        }
        val flashHtml = when {
            flash == null -> ""
            flashError -> """<div class="alert alert-error">${escapeHtml(flash)}</div>"""
            else -> """<div class="alert alert-success">${escapeHtml(flash)}</div>"""
        }
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
              <div class="admin-shell">
                <aside class="admin-sidebar">
                  <div class="brand">Famly Admin</div>
                  <nav>$navHtml</nav>
                </aside>
                <div class="admin-main">
                  <header class="admin-topbar">
                    <strong>${escapeHtml(title)}</strong>
                    <div style="display:flex;align-items:center;gap:12px">
                      <span class="email">${escapeHtml(adminEmail)}</span>
                      <form method="post" action="/admin/logout" style="margin:0">
                        <input type="hidden" name="csrf" value="${escapeHtml(csrfToken)}"/>
                        <button type="submit" class="btn btn-ghost btn-sm">Выйти</button>
                      </form>
                    </div>
                  </header>
                  <main class="admin-content">
                    $flashHtml
                    $content
                  </main>
                </div>
              </div>
            </body>
            </html>
        """.trimIndent()
    }

    fun dashboard(
        adminEmail: String,
        csrfToken: String,
        stats: AdminStatsResponse,
        chart: List<RegistrationsDayDto>,
    ): String {
        val maxBar = (chart.maxOfOrNull { it.count } ?: 1).coerceAtLeast(1)
        val bars = chart.joinToString("") { day ->
            val h = ((day.count.toFloat() / maxBar) * 100).toInt().coerceAtLeast(4)
            val label = day.day.substring(5)
            """<div class="chart-bar" style="height:${h}%" title="${day.count}"><span>$label</span></div>"""
        }
        val content = """
            ${if (!FamlyConfig.monetizationEnabled) """<div class="note">Монетизация отключена — весь функционал бесплатный для пользователей.</div>""" else ""}
            <div class="grid">
              <div class="card"><h2>${stats.usersCount}</h2><p>Пользователей</p></div>
              <div class="card"><h2>${stats.householdsCount}</h2><p>Семей</p></div>
              <div class="card"><h2>${stats.syncEvents24h}</h2><p>Sync за 24ч</p></div>
              ${if (FamlyConfig.monetizationEnabled) """<div class="card"><h2>${stats.activeSubscriptions}</h2><p>Активных подписок</p></div>""" else ""}
            </div>
            <div class="card">
              <p><strong>Регистрации за 7 дней</strong></p>
              <div class="chart-bars" style="padding-bottom:28px">$bars</div>
            </div>
        """.trimIndent()
        return layout("Обзор", "dashboard", adminEmail, csrfToken, content)
    }

    fun usersPage(
        adminEmail: String,
        csrfToken: String,
        users: List<AdminUserDto>,
        page: Int,
        total: Int,
        pageSize: Int,
        query: String?,
    ): String {
        val rows = if (users.isEmpty()) {
            """<tr><td colspan="5">Нет пользователей</td></tr>"""
        } else {
            users.joinToString("") { u ->
                val adminBadge = if (u.isAdmin) """ <span class="badge badge-admin">admin</span>""" else ""
                """<tr>
                  <td>${escapeHtml(u.email)}$adminBadge</td>
                  <td>${escapeHtml(u.displayName)}</td>
                  <td>${formatDay(u.createdAt)}</td>
                  <td class="mono">${u.householdId?.let { escapeHtml(it.take(8) + "…") } ?: "—"}</td>
                  <td class="mono">${escapeHtml(u.id.take(8))}…</td>
                </tr>"""
            }
        }
        val qParam = query?.let { "&q=${escapeHtml(it)}" } ?: ""
        val content = """
            <form class="search-form" method="get" action="/admin/users">
              <input type="text" name="q" placeholder="Поиск по email" value="${query?.let { escapeHtml(it) } ?: ""}"/>
              <button type="submit" class="btn btn-primary">Найти</button>
            </form>
            <div class="table-wrap">
              <table>
                <thead><tr><th>Email</th><th>Имя</th><th>Регистрация</th><th>Семья</th><th>ID</th></tr></thead>
                <tbody>$rows</tbody>
              </table>
            </div>
            ${pagination("/admin/users", page, total, pageSize, extraQuery = query?.let { "q=${escapeHtml(it)}" })}
        """.trimIndent()
        return layout("Пользователи", "users", adminEmail, csrfToken, content)
    }

    fun householdsPage(
        adminEmail: String,
        csrfToken: String,
        households: List<AdminHouseholdListDto>,
        page: Int,
        total: Int,
        pageSize: Int,
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
            <div class="table-wrap">
              <table>
                <thead><tr><th>Название</th><th>Участники</th><th>Invite</th><th>Создана</th><th></th></tr></thead>
                <tbody>${rows.ifBlank { """<tr><td colspan="5">Нет семей</td></tr>""" }}</tbody>
              </table>
            </div>
            ${pagination("/admin/households", page, total, pageSize)}
        """.trimIndent()
        return layout("Семьи", "households", adminEmail, csrfToken, content)
    }

    fun householdDetailPage(
        adminEmail: String,
        csrfToken: String,
        household: AdminHouseholdDto,
        transactions: List<AdminSyncLogSummaryDto>,
        flash: String? = null,
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
            <p><a href="/admin/households">← К списку семей</a></p>
            <div class="card" style="margin-bottom:20px">
              <h2 style="font-size:20px;margin-bottom:8px">${escapeHtml(household.name)}</h2>
              <p class="mono">ID: ${escapeHtml(household.id)}</p>
              <p>Invite: <strong>${escapeHtml(household.inviteCode)}</strong></p>
              <p class="mono">${escapeHtml(inviteUrl)}</p>
              <form method="post" action="/admin/households/${escapeHtml(household.id)}/regenerate-invite" style="margin-top:12px">
                <input type="hidden" name="csrf" value="${escapeHtml(csrfToken)}"/>
                <button type="submit" class="btn btn-primary btn-sm">Перевыпустить invite</button>
              </form>
            </div>
            <h2 style="font-size:18px">Участники</h2>
            <div class="table-wrap" style="margin-bottom:24px">
              <table>
                <thead><tr><th>Имя</th><th>Роль</th><th>Видимость</th><th>User ID</th></tr></thead>
                <tbody>$members</tbody>
              </table>
            </div>
            <h2 style="font-size:18px">Последние операции (20)</h2>
            <div class="table-wrap">
              <table>
                <thead><tr><th>Дата</th><th>ID</th><th>Статус</th><th></th></tr></thead>
                <tbody>${txRows.ifBlank { """<tr><td colspan="4">Нет операций</td></tr>""" }}</tbody>
              </table>
            </div>
        """.trimIndent()
        return layout("Семья: ${household.name}", "households", adminEmail, csrfToken, content, flash)
    }

    fun syncPage(
        adminEmail: String,
        csrfToken: String,
        entries: List<AdminSyncLogSummaryDto>,
        page: Int,
        total: Int,
        pageSize: Int,
        entityType: String?,
    ): String {
        val typeParam = entityType?.let { "&type=${escapeHtml(it)}" } ?: ""
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
        val content = """
            <form class="search-form" method="get" action="/admin/sync">
              <select name="type">
                <option value="">Все типы</option>
                <option value="transaction" ${if (entityType == "transaction") "selected" else ""}>transaction</option>
                <option value="category" ${if (entityType == "category") "selected" else ""}>category</option>
                <option value="account" ${if (entityType == "account") "selected" else ""}>account</option>
              </select>
              <button type="submit" class="btn btn-primary">Фильтр</button>
            </form>
            <div class="table-wrap">
              <table>
                <thead><tr><th>Время</th><th>Тип</th><th>Entity</th><th>Семья</th><th>Del</th><th></th></tr></thead>
                <tbody>${rows.ifBlank { """<tr><td colspan="6">Пусто</td></tr>""" }}</tbody>
              </table>
            </div>
            ${pagination("/admin/sync", page, total, pageSize, extraQuery = entityType?.let { "type=${escapeHtml(it)}" })}
        """.trimIndent()
        return layout("Sync log", "sync", adminEmail, csrfToken, content)
    }

    fun syncDetailPage(adminEmail: String, csrfToken: String, id: String, payload: String, meta: AdminSyncLogSummaryDto): String {
        val content = """
            <p><a href="/admin/sync">← К sync log</a></p>
            <div class="card">
              <p><strong>Тип:</strong> ${escapeHtml(meta.entityType)}</p>
              <p><strong>Entity:</strong> <span class="mono">${escapeHtml(meta.entityId)}</span></p>
              <p><strong>Семья:</strong> <span class="mono">${escapeHtml(meta.householdId)}</span></p>
              <p><strong>Время:</strong> ${formatTs(meta.updatedAt)}</p>
              <pre class="mono" style="white-space:pre-wrap;background:var(--surface-alt);padding:12px;border-radius:8px;max-height:400px;overflow:auto">${escapeHtml(payload)}</pre>
            </div>
        """.trimIndent()
        return layout("Sync: $id", "sync", adminEmail, csrfToken, content)
    }

    fun auditPage(
        adminEmail: String,
        csrfToken: String,
        entries: List<AdminAuditDto>,
        page: Int,
        total: Int,
        pageSize: Int,
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
              <table>
                <thead><tr><th>Время</th><th>Админ</th><th>Действие</th><th>Объект</th><th>ID</th><th>Детали</th></tr></thead>
                <tbody>${rows.ifBlank { """<tr><td colspan="6">Пусто</td></tr>""" }}</tbody>
              </table>
            </div>
            ${pagination("/admin/audit", page, total, pageSize)}
        """.trimIndent()
        return layout("Аудит", "audit", adminEmail, csrfToken, content)
    }

    fun healthPage(
        adminEmail: String,
        csrfToken: String,
        serviceVersion: String,
        uptimeMs: Long,
        stats: AdminStatsResponse,
    ): String {
        val uptimeMin = uptimeMs / 60_000
        val content = """
            <div class="grid">
              <div class="card"><h2>${escapeHtml(serviceVersion)}</h2><p>Версия backend</p></div>
              <div class="card"><h2>${uptimeMin}</h2><p>Минут uptime JVM</p></div>
              <div class="card"><h2>${stats.usersCount}</h2><p>Пользователей (кэш)</p></div>
              <div class="card"><h2>${stats.householdsCount}</h2><p>Семей (кэш)</p></div>
            </div>
            <div class="card">
              <p>API: <span class="mono">${escapeHtml(FamlyConfig.publicBaseUrl)}</span></p>
              <p>Монетизация: ${if (FamlyConfig.monetizationEnabled) "включена" else "выключена"}</p>
            </div>
        """.trimIndent()
        return layout("Сервер", "health", adminEmail, csrfToken, content)
    }

    private fun pagination(
        basePath: String,
        page: Int,
        total: Int,
        pageSize: Int,
        extraQuery: String? = null,
    ): String {
        val totalPages = ((total + pageSize - 1) / pageSize).coerceAtLeast(1)
        if (totalPages <= 1) return """<p class="pagination">Всего: $total</p>"""
        val extra = extraQuery?.let { "&$it" } ?: ""
        val prev = if (page > 1) """<a href="$basePath?page=${page - 1}$extra">←</a>""" else ""
        val next = if (page < totalPages) """<a href="$basePath?page=${page + 1}$extra">→</a>""" else ""
        return """<div class="pagination">$prev <span class="current">$page / $totalPages</span> $next · всего $total</div>"""
    }
}
