package it.unibo.psm.ricettasi.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.unibo.psm.ricettasi.R
import it.unibo.psm.ricettasi.data.settings.ThemeOption
import it.unibo.psm.ricettasi.ui.theme.FrauncesFamily
import it.unibo.psm.ricettasi.ui.theme.ManropeFamily
import it.unibo.psm.ricettasi.ui.theme.RoundedLg
import it.unibo.psm.ricettasi.ui.theme.Space2xl
import it.unibo.psm.ricettasi.ui.theme.SpaceLg
import it.unibo.psm.ricettasi.ui.theme.SpaceMd
import it.unibo.psm.ricettasi.ui.theme.SpaceXl
import it.unibo.psm.ricettasi.ui.theme.SpaceXs
import org.koin.compose.koinInject

private val AccentColor = Color(0xFFE65F2B)
private val ErrorBg = Color(0xFFFFF0EB)

// -- Route --

/** Settings screen entry point. */
@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinInject(),
) {
    val selectedTheme by viewModel.selectedTheme.collectAsStateWithLifecycle(
        initialValue = ThemeOption.AUTO,
    )

    SettingsScreen(
        onBack = onBack,
        selectedTheme = selectedTheme,
        onSelectTheme = viewModel::setTheme,
        onSignOut = viewModel::signOut,
    )
}

// -- Main screen --

@Composable
private fun SettingsScreen(
    onBack: () -> Unit,
    selectedTheme: ThemeOption,
    onSelectTheme: (ThemeOption) -> Unit,
    onSignOut: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Header
        SettingsHeader(onBack = onBack)

        Spacer(Modifier.height(Space2xl))

        // Theme section
        SectionLabel(text = "Tema")
        Spacer(Modifier.height(SpaceMd))
        ThemeBlock(
            selected = selectedTheme,
            onSelect = onSelectTheme,
        )

        Spacer(Modifier.height(Space2xl))

        // Account section
        SectionLabel(text = "Account")
        Spacer(Modifier.height(SpaceMd))
        LogoutBlock(onClick = onSignOut)

        Spacer(Modifier.weight(1f))
    }
}

// -- Header --

@Composable
private fun SettingsHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = SpaceXl, end = SpaceXl, top = SpaceXl, bottom = SpaceMd),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Impostazioni",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_left),
            contentDescription = "Indietro",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onBack),
        )
    }
}

// -- Section label --

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontFamily = FrauncesFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = SpaceXl),
    )
}

// -- Theme selection block --

@Composable
private fun ThemeBlock(
    selected: ThemeOption,
    onSelect: (ThemeOption) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpaceXl),
        shape = RoundedCornerShape(RoundedLg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            ThemeRow(
                option = ThemeOption.LIGHT,
                isSelected = selected == ThemeOption.LIGHT,
                onClick = { onSelect(ThemeOption.LIGHT) },
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = SpaceXl),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
            )
            ThemeRow(
                option = ThemeOption.DARK,
                isSelected = selected == ThemeOption.DARK,
                onClick = { onSelect(ThemeOption.DARK) },
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = SpaceXl),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
            )
            ThemeRow(
                option = ThemeOption.AUTO,
                isSelected = selected == ThemeOption.AUTO,
                onClick = { onSelect(ThemeOption.AUTO) },
            )
        }
    }
}

@Composable
private fun ThemeRow(
    option: ThemeOption,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = SpaceXl, vertical = SpaceLg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Leading icon circle
        val (iconRes, circleBg) = when (option) {
            ThemeOption.LIGHT -> R.drawable.ic_sun to AccentColor
            ThemeOption.DARK -> R.drawable.ic_moon to Color(0xFFF0F0F0)
            ThemeOption.AUTO -> R.drawable.ic_monitor to Color(0xFFF0F0F0)
        }
        val iconTint = when (option) {
            ThemeOption.LIGHT -> Color.White
            else -> MaterialTheme.colorScheme.onSurface
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(circleBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = option.label,
                tint = iconTint,
                modifier = Modifier.size(18.dp),
            )
        }

        Spacer(Modifier.width(SpaceLg))

        // Label and description
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.label,
                fontFamily = ManropeFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = option.description,
                fontFamily = ManropeFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Trailing radio button
        RadioButton(active = isSelected)
    }
}

// -- Radio button --

@Composable
private fun RadioButton(active: Boolean) {
    val size = 22.dp
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (active) {
                    Modifier.background(AccentColor)
                } else {
                    Modifier
                        .background(Color.Transparent)
                        .then(
                            Modifier.border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape)
                        )
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (active) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color.White),
            )
        }
    }
}

// -- Logout block --

@Composable
private fun LogoutBlock(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpaceXl),
        shape = RoundedCornerShape(RoundedLg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = SpaceXl, vertical = SpaceLg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Leading icon circle (error tint)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ErrorBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_logout),
                    contentDescription = "Esci",
                    tint = AccentColor,
                    modifier = Modifier.size(18.dp),
                )
            }

            Spacer(Modifier.width(SpaceLg))

            Text(
                text = "Esci",
                fontFamily = ManropeFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = AccentColor,
            )
        }
    }
}