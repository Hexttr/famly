package com.famly.app.domain

val MEMBER_AVATARS = listOf("👨", "👩", "👦", "👧", "🧑", "👴", "👵", "🧔", "👱", "🧒")

const val DEFAULT_MEMBER_AVATAR = "🧑"

fun nextMemberAvatar(current: String): String {
    val idx = MEMBER_AVATARS.indexOf(current)
    if (idx == -1) return DEFAULT_MEMBER_AVATAR
    return MEMBER_AVATARS[(idx + 1) % MEMBER_AVATARS.size]
}
