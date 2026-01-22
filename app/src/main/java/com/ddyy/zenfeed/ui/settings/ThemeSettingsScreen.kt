package com.ddyy.zenfeed.ui.settings

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ddyy.zenfeed.R
import com.ddyy.zenfeed.data.SettingsDataStore
import androidx.core.graphics.ColorUtils
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.launch

private data class ThemeColorOption(
    val id: String,
    val color: Color
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ThemeSettingsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val settingsDataStore = remember { SettingsDataStore(context) }
    val themeMode by settingsDataStore.themeMode.collectAsState(initial = SettingsDataStore.DEFAULT_THEME_MODE)
    val themeColorId by settingsDataStore.themeColor.collectAsState(initial = SettingsDataStore.DEFAULT_THEME_COLOR)

    var tempThemeMode by remember(themeMode) { mutableStateOf(themeMode) }
    var tempThemeColorId by remember(themeColorId) { mutableStateOf(themeColorId) }
    val hasChanges = tempThemeMode != themeMode || tempThemeColorId != themeColorId
    var showExitDialog by remember { mutableStateOf(false) }

    val themeModeOptions = remember {
        listOf(
            "system" to "跟随系统",
            "light" to "日间模式",
            "dark" to "夜间模式"
        )
    }

    val themeColorOptions = remember {
        listOf(
            ThemeColorOption(
                id = SettingsDataStore.DEFAULT_THEME_COLOR,
                color = Color(0xFF009688)
            ),
            ThemeColorOption("blue", Color(0xFF2196F3)),
            ThemeColorOption("indigo", Color(0xFF3F51B5)),
            ThemeColorOption("purple", Color(0xFF9C27B0)),
            ThemeColorOption("pink", Color(0xFFE91E63)),
            ThemeColorOption("red", Color(0xFFF44336)),
            ThemeColorOption("orange", Color(0xFFFF9800)),
            ThemeColorOption("amber", Color(0xFFFFC107)),
            ThemeColorOption("green", Color(0xFF4CAF50)),
            ThemeColorOption("cyan", Color(0xFF00BCD4))
        )
    }
    val isDarkPreview = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "主题设置",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasChanges) showExitDialog = true else navController.navigateUp()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                SettingsBottomButtons(
                    onReset = {
                        tempThemeMode = SettingsDataStore.DEFAULT_THEME_MODE
                        tempThemeColorId = SettingsDataStore.DEFAULT_THEME_COLOR
                    },
                    onSave = {
                        coroutineScope.launch {
                            settingsDataStore.saveThemeMode(tempThemeMode)
                            settingsDataStore.saveThemeColor(tempThemeColorId)
                            Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
                        }
                    },
                    hasChanges = hasChanges
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "主题模式",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "选择应用外观模式：跟随系统、日间或夜间",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    themeModeOptions.forEach { (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { tempThemeMode = mode }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = label, style = MaterialTheme.typography.bodyLarge)
                            RadioButton(
                                selected = tempThemeMode == mode,
                                onClick = { tempThemeMode = mode }
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "主题色",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "选择一个强调色，用于按钮、顶部栏等关键元素",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    ThemeColorGrid(
                        options = themeColorOptions,
                        selectedId = tempThemeColorId,
                        isDarkPreview = isDarkPreview,
                        onSelect = { tempThemeColorId = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    BackHandler(enabled = hasChanges) {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("提示") },
            text = { Text("您还没有保存更改，是否保存？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            settingsDataStore.saveThemeMode(tempThemeMode)
                            settingsDataStore.saveThemeColor(tempThemeColorId)
                            showExitDialog = false
                            navController.navigateUp()
                        }
                    }
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showExitDialog = false
                        navController.navigateUp()
                    }
                ) {
                    Text("不保存")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun ThemeColorGrid(
    options: List<ThemeColorOption>,
    selectedId: String,
    isDarkPreview: Boolean,
    onSelect: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        options.forEach { option ->
            val selected = selectedId == option.id
            val displayColor = option.color
            ThemeColorSwatch(
                color = displayColor.adjustForPreview(isDarkPreview),
                selected = selected,
                onClick = { onSelect(option.id) }
            )
        }
    }
}

@Composable
private fun ThemeColorSwatch(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val borderWidth = if (selected) 2.dp else 1.dp
    val checkColor = if (color.luminance() > 0.5f) Color.Black else Color.White

    Card(
        modifier = Modifier
            .size(42.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = color),
        border = BorderStroke(borderWidth, borderColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (selected) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = checkColor
                )
            }
        }
    }
}

private fun Color.adjustForPreview(isDarkPreview: Boolean): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(this.toArgb(), hsl)

    hsl[1] = if (isDarkPreview) {
        (hsl[1] * 0.72f).coerceIn(0.18f, 0.65f)
    } else {
        (hsl[1] * 0.76f).coerceIn(0.14f, 0.62f)
    }
    hsl[2] = if (isDarkPreview) {
        hsl[2].coerceIn(0.40f, 0.55f)
    } else {
        hsl[2].coerceIn(0.22f, 0.42f)
    }
    return Color(ColorUtils.HSLToColor(hsl))
}
