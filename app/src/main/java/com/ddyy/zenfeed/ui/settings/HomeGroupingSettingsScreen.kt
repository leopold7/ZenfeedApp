package com.ddyy.zenfeed.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.ddyy.zenfeed.data.CategoryFilterConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeGroupingSettingsScreen(
    navController: NavController,
    categories: List<String>,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiState by settingsViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val configs = remember { mutableStateListOf<CategoryFilterConfig>() }
    val originalConfigs = remember { mutableStateListOf<CategoryFilterConfig>() }
    val listState = rememberLazyListState()
    var scrollToIndex by remember { mutableStateOf<Int?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }

    // 滚动到指定位置
    LaunchedEffect(scrollToIndex) {
        scrollToIndex?.let { index ->
            if (index >= 0 && index < configs.size) {
                listState.animateScrollToItem(index)
            }
            scrollToIndex = null
        }
    }
    
    LaunchedEffect(uiState.message) {
        if (uiState.message.isNotEmpty()) {
            snackbarHostState.showSnackbar(uiState.message)
            settingsViewModel.clearMessage()
        }
    }
    
    LaunchedEffect(uiState.categoryFilterConfigs, uiState.isLoading) {
        val storedConfigs = uiState.categoryFilterConfigs
        
        // 只有在数据加载完成时才进行初始化
        if (uiState.isLoading) {
            return@LaunchedEffect
        }
        
        configs.clear()
        val newConfigs = mutableListOf<CategoryFilterConfig>()
        
        // 判断是否为初次排序（存储的配置为空或所有sortOrder都是0）
        val isFirstTimeInit = storedConfigs.isEmpty() || storedConfigs.all { it.sortOrder == 0 }
        
        // 获取当前分组模式
        val groupingMode = uiState.homeGroupingMode
        
        // 如果是"分类,来源"模式，需要先排序分类，后排序来源
        if (isFirstTimeInit && groupingMode == "category,source") {
            // categories参数已经是先分类后来源的顺序，直接按顺序设置sortOrder即可
            categories.forEachIndexed { index, category ->
                val existingConfig = storedConfigs.find { config: CategoryFilterConfig -> config.categoryName == category }
                if (existingConfig != null) {
                    newConfigs.add(
                        existingConfig.copy(sortOrder = index + 1)
                    )
                } else {
                    newConfigs.add(
                        CategoryFilterConfig(
                            categoryName = category,
                            sortOrder = index + 1
                        )
                    )
                }
            }
        } else {
            // 其他模式，按照categories的顺序设置sortOrder（从1开始）
            categories.forEachIndexed { index, category ->
                val existingConfig = storedConfigs.find { config: CategoryFilterConfig -> config.categoryName == category }
                if (existingConfig != null) {
                    if (isFirstTimeInit) {
                        newConfigs.add(
                            existingConfig.copy(sortOrder = index + 1)
                        )
                    } else {
                        newConfigs.add(existingConfig)
                    }
                } else {
                    newConfigs.add(
                        CategoryFilterConfig(
                            categoryName = category,
                        )
                    )
                }
            }
        }
        
        // 按sortOrder排序，sortOrder为0的放到末尾，然后按名称排序
        newConfigs.sortWith(compareBy<CategoryFilterConfig> { it.sortOrder == 0 }.thenBy { it.sortOrder }.thenBy { it.categoryName })
        
        configs.addAll(newConfigs)
        originalConfigs.clear()
        originalConfigs.addAll(newConfigs)

        scrollToIndex = 0
    }
    
    val hasChanges = configs.size != originalConfigs.size ||
                     configs.zip(originalConfigs).any { (a, b) -> a != b }
    
    // 更新sortOrder
    fun updateSortOrders() {
        configs.forEachIndexed { index, config ->
            configs[index] = config.copy(sortOrder = index + 1)
        }
    }
    
    // 置顶
    fun moveToTop(index: Int) {
        if (index > 0) {
            val item = configs.removeAt(index)
            configs.add(0, item)
            updateSortOrders()
            scrollToIndex = 0
        }
    }
    
    // 上移
    fun moveUp(index: Int) {
        if (index > 0) {
            val item = configs.removeAt(index)
            configs.add(index - 1, item)
            updateSortOrders()
            scrollToIndex = index - 1
        }
    }
    
    // 下移
    fun moveDown(index: Int) {
        if (index < configs.size - 1) {
            val item = configs.removeAt(index)
            configs.add(index + 1, item)
            updateSortOrders()
            scrollToIndex = index + 1
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.person_home_grouping_settings),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasChanges) {
                            showExitDialog = true
                        } else {
                            navController.navigateUp()
                        }
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        content = { it ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .padding(horizontal = 16.dp)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                ) {
                    itemsIndexed(
                        items = configs,
                        key = { _, item -> item.categoryName }
                    ) { index, config ->
                        CategoryFilterConfigCard(
                            config = config,
                            index = index,
                            onConfigChange = {
                                val configIndex = configs.indexOf(config)
                                if (configIndex >= 0) {
                                    configs[configIndex] = it
                                }
                            },
                            canMoveToTop = index > 0,
                            canMoveUp = index > 0,
                            canMoveDown = index < configs.size - 1,
                            onMoveToTop = { moveToTop(index) },
                            onMoveUp = { moveUp(index) },
                            onMoveDown = { moveDown(index) }
                        )
                    }
                }
                
                SettingsBottomButtons(
                    onReset = {
                        configs.clear()
                        configs.addAll(
                            categories.mapIndexed { index, category ->
                                CategoryFilterConfig(
                                    categoryName = category,
                                    showInAll = true,
                                    showGroup = true,
                                    sortOrder = index + 1
                                )
                            }
                        )
                        scrollToIndex = 0
                    },
                    onSave = {
                        settingsViewModel.updateCategoryFilterConfigs(configs.toList())
                        settingsViewModel.saveCategoryFilterConfigs()
                    },
                    isLoading = uiState.isLoading,
                    hasChanges = hasChanges
                )
            }
        }
    )
    
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
                        settingsViewModel.updateCategoryFilterConfigs(configs.toList())
                        settingsViewModel.saveCategoryFilterConfigs()
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

@Composable
fun CategoryFilterConfigCard(
    config: CategoryFilterConfig,
    index: Int,
    onConfigChange: (CategoryFilterConfig) -> Unit,
    canMoveToTop: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveToTop: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
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
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = config.categoryName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = "#${index + 1}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "全部中显示文章",
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = config.showInAll,
                    onCheckedChange = {
                        onConfigChange(config.copy(showInAll = it))
                    }
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "显示分组",
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = config.showGroup,
                    onCheckedChange = {
                        onConfigChange(config.copy(showGroup = it))
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onMoveToTop,
                    enabled = canMoveToTop,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VerticalAlignTop,
                        contentDescription = "置顶",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("置顶")
                }
                OutlinedButton(
                    onClick = onMoveUp,
                    enabled = canMoveUp,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "上移",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("上移")
                }
                OutlinedButton(
                    onClick = onMoveDown,
                    enabled = canMoveDown,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "下移",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("下移")
                }
            }
        }
    }
}
