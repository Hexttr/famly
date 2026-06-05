package com.famly.app.domain

import com.famly.app.BuildConfig

object InviteLinks {
    private val apiBase: String
        get() = BuildConfig.API_BASE_URL.trimEnd('/')

    fun httpsJoinUrl(code: String): String = "$apiBase/join?code=${code.trim()}"

    fun deepLink(code: String): String = "famly://join?code=${code.trim()}"

    /** QR scanners reliably open HTTPS URLs; custom schemes often fail from the camera app. */
    fun qrPayload(code: String, serverInviteUrl: String? = null): String {
        val fromServer = serverInviteUrl?.trim()?.takeIf { url ->
            url.startsWith("http://") || url.startsWith("https://")
        }
        if (fromServer != null && !fromServer.contains("famly.app")) {
            return fromServer
        }
        return httpsJoinUrl(code)
    }

    fun shareText(code: String, familyName: String?, serverInviteUrl: String? = null): String {
        val link = qrPayload(code, serverInviteUrl)
        return buildString {
            append("Присоединяйся к семье «${familyName ?: "наша семья"}» в Мой (Наш) Бюджет!\n")
            append("Код: $code\n")
            append("Ссылка: $link")
        }
    }
}
