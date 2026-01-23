package com.ddyy.zenfeed.ui.favorites

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ddyy.zenfeed.data.FeedRepository
import com.ddyy.zenfeed.data.ServerConfig
import com.ddyy.zenfeed.data.StyleConfig
import com.ddyy.zenfeed.extension.navigateToFeedDetail
import com.ddyy.zenfeed.ui.SharedViewModel
import com.ddyy.zenfeed.ui.feeds.components.common.FeedItem
import com.ddyy.zenfeed.ui.player.PlayerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteFeedsScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    favoritesViewModel: FavoritesViewModel,
    playerViewModel: PlayerViewModel,
    serverConfigs: List<ServerConfig>,
    imageCacheEnabled: Boolean,
    styleConfig: StyleConfig,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var menuExpanded by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    val feeds = favoritesViewModel.favoriteFeeds

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "我的收藏",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "更多"
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("一键清空") },
                                onClick = {
                                    menuExpanded = false
                                    showClearDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.DeleteSweep,
                                        contentDescription = "一键清空"
                                    )
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (feeds.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.StarBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = "暂无收藏",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "在文章详情页点击星标即可收藏",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(1),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 12.dp),
                verticalItemSpacing = 0.dp
            ) {
                items(
                    count = feeds.size,
                    key = { index ->
                        val feed = feeds[index]
                        "${feed.time}-${feed.labels.title?.hashCode() ?: 0}"
                    }
                ) { index ->
                    val feed = feeds[index]
                    val playlistInfo = playerViewModel.playlistInfo.value
                    val isPlaying = playerViewModel.isPlaying.value == true
                    val currentPlaylist = playerViewModel.getCurrentPlaylist()
                    val isCurrentlyPlaying = if (feed.labels.podcastUrl.isNullOrBlank()) {
                        false
                    } else {
                        playlistInfo?.let { info ->
                            info.currentIndex >= 0 &&
                                info.currentIndex < currentPlaylist.size &&
                                currentPlaylist[info.currentIndex].labels.podcastUrl == feed.labels.podcastUrl
                        } == true
                    }

                    FeedItem(
                        feed = feed,
                        serverConfigs = serverConfigs,
                        onClick = {
                            sharedViewModel.updateAllFeeds(feeds)
                            sharedViewModel.setEntryCategory("收藏")
                            sharedViewModel.selectFeed(feed)
                            sharedViewModel.updateLastViewedFeed(feed)
                            sharedViewModel.updateDetailPageStatus(true)

                            coroutineScope.launch {
                                val repo = FeedRepository.getInstance(context)
                                val feedId = "${feed.labels.title.orEmpty()}-${feed.time}-${feed.serverId.orEmpty()}"
                                withContext(Dispatchers.IO) {
                                    repo.addReadFeedId(feedId)
                                }
                            }

                            navController.navigateToFeedDetail()
                        },
                        onPlayPodcastList = if (!feed.labels.podcastUrl.isNullOrBlank()) {
                            {
                                val podcastFeeds = feeds.filter { !it.labels.podcastUrl.isNullOrBlank() }
                                if (podcastFeeds.isNotEmpty()) {
                                    val correctedIndex = podcastFeeds.indexOfFirst {
                                        it.labels.podcastUrl == feed.labels.podcastUrl
                                    }.coerceAtLeast(0)
                                    playerViewModel.playPodcastPlaylist(podcastFeeds, correctedIndex, context)
                                } else {
                                    Toast.makeText(context, "没有可播放的博客", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else null,
                        onTogglePlayPause = if (!feed.labels.podcastUrl.isNullOrBlank()) {
                            { playerViewModel.togglePlayPause() }
                        } else null,
                        isCurrentlyPlaying = isCurrentlyPlaying,
                        isPlaying = isPlaying,
                        imageCacheEnabled = imageCacheEnabled,
                        styleConfig = styleConfig
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("提示") },
            text = { Text("确定要清空所有收藏吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        favoritesViewModel.clearAll()
                        showClearDialog = false
                        Toast.makeText(context, "已清空收藏", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("清空")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
}
