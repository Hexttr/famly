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

/** Space for floating bottom bar + raised FAB + grounding gradient. */
object LayoutInsets {
    /** FAB protrudes above the nav capsule; must be included in bottomBar slot height. */
    val fabOverhang = 32.dp
    val bottomNavBarBody = 104.dp
    val bottomNavHeight = fabOverhang + bottomNavBarBody
    val scrollBottomClearance = 32.dp
    /** Extra scroll padding on main tabs (NavHost already reserves bottomBar height). */
    val mainTabScrollBottom = scrollBottomClearance
    /** Nested screens (ScreenScaffold): FAB can overlap content if slot height is underestimated. */
    val stackedScreenScrollBottom = fabOverhang + scrollBottomClearance + 16.dp
}
