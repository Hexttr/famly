package com.famly.app.ui.theme

import androidx.compose.ui.unit.dp

object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}

object Radius {
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
}

object HeaderLayout {
    val inset = 8.dp
    val logoSize = 88.dp
    val sideWidth = 52.dp
    val buttonSize = 48.dp
    val height = 112.dp
}

/** Extra space so scrollable content is not hidden under the bottom tab bar or system nav. */
object LayoutInsets {
    val bottomNavHeight = 64.dp
    val scrollBottomClearance = 32.dp
    val mainTabScrollBottom = bottomNavHeight + scrollBottomClearance
}
