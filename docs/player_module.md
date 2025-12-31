# 播放器模块指南

## 1. 模块概述

播放器模块是ZenFeedApp的核心功能之一，负责音频内容的播放、控制和管理。该模块采用服务端客户端架构，使用MediaSession API实现后台播放和通知栏控制，支持播客音频的播放、暂停、快进、快退、倍速播放等功能。

## 2. 核心组件

### 2.1 PlayerService

**职责**：
- 实现后台音频播放服务
- 管理媒体会话
- 处理音频焦点
- 提供通知栏控制
- 支持耳机控制和蓝牙设备

**主要功能**：

| 功能 | 方法/属性 | 说明 |
|------|-----------|------|
| 媒体会话 | `mediaSession: MediaSessionCompat` | 管理媒体播放会话 |
| 媒体播放器 | `mediaPlayer: MediaPlayer?` | 底层媒体播放实现 |
| 播放列表管理 | `playlist: List<Feed>`, `currentIndex: Int` | 管理播放列表和当前播放位置 |
| 播放状态 | `isPlaying: Boolean`, `currentPosition: Int` | 提供播放状态信息 |
| 播放控制 | `play()`, `pause()`, `stop()`, `skipToNext()`, `skipToPrevious()` | 基本播放控制方法 |
| 进度控制 | `seekTo(position: Int)`, `setPlaybackSpeed(speed: Float)` | 进度调整和倍速播放 |
| 通知管理 | `showNotification()`, `hideNotification()` | 显示和隐藏播放通知 |
| 音频焦点处理 | `onAudioFocusChange(focusChange: Int)` | 处理音频焦点变化 |

### 2.2 PlayerViewModel

**职责**：
- 管理播放器的状态和业务逻辑
- 作为Service和UI之间的桥梁
- 处理播放列表管理
- 提供播放器控制API

**主要功能**：

| 功能 | 方法签名 | 说明 |
|------|----------|------|
| 绑定服务 | `fun bindService(context: Context)` | 绑定到PlayerService |
| 解绑服务 | `fun unbindService(context: Context)` | 解绑PlayerService |
| 播放播客列表 | `fun playPodcastPlaylist(feeds: List<Feed>, startIndex: Int = 0, context: Context)` | 开始播放播客列表 |
| 播放/暂停 | `fun togglePlayPause()` | 切换播放/暂停状态 |
| 下一首/上一首 | `fun skipToNext()`, `fun skipToPrevious()` | 切换到下一首或上一首 |
| 调整进度 | `fun seekTo(position: Int)` | 调整播放进度 |
| 设置倍速 | `fun setPlaybackSpeed(speed: Float)` | 设置播放速度 |
| 设置循环模式 | `fun setRepeatMode(mode: Int)` | 设置循环播放模式 |
| 设置随机播放 | `fun setShuffleModeEnabled(enabled: Boolean)` | 设置随机播放模式 |
| 获取当前播放列表 | `fun getCurrentPlaylist(): List<Feed>` | 获取当前播放列表 |

**状态管理**：

| 状态属性 | 类型 | 说明 |
|----------|------|------|
| `isPlaying` | `MutableStateFlow<Boolean?>` | 播放状态 |
| `currentPosition` | `MutableStateFlow<Int>` | 当前播放进度 |
| `duration` | `MutableStateFlow<Int>` | 总播放时长 |
| `currentFeed` | `MutableStateFlow<Feed?>` | 当前播放的Feed |
| `playlistInfo` | `MutableStateFlow<PlaylistInfo?>` | 播放列表信息 |
| `playbackSpeed` | `MutableStateFlow<Float>` | 播放速度 |

### 2.3 SharedViewModel

**职责**：
- 管理多个页面共享的播放器状态
- 协调不同页面的播放器操作
- 处理从通知栏和外部Intent的播放请求

**主要功能**：

| 功能 | 方法签名 | 说明 |
|------|----------|------|
| 更新所有Feed | `fun updateAllFeeds(feeds: List<Feed>)` | 更新共享的Feed列表 |
| 选择Feed | `fun selectFeed(feed: Feed)` | 选择要播放的Feed |
| 从Intent选择Feed | `fun selectFeedFromIntent(intent: Intent)` | 从Intent中解析并选择Feed |
| 设置导航标志 | `fun setNavigateToDetail(shouldNavigate: Boolean)` | 设置是否导航到详情页 |
| 更新最后浏览的Feed | `fun updateLastViewedFeed(feed: Feed)` | 更新最后浏览的Feed |
| 获取当前Feed索引 | `fun getCurrentFeedIndex(): Int` | 获取当前Feed在列表中的索引 |

## 3. 架构设计

### 3.1 服务端客户端架构

播放器模块采用服务端客户端架构：

- **服务端**：`PlayerService`作为后台服务，负责实际的音频播放
- **客户端**：`PlayerViewModel`作为客户端，提供与Service通信的API
- **通信机制**：使用Binder实现Service和客户端之间的通信

### 3.2 状态管理流程

```
UI → PlayerViewModel → PlayerService → MediaPlayer → 音频输出
PlayerService → MediaSession → 通知栏/耳机控制
PlayerService → PlayerViewModel → StateFlow → UI
```

### 3.3 播放控制流程

1. **用户交互**：用户在UI上执行播放、暂停等操作
2. **ViewModel处理**：`PlayerViewModel`接收并处理用户操作
3. **服务通信**：通过Binder调用`PlayerService`的相应方法
4. **媒体播放**：`PlayerService`使用`MediaPlayer`执行实际的播放操作
5. **状态更新**：`PlayerService`更新播放状态
6. **UI更新**：`PlayerViewModel`通过StateFlow将状态变化通知给UI

## 4. 功能详解

### 4.1 播放列表管理

**功能说明**：
- 支持播放单个播客或播客列表
- 支持从当前位置开始播放
- 支持循环播放、随机播放模式
- 支持添加到播放列表和从播放列表移除

**使用示例**：

```kotlin
// 播放播客列表
playerViewModel.playPodcastPlaylist(
    feeds = podcastFeeds,
    startIndex = 0,
    context = context
)

// 获取当前播放列表
val currentPlaylist = playerViewModel.getCurrentPlaylist()

// 获取播放列表信息
val playlistInfo = playerViewModel.playlistInfo.value
```

### 4.2 播放控制

**功能说明**：
- 播放/暂停控制
- 上一首/下一首切换
- 进度调整
- 倍速播放（0.5x - 2.0x）

**使用示例**：

```kotlin
// 切换播放/暂停状态
playerViewModel.togglePlayPause()

// 下一首
playerViewModel.skipToNext()

// 上一首
playerViewModel.skipToPrevious()

// 调整进度到30秒位置
playerViewModel.seekTo(30000)

// 设置播放速度为1.5倍速
playerViewModel.setPlaybackSpeed(1.5f)
```

### 4.3 后台播放

**功能说明**：
- 支持应用在后台时继续播放
- 提供通知栏控制界面
- 支持媒体会话，可通过系统媒体控件控制
- 处理音频焦点，与其他应用协调音频播放

**实现细节**：
- 使用前台服务确保后台播放稳定性
- 通过MediaSessionCompat实现媒体会话
- 监听音频焦点变化，处理中断和恢复
- 支持耳机插拔和蓝牙设备控制

### 4.4 通知栏控制

**功能说明**：
- 显示当前播放信息（标题、作者、封面）
- 提供播放/暂停、上一首/下一首控制按钮
- 支持进度显示和调整
- 点击通知跳转到应用
- 滑动通知可关闭播放

## 5. 使用指南

### 5.1 初始化和绑定服务

**在Activity或Compose组件中**：

```kotlin
// 在ViewModel中
val playerViewModel: PlayerViewModel = viewModel()

// 在Compose中绑定服务
val context = LocalContext.current
DisposableEffect(playerViewModel) {
    playerViewModel.bindService(context)
    onDispose {
        playerViewModel.unbindService(context)
    }
}
```

### 5.2 播放播客

**从列表播放**：

```kotlin
// 在FeedsScreen或FeedDetailScreen中
onPlayPodcastList = { feeds, startIndex ->
    // 过滤出有播客URL的Feed
    val podcastFeeds = feeds.filter { !it.labels.podcastUrl.isNullOrBlank() }
    if (podcastFeeds.isNotEmpty()) {
        // 找到当前Feed在过滤后列表中的正确索引
        val targetFeed = feeds[startIndex]
        val correctedIndex = podcastFeeds.indexOfFirst { 
            it.labels.podcastUrl == targetFeed.labels.podcastUrl 
        }.coerceAtLeast(0)

        // 播放播客列表
        playerViewModel.playPodcastPlaylist(podcastFeeds, correctedIndex, context)
    }
}
```

### 5.3 监听播放状态

**在Compose组件中**：

```kotlin
val isPlaying by playerViewModel.isPlaying.collectAsState()
val currentFeed by playerViewModel.currentFeed.collectAsState()
val currentPosition by playerViewModel.currentPosition.collectAsState()
val duration by playerViewModel.duration.collectAsState()

// 根据播放状态显示不同UI
if (isPlaying == true && currentFeed != null) {
    // 显示播放控件
    PlayerControls(
        isPlaying = isPlaying == true,
        onPlayPause = { playerViewModel.togglePlayPause() },
        onNext = { playerViewModel.skipToNext() },
        onPrevious = { playerViewModel.skipToPrevious() },
        currentPosition = currentPosition,
        duration = duration,
        onSeek = { playerViewModel.seekTo(it) }
    )
}
```

### 5.4 处理播放完成

**播放器自动处理**：
- 播放完成后自动播放下一首（如果有）
- 所有曲目播放完成后停止播放
- 循环模式下重新开始播放

## 6. 最佳实践

### 6.1 资源管理

- 确保在组件销毁时解绑服务
- 使用`DisposableEffect`管理Service的绑定和解绑
- 避免内存泄漏，及时清理资源

### 6.2 状态同步

- 始终通过`PlayerViewModel`控制播放器，避免直接调用Service
- 使用StateFlow监听播放状态变化
- 在Compose中使用`collectAsState`将StateFlow转换为可组合状态

### 6.3 错误处理

- 处理网络错误和媒体加载失败
- 提供友好的错误提示
- 支持重试机制
- 确保播放器在错误情况下能够恢复

### 6.4 性能优化

- 合理管理MediaPlayer实例，避免频繁创建和销毁
- 优化通知更新频率
- 避免在主线程执行耗时操作
- 使用协程处理异步操作

## 7. 测试策略

### 7.1 单元测试

- 测试`PlayerViewModel`的业务逻辑
- 测试播放列表管理功能
- 测试状态转换逻辑

### 7.2 集成测试

- 测试Service和ViewModel之间的通信
- 测试播放控制功能
- 测试通知栏功能
- 测试音频焦点处理

### 7.3 UI测试

- 测试播放器控件的交互
- 测试播放状态变化时的UI更新
- 测试从不同页面控制播放器

## 8. 扩展性设计

### 8.1 支持多种音频格式

- 当前支持MP3、AAC等常见音频格式
- 可扩展支持更多格式，如OGG、FLAC等

### 8.2 支持在线流媒体

- 当前支持HTTP/HTTPS协议的音频流
- 可扩展支持HLS、DASH等流媒体协议

### 8.3 支持本地音频文件

- 当前主要支持在线音频播放
- 可扩展支持本地音频文件的播放

### 8.4 支持更多播放控制功能

- 可扩展支持播放列表编辑
- 支持收藏曲目
- 支持播放历史记录
- 支持音频效果调节（均衡器、音效）

## 9. 总结

播放器模块是ZenFeedApp的核心功能之一，采用了现代化的架构设计和技术栈。该模块提供了完整的音频播放功能，支持后台播放、通知栏控制、耳机控制等特性，为用户提供了良好的播客收听体验。在开发新功能时，应遵循现有架构设计和最佳实践，确保播放器模块的稳定性、可靠性和扩展性。