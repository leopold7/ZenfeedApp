package com.ddyy.zenfeed.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ddyy.zenfeed.R
import com.ddyy.zenfeed.data.SettingsDataStore
import com.ddyy.zenfeed.ui.theme.ZenfeedTheme
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.BackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogSettingsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by settingsViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    var tempMarkPodcastAsRead by remember(uiState.markPodcastAsRead) { mutableStateOf(uiState.markPodcastAsRead) }
    var tempPlaybackSpeed by remember(uiState.playbackSpeed) { mutableStateOf(uiState.playbackSpeed) }
    val hasChanges = tempMarkPodcastAsRead != uiState.markPodcastAsRead || tempPlaybackSpeed != uiState.playbackSpeed
    var showExitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.message) {
        if (uiState.message.isNotEmpty()) {
            snackbarHostState.showSnackbar(uiState.message)
            settingsViewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "博客设置",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
            ) {
                SettingsBottomButtons(
                    onReset = {
                        tempMarkPodcastAsRead = SettingsDataStore.DEFAULT_MARK_PODCAST_AS_READ
                        tempPlaybackSpeed = SettingsDataStore.DEFAULT_PLAYBACK_SPEED
                    },
                    onSave = {
                        settingsViewModel.updateMarkPodcastAsRead(tempMarkPodcastAsRead)
                        settingsViewModel.updatePlaybackSpeed(tempPlaybackSpeed)
                        settingsViewModel.saveBlogSettings()
                        Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
                    },
                    isLoading = uiState.isLoading,
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
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "自动已读",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "听博客时自动将文章标记为已读，方便管理已阅读的文章",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "启用自动已读",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = tempMarkPodcastAsRead,
                            onCheckedChange = { tempMarkPodcastAsRead = it },
                            enabled = !uiState.isLoading
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "播放速度",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "设置博客播放的默认速度，可在播放器中临时调整",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "当前速度",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${String.format("%.2f", tempPlaybackSpeed)}x",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        val speedOptions = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
                        Slider(
                            value = tempPlaybackSpeed,
                            onValueChange = { tempPlaybackSpeed = it },
                            valueRange = 0.5f..2.0f,
                            steps = speedOptions.size - 2,
                            enabled = !uiState.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            speedOptions.forEach { speed ->
                                Text(
                                    text = "${speed}x",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (tempPlaybackSpeed == speed) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.clickable(
                                        enabled = !uiState.isLoading
                                    ) {
                                        tempPlaybackSpeed = speed
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    BackHandler(enabled = hasChanges) {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = {
                Text("提示")
            },
            text = {
                Text("您还没有保存更改，是否保存？")
            },
            confirmButton = {
                Button(
                    onClick = {
                        settingsViewModel.updateMarkPodcastAsRead(tempMarkPodcastAsRead)
                        settingsViewModel.updatePlaybackSpeed(tempPlaybackSpeed)
                        settingsViewModel.saveBlogSettings()
                        showExitDialog = false
                        navController.navigateUp()
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
            }
        )
    }
}
