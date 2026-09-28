package com.example.ui.settings

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.remote.StreamUrlResolver
import com.example.player.PlayerManager
import com.example.repository.MusicRepository
import com.example.ui.theme.AccentPalette
import com.example.ui.theme.ThemeManager
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: MusicRepository,
    playerManager: PlayerManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    themeManager: ThemeManager = ThemeManager.getInstance(LocalContext.current)
) {
    BackHandler { onBack() }
    val scope = rememberCoroutineScope()

    val currentThemeMode by themeManager.themeMode.collectAsState()
    val useDynamicColor by themeManager.useDynamicColor.collectAsState()
    val isAmoledBlack by themeManager.isAmoledBlack.collectAsState()
    val accentPalette by themeManager.accentPalette.collectAsState()

    var autoplayEnabled by remember { mutableStateOf(true) }
    var selectedQuality by remember { mutableStateOf(StreamUrlResolver.AudioQuality.HIGH) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showClearRecentDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // M3 Expressive Appearance & Theme Section
            item {
                SettingsSectionHeader("Appearance & Theme")
            }

            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Theme Mode",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // 3-Way M3 Expressive Theme Selector Cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ThemeModeCard(
                                title = "System",
                                icon = Icons.Outlined.BrightnessAuto,
                                isSelected = currentThemeMode == ThemeMode.SYSTEM,
                                onClick = { themeManager.setThemeMode(ThemeMode.SYSTEM) },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeModeCard(
                                title = "Light",
                                icon = Icons.Outlined.LightMode,
                                isSelected = currentThemeMode == ThemeMode.LIGHT,
                                onClick = { themeManager.setThemeMode(ThemeMode.LIGHT) },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeModeCard(
                                title = "Dark",
                                icon = Icons.Outlined.DarkMode,
                                isSelected = currentThemeMode == ThemeMode.DARK,
                                onClick = { themeManager.setThemeMode(ThemeMode.DARK) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // AMOLED Pure Black Mode Toggle (applicable for Dark or System Dark)
                        AnimatedVisibility(visible = currentThemeMode != ThemeMode.LIGHT) {
                            Column {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { themeManager.setAmoledBlack(!isAmoledBlack) },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Contrast,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "AMOLED Pure Black",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                        Text(
                                            text = "Deep pitch black background for OLED screens & battery saving",
                                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(
                                        checked = isAmoledBlack,
                                        onCheckedChange = { themeManager.setAmoledBlack(it) }
                                    )
                                }
                            }
                        }

                        // Dynamic Color (Material You) Toggle on Android 12+
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { themeManager.setDynamicColor(!useDynamicColor) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ColorLens,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Dynamic Color (Material You)",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = "Adapt theme colors to your wallpaper & system palette",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = useDynamicColor,
                                    onCheckedChange = { themeManager.setDynamicColor(it) }
                                )
                            }
                        }

                        // Custom Accent Colors Selection (active when dynamic color is disabled)
                        AnimatedVisibility(visible = !useDynamicColor || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                )
                                Text(
                                    text = "Accent Color Palette",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    AccentPalette.entries.forEach { palette ->
                                        AccentChip(
                                            palette = palette,
                                            isSelected = accentPalette == palette,
                                            onClick = { themeManager.setAccentPalette(palette) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Playback section
            item {
                Spacer(modifier = Modifier.height(6.dp))
                SettingsSectionHeader("Playback")
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column {
                        SettingsSwitchItem(
                            icon = Icons.Outlined.Autorenew,
                            title = "Autoplay",
                            subtitle = "Automatically fetch and play recommendations when queue finishes",
                            checked = autoplayEnabled,
                            onCheckedChange = {
                                autoplayEnabled = it
                                playerManager.setAutoplayEnabled(it)
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        SettingsClickableItem(
                            icon = Icons.Outlined.HighQuality,
                            title = "Streaming Quality",
                            subtitle = when (selectedQuality) {
                                StreamUrlResolver.AudioQuality.HIGH -> "320 kbps (Extreme)"
                                StreamUrlResolver.AudioQuality.MEDIUM -> "160 kbps (High)"
                                StreamUrlResolver.AudioQuality.LOW -> "96 kbps (Normal)"
                            },
                            onClick = { showQualityDialog = true }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        SettingsSwitchItem(
                            icon = Icons.Outlined.Headphones,
                            title = "Pause on Disconnect",
                            subtitle = "Automatically pause when headphones or Bluetooth device disconnects",
                            checked = true,
                            onCheckedChange = {}
                        )
                    }
                }
            }

            // Data & Storage
            item {
                Spacer(modifier = Modifier.height(6.dp))
                SettingsSectionHeader("Data & Storage")
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column {
                        SettingsClickableItem(
                            icon = Icons.Outlined.History,
                            title = "Clear Search History",
                            subtitle = "Remove all past searches from suggestions",
                            onClick = { showClearHistoryDialog = true }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        SettingsClickableItem(
                            icon = Icons.Outlined.DeleteSweep,
                            title = "Clear Recently Played",
                            subtitle = "Reset your recently played songs history",
                            onClick = { showClearRecentDialog = true }
                        )
                    }
                }
            }

            // About SMusic
            item {
                Spacer(modifier = Modifier.height(6.dp))
                SettingsSectionHeader("About SMusic")
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "SMusic",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Material 3 Expressive Music Experience with real streaming, smart recommendations, and lock screen player controls.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Version 2.4.0 (Expressive M3)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    // Streaming Quality Dialog
    if (showQualityDialog) {
        AlertDialog(
            onDismissRequest = { showQualityDialog = false },
            title = { Text("Streaming Quality", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    StreamUrlResolver.AudioQuality.values().forEach { quality ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedQuality = quality
                                    showQualityDialog = false
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Quality set to ${quality.name}")
                                    }
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedQuality == quality,
                                onClick = {
                                    selectedQuality = quality
                                    showQualityDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = when (quality) {
                                        StreamUrlResolver.AudioQuality.HIGH -> "Extreme (320 kbps)"
                                        StreamUrlResolver.AudioQuality.MEDIUM -> "High (160 kbps)"
                                        StreamUrlResolver.AudioQuality.LOW -> "Normal (96 kbps)"
                                    },
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = when (quality) {
                                        StreamUrlResolver.AudioQuality.HIGH -> "Best audio fidelity (FLAC / AAC-HQ)"
                                        StreamUrlResolver.AudioQuality.MEDIUM -> "Balanced data & fidelity"
                                        StreamUrlResolver.AudioQuality.LOW -> "Data saver mode"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQualityDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Clear History Dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear Search History") },
            text = { Text("Are you sure you want to clear your search history?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            repository.clearSearchHistory()
                            showClearHistoryDialog = false
                            snackbarHostState.showSnackbar("Search history cleared")
                        }
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear Recent Dialog
    if (showClearRecentDialog) {
        AlertDialog(
            onDismissRequest = { showClearRecentDialog = false },
            title = { Text("Clear Recently Played") },
            text = { Text("Are you sure you want to reset your recently played songs?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            repository.clearRecentlyPlayed()
                            showClearRecentDialog = false
                            snackbarHostState.showSnackbar("Recently played history cleared")
                        }
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearRecentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ThemeModeCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .border(width = if (isSelected) 2.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = contentColor
            )
        }
    }
}

@Composable
private fun AccentChip(
    palette: AccentPalette,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .border(width = if (isSelected) 2.dp else 0.dp, color = borderColor, shape = RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(palette.primaryDark),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Text(
                text = palette.displayName,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        }
    }
}
