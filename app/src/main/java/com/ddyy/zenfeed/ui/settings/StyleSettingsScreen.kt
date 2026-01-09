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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ddyy.zenfeed.R
import com.ddyy.zenfeed.data.StyleConfig
import com.ddyy.zenfeed.ui.theme.ZenfeedTheme
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.BackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyleSettingsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by settingsViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    var tempStyleConfig by remember(uiState.styleConfig) { mutableStateOf(uiState.styleConfig) }
    val hasChanges = tempStyleConfig.tagMaxLength != uiState.styleConfig.tagMaxLength
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
                        text = "样式设置",
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                        text = "标签最大显示长度",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "设置标签的最大显示字符数，超过此长度的标签将被截断并显示省略号",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = tempStyleConfig.tagMaxLength.toString(),
                        onValueChange = { 
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                tempStyleConfig = tempStyleConfig.copy(tagMaxLength = it.toIntOrNull() ?: 6)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("标签最大长度") },
                        placeholder = { Text("例如：6") },
                        supportingText = { Text("建议范围：1-20，默认为6") },
                        enabled = !uiState.isLoading,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                tempStyleConfig = tempStyleConfig.copy(tagMaxLength = 6)
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.reset))
                        }
                        
                        Button(
                            onClick = {
                                val length = tempStyleConfig.tagMaxLength
                                if (length >= 1 && length <= 20) {
                                    settingsViewModel.updateStyleConfig(tempStyleConfig)
                                    settingsViewModel.saveStyleConfigSettings()
                                    Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "请输入1-20之间的数字", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isLoading && hasChanges,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.save))
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
                        val length = tempStyleConfig.tagMaxLength
                        if (length >= 1 && length <= 20) {
                            settingsViewModel.updateStyleConfig(tempStyleConfig)
                            settingsViewModel.saveStyleConfigSettings()
                            showExitDialog = false
                            navController.navigateUp()
                        } else {
                            Toast.makeText(context, "请输入1-20之间的数字", Toast.LENGTH_SHORT).show()
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
            }
        )
    }
}
