# UI模块指南

## 1. 模块概述

UI模块是ZenFeedApp的用户界面层，采用Jetpack Compose实现声明式UI。该模块负责展示界面、处理用户交互和响应状态变化，遵循现代化的UI设计原则和Material Design规范。

## 2. 核心组件

### 2.1 MainActivity

**职责**：
- 应用的入口点
- 设置Compose内容
- 处理启动Intent和配置变更
- 初始化应用状态

**主要功能**：

| 功能 | 方法签名 | 说明 |
|------|----------|------|
| 初始化应用 | `override fun onCreate(savedInstanceState: Bundle?)` | 设置Compose内容，初始化数据存储 |
| 处理新Intent | `override fun onNewIntent(intent: Intent)` | 处理从通知栏等外部启动的Intent |
| 处理Intent | `private fun handleIntent(intent: Intent?)` | 解析Intent并执行相应操作 |
| 检查应用更新 | `private fun checkUpdateOnStart()` | 启动时检查应用更新 |

**使用示例**：

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 初始化设置数据存储
        settingsDataStore = SettingsDataStore(applicationContext)
        
        setContent {
            // 获取主题控制器
            val themeController = rememberThemeController(settingsDataStore)
            
            ZenfeedTheme(darkTheme = themeController.useDarkTheme) {
                AppNavigation(sharedViewModel = sharedViewModel)
            }
        }
    }
}
```

### 2.2 AppNavigation

**职责**：
- 定义应用的导航结构
- 管理页面间的跳转和过渡动画
- 处理导航参数
- 协调不同页面的状态

**主要功能**：

| 功能 | 说明 |
|------|------|
| 定义导航图 | 使用`NavHost`和`composable`定义页面路由 |
| 配置过渡动画 | 为页面进入、退出、返回等配置动画效果 |
| 处理导航事件 | 监听和处理来自ViewModel的导航事件 |
| 传递参数 | 在页面间传递数据和参数 |

**导航结构**：

```
NavHost(
    navController = navController,
    startDestination = "feeds"
) {
    composable("feeds") { /* FeedsScreen */ }
    composable("feedDetail") { /* FeedDetailScreen */ }
    composable("webview") { /* WebViewScreen */ }
    composable("settings") { /* SettingsScreen */ }
    composable("logging") { /* LoggingScreen */ }
    composable("about") { /* AboutScreen */ }
}
```

### 2.3 屏幕组件

**主要屏幕组件**：

| 组件 | 职责 | 核心功能 |
|------|------|----------|
| `FeedsScreen` | 展示摘要列表 | 分类筛选、搜索、文章卡片展示 |
| `FeedDetailScreen` | 展示文章详情 | HTML内容渲染、左右滑动切换、播客播放 |
| `WebViewScreen` | 内置浏览器 | 网页加载、前进后退、分享功能 |
| `SettingsScreen` | 设置页面 | API配置、代理设置、主题切换 |
| `LoggingScreen` | 日志页面 | 展示应用日志、清除日志 |
| `AboutScreen` | 关于页面 | 应用信息、版本号、开源协议 |

**组件设计原则**：

1. **单一职责**：每个组件只负责一项功能
2. **可组合性**：组件应易于组合和复用
3. **状态驱动**：UI由状态驱动，避免直接操作UI
4. **无副作用**：组件本身不应产生副作用，副作用应通过`LaunchedEffect`等处理

### 2.4 主题管理

**职责**：
- 管理应用的主题样式
- 支持浅色/深色/系统主题切换
- 提供统一的色彩和排版规范

**核心组件**：

| 组件 | 职责 | 主要功能 |
|------|------|----------|
| `ZenfeedTheme` | 主题包装器 | 应用主题样式到子组件 |
| `ThemeController` | 主题控制器 | 管理主题模式的切换和持久化 |
| `Color.kt` | 色彩定义 | 定义应用的色彩方案 |
| `Type.kt` | 排版定义 | 定义应用的字体样式 |
| `Theme.kt` | 主题配置 | 配置主题的各种属性 |

**使用示例**：

```kotlin
// 在Compose中使用主题
ZenfeedTheme(darkTheme = useDarkTheme) {
    // 应用主题的子组件
    Surface {
        Text(text = "Hello, World!", style = MaterialTheme.typography.headlineMedium)
    }
}

// 主题控制器使用
val themeController = rememberThemeController(settingsDataStore)
LaunchedEffect(Unit) {
    themeController.themeMode.collect {mode ->
        // 处理主题变化
    }
}
```

## 3. 导航系统

### 3.1 导航结构

应用采用单活动多页面的导航结构，使用Jetpack Navigation Compose管理页面跳转。

**主要页面路由**：

| 路由 | 页面组件 | 说明 |
|------|----------|------|
| `feeds` | `FeedsScreen` | 摘要列表页面（首页） |
| `feedDetail` | `FeedDetailScreen` | 文章详情页面 |
| `webview` | `WebViewScreen` | 内置浏览器页面 |
| `settings` | `SettingsScreen` | 设置页面 |
| `logging` | `LoggingScreen` | 日志页面 |
| `about` | `AboutScreen` | 关于页面 |

### 3.2 导航动画

为提升用户体验，应用为页面切换配置了平滑的过渡动画：

| 动画类型 | 配置 | 效果 |
|----------|------|------|
| 进入动画 | `slideIntoContainer` + `fadeIn` | 从右向左滑入，伴随淡入效果 |
| 退出动画 | `fadeOut` + `slideOutOfContainer` | 先淡出再滑出，避免背景色闪烁 |
| 返回动画 | `fadeIn` | 快速淡入，避免闪烁 |

**动画配置示例**：

```kotlin
composable(
    "feedDetail",
    enterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ) + fadeIn(
            animationSpec = tween(durationMillis = 150, delayMillis = 50, easing = FastOutSlowInEasing)
        )
    },
    exitTransition = {
        fadeOut(
            animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing)
        ) + slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(durationMillis = 250, delayMillis = 50, easing = FastOutSlowInEasing)
        )
    }
) {
    // 页面内容
}
```

### 3.3 页面间通信

页面间通信主要通过以下方式实现：

1. **SharedViewModel**：多个页面共享的状态管理
2. **导航参数**：通过导航参数传递简单数据
3. **回调函数**：通过lambda函数传递事件
4. **Intent**：处理外部应用的启动请求

## 4. 组件设计模式

### 4.1 容器组件与展示组件分离

- **容器组件**：负责数据获取和状态管理，如`FeedsScreen`
- **展示组件**：负责UI展示，如`FeedItem`、`CategoryTabs`等
- 分离关注点，提高组件的复用性和可测试性

### 4.2 状态提升

- 将共享状态提升到共同的父组件
- 避免组件间的直接依赖
- 便于状态的集中管理和调试

### 4.3 副作用处理

- 使用`LaunchedEffect`处理协程副作用
- 使用`DisposableEffect`处理需要清理的资源
- 使用`SideEffect`处理Compose重组时的副作用
- 使用`rememberCoroutineScope`创建协程作用域

**副作用处理示例**：

```kotlin
// 监听状态变化并执行异步操作
LaunchedEffect(feedId) {
    feedsViewModel.markFeedAsRead(feed)
}

// 处理需要清理的资源
DisposableEffect(playerViewModel) {
    playerViewModel.bindService(context)
    onDispose {
        playerViewModel.unbindService(context)
    }
}
```

## 5. 状态管理

### 5.1 UI状态

UI状态使用`sealed class`定义，便于处理不同状态的UI展示：

```kotlin
sealed class FeedsUiState {
    object Loading : FeedsUiState()
    data class Success(val feeds: List<Feed>) : FeedsUiState()
    data class Error(val message: String) : FeedsUiState()
}
```

### 5.2 状态转换

在ViewModel中管理UI状态的转换：

```kotlin
private val _uiState = MutableStateFlow<FeedsUiState>(FeedsUiState.Loading)
val uiState: StateFlow<FeedsUiState> = _uiState.asStateFlow()

suspend fun getFeeds() {
    _uiState.value = FeedsUiState.Loading
    try {
        val result = feedRepository.getFeeds()
        if (result.isSuccess) {
            _uiState.value = FeedsUiState.Success(result.getOrThrow().feeds)
        } else {
            _uiState.value = FeedsUiState.Error(result.exceptionOrNull()?.message ?: "加载失败")
        }
    } catch (e: Exception) {
        _uiState.value = FeedsUiState.Error(e.message ?: "加载失败")
    }
}
```

### 5.3 在Compose中使用状态

在Compose组件中使用`collectAsState`将Flow转换为可组合状态：

```kotlin
val feedsUiState by feedsViewModel.uiState.collectAsState()

when (feedsUiState) {
    is FeedsUiState.Loading -> {
        // 加载中UI
        ModernLoadingScreen()
    }
    is FeedsUiState.Success -> {
        // 成功状态UI
        val feeds = (feedsUiState as FeedsUiState.Success).feeds
        LazyColumn {
            items(feeds) { feed ->
                FeedItem(feed = feed, onItemClick = { /* 处理点击事件 */ })
            }
        }
    }
    is FeedsUiState.Error -> {
        // 错误状态UI
        val errorMessage = (feedsUiState as FeedsUiState.Error).message
        ModernErrorScreen(errorMessage = errorMessage, onRetry = { feedsViewModel.getFeeds() })
    }
}
```

## 6. 最佳实践

### 6.1 Compose组件最佳实践

1. **保持组件简洁**：每个组件只负责一项功能，代码行数不宜过多
2. **使用命名参数**：提高函数调用的可读性
3. **合理使用`remember`**：缓存计算结果，避免不必要的重组
4. **使用`derivedStateOf`**：根据其他状态派生新状态
5. **避免在重组中创建新对象**：将对象创建移到`remember`或组件外部
6. **使用`key`函数**：在列表中为每个项提供唯一键，提高性能

### 6.2 主题设计最佳实践

1. **遵循Material Design规范**：使用Material Design的色彩、排版和组件
2. **提供主题切换**：支持浅色/深色/系统主题
3. **使用主题扩展**：为组件提供主题化的扩展属性
4. **保持色彩一致性**：在整个应用中使用统一的色彩方案
5. **考虑可访问性**：确保文本和背景的对比度符合可访问性标准

### 6.3 导航最佳实践

1. **使用描述性的路由名称**：便于理解和调试
2. **配置合理的过渡动画**：提升用户体验，避免过度动画
3. **处理深层链接**：支持从外部应用直接跳转到特定页面
4. **合理使用`popUpTo`和`launchSingleTop`**：管理导航栈
5. **避免深层嵌套导航**：保持导航结构清晰

### 6.4 性能优化

1. **使用`remember`缓存计算结果**：避免在每次重组时重新计算
2. **使用`LazyColumn`和`LazyRow`**：高效渲染长列表
3. **避免不必要的重组**：使用`shouldUpdate`参数或`remember`优化
4. **合理使用`LaunchedEffect`的键**：仅在必要时重新执行协程
5. **使用`Modifier.animateContentSize`**：平滑过渡大小变化
6. **避免在`compositionLocalOf`中存储可变状态**：可能导致不可预测的行为

## 7. 测试策略

### 7.1 组件测试

使用Compose Testing测试UI组件：

- 测试组件的渲染效果
- 测试用户交互
- 测试状态变化
- 测试主题切换

### 7.2 集成测试

测试页面间的导航和交互：

- 测试页面跳转
- 测试参数传递
- 测试状态共享

## 8. 扩展性设计

### 8.1 组件库设计

- 构建可复用的组件库
- 支持主题定制
- 提供清晰的API文档

### 8.2 主题扩展

- 支持自定义主题
- 提供主题扩展点
- 支持动态主题切换

### 8.3 导航扩展

- 支持动态添加导航路由
- 支持导航拦截
- 支持自定义过渡动画

## 9. 总结

UI模块是ZenFeedApp的重要组成部分，采用Jetpack Compose实现现代化的声明式UI。该模块遵循清晰的架构设计和最佳实践，具有良好的可维护性、可测试性和可扩展性。在开发新组件时，应遵循现有代码风格和设计原则，确保整个应用的UI一致性和高质量。