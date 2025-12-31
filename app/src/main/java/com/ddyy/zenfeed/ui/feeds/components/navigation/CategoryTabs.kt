package com.ddyy.zenfeed.ui.feeds.components.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HideSource
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryTabs(
    pagerState: PagerState,
    categories: List<String>,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onTabDoubleClick: (Int) -> Unit = {},
    onTimeRangeSelected: (Int) -> Unit,
    selectedTimeRangeHours: Int,
    onAddToBlacklist: (String) -> Unit = {}
) {
    // 使用remember监听categories变化，确保tab栏与数据同步
    val allCategories = remember(categories) { listOf("全部") + categories }

    // 双击检测状态 - 为每个tab维护独立的双击状态
    var lastClickTimes by remember { mutableStateOf(emptyMap<Int, Long>()) }
    val doubleTapThreshold = 300L // 双击时间间隔阈值（毫秒）

    // 时间范围选择菜单状态
    var timeMenuExpanded by remember { mutableStateOf(false) }

    // 更多菜单状态
    var moreMenuExpanded by remember { mutableStateOf(false) }
    
    // 长按菜单状态
    var longPressMenuExpanded by remember { mutableStateOf(false) }
    var longPressedCategory by remember { mutableStateOf("") }
    
    // 始终显示所有tab，在右侧添加浮动更多按钮
    val showMoreButton = true // 始终显示更多按钮
    
    Box(modifier = modifier) {
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                if (pagerState.currentPage < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        ) {
            // 显示所有tab
            allCategories.forEachIndexed { index, category ->
                // 交互源，用于检测长按事件
                val interactionSource = remember { MutableInteractionSource() }
                
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        val currentTime = System.currentTimeMillis()
                        val lastTime = lastClickTimes[index] ?: 0L

                        if (currentTime - lastTime <= doubleTapThreshold) {
                            // 双击事件：滚动到列表顶部
                            onTabDoubleClick(index)
                            lastClickTimes = lastClickTimes + (index to 0L) // 重置时间避免三击
                        } else {
                            // 单击事件：切换tab
                            onTabSelected(index)
                            lastClickTimes = lastClickTimes + (index to currentTime)
                        }
                    },
                    modifier = Modifier.combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onLongClick = {
                            if (category != "全部") {
                                longPressedCategory = category
                                longPressMenuExpanded = true
                            }
                        },
                        onClick = {}
                    ),
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = category,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (category == "全部") {
                                Box {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "选择时间范围",
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable { timeMenuExpanded = true }
                                    )
                                    DropdownMenu(
                                        expanded = timeMenuExpanded,
                                        onDismissRequest = { timeMenuExpanded = false }
                                    ) {
                                        val timeRanges = listOf(
                                            "12小时内" to 12,
                                            "一天内" to 24,
                                            "三天内" to 72,
                                            "一周内" to 168,
                                            "一个月内" to 720
                                        )
                                        timeRanges.forEach { (text, hours) ->
                                            val isSelected = selectedTimeRangeHours == hours
                                            DropdownMenuItem(
                                                text = { Text(text) },
                                                onClick = {
                                                    onTimeRangeSelected(hours)
                                                    timeMenuExpanded = false
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = if (isSelected) Icons.Default.Check else Icons.Default.AccessTime,
                                                        contentDescription = null,
                                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
        
        // 浮动的更多按钮，位于TabRow最右侧
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(40.dp)
            ) {
                IconButton(
                    onClick = {
                        moreMenuExpanded = true
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "更多分类",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            // 更多分类菜单，Material 3会自动处理竖向滚动
            DropdownMenu(
                expanded = moreMenuExpanded,
                onDismissRequest = { moreMenuExpanded = false },
                offset = androidx.compose.ui.unit.DpOffset(x = (-16).dp, y = 0.dp)
            ) {
                allCategories.forEachIndexed { index, category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            onTabSelected(index)
                            moreMenuExpanded = false
                        },
                        leadingIcon = {
                            if (pagerState.currentPage == index) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "当前选中",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                }
            }
        }
        
        // 长按菜单
        DropdownMenu(
            expanded = longPressMenuExpanded,
            onDismissRequest = { longPressMenuExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("隐藏") },
                onClick = {
                    onAddToBlacklist(longPressedCategory)
                    longPressMenuExpanded = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.HideSource,
                        contentDescription = "隐藏分类"
                    )
                }
            )
        }
    }
}