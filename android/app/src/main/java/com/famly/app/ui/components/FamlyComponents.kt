package com.famly.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.famly.app.R
import com.famly.app.ui.theme.Accent
import com.famly.app.ui.theme.Border
import com.famly.app.ui.theme.Expense
import com.famly.app.ui.theme.HeaderLayout
import com.famly.app.ui.theme.Premium
import com.famly.app.ui.theme.PremiumBg
import com.famly.app.ui.theme.Primary
import com.famly.app.ui.theme.BottomNavBackground
import com.famly.app.ui.theme.PrimaryDark
import com.famly.app.ui.theme.PrimaryLight
import com.famly.app.ui.theme.Radius
import com.famly.app.ui.theme.Spacing
import com.famly.app.ui.theme.TextMuted
import com.famly.app.ui.theme.TextSecondary
import com.famly.app.ui.theme.famlyCardShadow
import com.famly.app.ui.theme.famlyFabShadow
import com.famly.app.ui.theme.famlyFloatingNavShadow
import com.famly.app.ui.theme.famlyHeaderButtonShadow
import com.famly.app.ui.theme.famlyHeroShadow
import com.famly.app.ui.theme.famlySmShadow
import com.famly.app.ui.theme.parseHexColor

enum class HeaderLeftSlot { Notifications, None }

enum class HeaderRightSlot { QuickAdd, Settings, Add, None }

@Composable
fun AppHeader(
    modifier: Modifier = Modifier,
    showBack: Boolean = false,
    onBack: (() -> Unit)? = null,
    leftSlot: HeaderLeftSlot = HeaderLeftSlot.None,
    rightSlot: HeaderRightSlot = HeaderRightSlot.None,
    onQuickAdd: () -> Unit = {},
    onSettings: () -> Unit = {},
    onAdd: () -> Unit = {},
    onHome: () -> Unit = {},
    onNotifications: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(HeaderLayout.height)
            .background(MaterialTheme.colorScheme.background),
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "Мой (Наш) Бюджет",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(2f)
                .padding(top = HeaderLayout.inset)
                .size(HeaderLayout.logoSize)
                .clickable(onClick = onHome),
            contentScale = ContentScale.Fit,
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = HeaderLayout.inset + HeaderLayout.logoSize / 2 - HeaderLayout.buttonSize / 2)
                .padding(start = 28.dp),
        ) {
            when {
                showBack && onBack != null -> HeaderCircleButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    onClick = onBack,
                    tint = PrimaryDark,
                )
                leftSlot == HeaderLeftSlot.Notifications -> HeaderNotificationButton(onClick = onNotifications)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = HeaderLayout.inset + HeaderLayout.logoSize / 2 - HeaderLayout.buttonSize / 2)
                .padding(end = 28.dp),
        ) {
            when (rightSlot) {
                HeaderRightSlot.QuickAdd -> HeaderCircleButton(
                    icon = Icons.Default.Add,
                    contentDescription = "Добавить операцию",
                    onClick = onQuickAdd,
                    primary = true,
                )
                HeaderRightSlot.Settings -> HeaderCircleButton(
                    icon = Icons.Default.Settings,
                    contentDescription = "Настройки",
                    onClick = onSettings,
                    tint = PrimaryDark,
                )
                HeaderRightSlot.Add -> HeaderCircleButton(
                    icon = Icons.Default.Add,
                    contentDescription = "Добавить",
                    onClick = onAdd,
                    tint = PrimaryDark,
                )
                HeaderRightSlot.None -> Unit
            }
        }
    }
}

@Composable
private fun HeaderCircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    primary: Boolean = false,
    tint: Color = Color.White,
) {
    Box(
        modifier = Modifier
            .size(HeaderLayout.buttonSize)
            .famlyHeaderButtonShadow(primary = primary)
            .clip(CircleShape)
            .background(if (primary) Primary else MaterialTheme.colorScheme.surface)
            .then(
                if (!primary) {
                    Modifier.border(2.dp, Primary.copy(alpha = 0.31f), CircleShape)
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun HeaderNotificationButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(HeaderLayout.buttonSize)
            .famlyHeaderButtonShadow()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(2.dp, Expense.copy(alpha = 0.31f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Default.Notifications, contentDescription = "Уведомления", tint = Expense, modifier = Modifier.size(22.dp))
    }
}

@Composable
fun FamlyBottomNav(
    selectedRoute: String,
    onTabSelected: (String) -> Unit,
    onQuickAdd: () -> Unit,
    tabs: List<Triple<String, ImageVector, String>>,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    val leftTabs = tabs.take(2)
    val rightTabs = tabs.drop(2)
    val capsuleShape = RoundedCornerShape(28.dp)
    val fabGradient = Brush.linearGradient(
        colors = listOf(PrimaryLight, Primary, PrimaryDark),
        start = Offset(0f, 0f),
        end = Offset(120f, 120f),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 16.dp, end = 16.dp, bottom = 10.dp, top = 4.dp)
            .height(72.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .famlyFloatingNavShadow(capsuleShape)
                .clip(capsuleShape)
                .background(BottomNavBackground)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leftTabs.forEach { (route, icon, label) ->
                FloatingNavTab(
                    icon = icon,
                    label = label,
                    selected = selectedRoute == route,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onTabSelected(route)
                    },
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.width(52.dp))
            rightTabs.forEach { (route, icon, label) ->
                FloatingNavTab(
                    icon = icon,
                    label = label,
                    selected = selectedRoute == route,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onTabSelected(route)
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-6).dp)
                .size(58.dp)
                .famlyFabShadow()
                .clip(CircleShape)
                .background(fabGradient)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        onQuickAdd()
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Новая операция",
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun FloatingNavTab(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 380f),
        label = "navTabScale",
    )
    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(if (selected) Color.White.copy(alpha = 0.2f) else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (selected) Color.White else Color.White.copy(alpha = 0.55f),
                modifier = Modifier.size(22.dp),
            )
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(Accent),
            )
        }
    }
}

@Composable
fun FamlyCard(
    modifier: Modifier = Modifier,
    borderColor: Color = Primary.copy(alpha = 0.27f),
    cornerRadius: Dp = Radius.lg,
    padding: Dp = Spacing.md,
    withShadow: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    Column(
        modifier = modifier
            .then(if (withShadow) Modifier.famlyCardShadow(shape) else Modifier)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(2.dp, borderColor, shape)
            .padding(padding),
        content = content,
    )
}

@Composable
fun AccentCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val shape = RoundedCornerShape(Radius.lg)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .famlyCardShadow(shape)
            .clip(shape)
            .background(Primary.copy(alpha = 0.08f))
            .border(2.dp, Primary.copy(alpha = 0.27f), shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(52.dp)
                .background(Primary),
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
fun AccentCardColumn(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(Radius.lg)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .famlyCardShadow(shape)
            .clip(shape)
            .background(Primary.copy(alpha = 0.08f))
            .border(2.dp, Primary.copy(alpha = 0.27f), shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(52.dp)
                .background(Primary),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content,
        )
    }
}

@Composable
fun HeroCard(
    modifier: Modifier = Modifier,
    gradientStart: Color = Primary,
    gradientEnd: Color = PrimaryDark,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(Radius.xl)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .famlyHeroShadow(shape)
            .clip(shape)
            .background(Brush.linearGradient(listOf(gradientStart, gradientEnd)))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = (-36).dp)
                .size(130.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f)),
        )
        content()
    }
}

@Composable
fun FamlyFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = Primary,
    leading: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .clip(shape)
            .then(if (!selected) Modifier.famlySmShadow(shape) else Modifier)
            .background(if (selected) accent else MaterialTheme.colorScheme.surface)
            .border(2.dp, if (selected) accent else Primary.copy(alpha = 0.27f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            CompositionLocalProvider(
                LocalContentColor provides if (selected) Color.White else TextSecondary,
            ) {
                leading()
            }
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun FamlyCategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    accent: Color = Primary,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .clip(shape)
            .then(if (!selected) Modifier.famlySmShadow(shape) else Modifier)
            .background(if (selected) accent.copy(alpha = 0.09f) else MaterialTheme.colorScheme.surface)
            .border(2.dp, if (selected) accent else Primary.copy(alpha = 0.27f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun CategoryEmojiIcon(
    emoji: String,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    accent: Color = Primary,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.09f))
            .border(2.dp, accent.copy(alpha = 0.33f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(emoji, fontSize = (size.value * 0.45).sp)
    }
}

@Composable
fun FamlySearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Radius.md)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .famlyCardShadow(shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(2.dp, Primary.copy(alpha = 0.27f), shape)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("🔍", color = TextMuted, modifier = Modifier.padding(end = 10.dp))
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(Primary),
            singleLine = true,
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(placeholder, color = TextMuted, style = MaterialTheme.typography.bodyLarge)
                }
                inner()
            },
        )
    }
}

@Composable
fun GroupedListCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(Radius.lg)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .famlyCardShadow(shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface),
        content = content,
    )
}

@Composable
fun QuickCategoryTile(
    emoji: String,
    name: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Radius.md)
    Column(
        modifier = modifier
            .clip(shape)
            .famlySmShadow(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(2.dp, Border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CategoryEmojiIcon(emoji = emoji, size = 38.dp, accent = accent)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            name,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun SectionHeading(icon: String, title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(bottom = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Primary.copy(alpha = 0.08f))
                .border(2.dp, Primary.copy(alpha = 0.27f), RoundedCornerShape(16.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            Text(icon, fontSize = 18.sp)
        }
        Text(
            title,
            modifier = Modifier.padding(start = Spacing.sm),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
fun TrialBanner(
    trialDaysLeft: Int,
    isPremium: Boolean,
    modifier: Modifier = Modifier,
    onUpgrade: () -> Unit = {},
) {
    if (isPremium || trialDaysLeft <= 0) return
    FamlyCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.sm),
        borderColor = Premium.copy(alpha = 0.33f),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("⭐", fontSize = 20.sp)
            Column(modifier = Modifier.weight(1f).padding(start = Spacing.sm)) {
                Text("Пробный Премиум", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    "Осталось $trialDaysLeft дн. · семья, split, аналитика",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.sm))
                    .background(Premium)
                    .clickable(onClick = onUpgrade)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text("Подробнее", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun PremiumGateContent(
    feature: String,
    onUpgrade: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("⭐", fontSize = 40.sp)
        Text("Премиум", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(
            "$feature доступна в Премиум",
            modifier = Modifier.padding(vertical = Spacing.sm),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(Radius.md))
                .background(Premium)
                .clickable(onClick = onUpgrade)
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Text("Попробовать Премиум", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

fun categoryAccentColor(hex: String): Color = parseHexColor(hex)
