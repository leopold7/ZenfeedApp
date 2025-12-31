# 数据模块指南

## 1. 模块概述

数据模块是ZenFeedApp的核心组件之一，负责数据的获取、存储和处理。该模块采用Repository模式设计，提供统一的数据访问接口，封装了数据来源（网络、本地存储）的实现细节。

## 2. 核心组件

### 2.1 FeedRepository

**职责**：
- 管理Feed数据的获取和缓存
- 处理网络请求和本地存储的交互
- 提供统一的数据访问接口

**主要功能**：

| 功能 | 方法签名 | 说明 |
|------|----------|------|
| 获取Feed列表 | `suspend fun getFeeds(useCache: Boolean = true, hours: Int = 24, query: String = "", threshold: Float? = null, limit: Int = 500): Result<FeedResponse>` | 从网络或缓存获取Feed列表 |
| 获取缓存的Feed | `fun getCachedFeeds(): List<Feed>?` | 从本地缓存获取Feed列表 |
| 缓存Feed数据 | `private fun cacheFeeds(feeds: List<Feed>)` | 将Feed数据缓存到本地 |
| 检查缓存是否有效 | `private fun isCacheValid(): Boolean` | 检查缓存是否过期 |
| 获取缓存大小 | `fun getCacheSize(): Long` | 计算并返回总缓存大小 |
| 清除缓存 | `fun clearCache()` | 清除所有类型的缓存 |
| 保存已读文章ID | `fun saveReadFeedIds(readFeedIds: Set<String>)` | 保存已读文章ID集合 |
| 获取已读文章ID | `fun getReadFeedIds(): Set<String>` | 获取已读文章ID集合 |
| 添加已读文章ID | `fun addReadFeedId(feedId: String)` | 将文章标记为已读 |
| 移除已读文章ID | `fun removeReadFeedId(feedId: String)` | 将文章标记为未读 |
| 检查文章是否已读 | `fun isFeedRead(feedId: String): Boolean` | 检查文章是否已读 |
| 检查应用更新 | `suspend fun checkForUpdate(): GithubRelease?` | 检查是否有新版本发布 |

**使用示例**：

```kotlin
// 创建Repository实例
val feedRepository = FeedRepository(context)

// 获取Feed列表
suspend fun fetchFeeds() {
    val result = feedRepository.getFeeds(hours = 24, limit = 100)
    when (result) {
        is Result.Success -> {
            val feeds = result.value.feeds
            // 处理成功获取的Feed数据
        }
        is Result.Failure -> {
            val error = result.exception
            // 处理获取失败的情况
        }
    }
}

// 标记文章为已读
feedRepository.addReadFeedId(feedId)

// 检查文章是否已读
val isRead = feedRepository.isFeedRead(feedId)
```

### 2.2 SettingsDataStore

**职责**：
- 使用DataStore存储用户偏好设置
- 提供设置的读取和保存接口
- 支持设置的重置和验证

**主要功能**：

| 功能 | 方法签名 | 说明 |
|------|----------|------|
| 获取API基础地址 | `val apiBaseUrl: Flow<String>` | 以Flow形式获取API基础地址 |
| 获取后端URL | `val backendUrl: Flow<String>` | 以Flow形式获取后端URL |
| 获取代理设置 | `val proxyEnabled: Flow<Boolean>`, `val proxyType: Flow<String>`, 等 | 以Flow形式获取代理相关设置 |
| 获取主题模式 | `val themeMode: Flow<String>` | 以Flow形式获取主题模式 |
| 保存API基础地址 | `suspend fun saveApiBaseUrl(url: String)` | 保存API基础地址 |
| 保存后端URL | `suspend fun saveBackendUrl(url: String)` | 保存后端URL |
| 保存代理设置 | `suspend fun saveProxySettings(enabled: Boolean, type: String, host: String, port: Int, username: String, password: String)` | 保存完整的代理设置 |
| 保存主题模式 | `suspend fun saveThemeMode(mode: String)` | 保存主题模式 |
| 重置所有设置 | `suspend fun resetAllSettings()` | 将所有设置重置为默认值 |
| 验证URL格式 | `fun isValidUrl(url: String): Boolean` | 验证URL格式是否有效 |
| 格式化URL | `fun formatUrl(url: String): String` | 格式化URL，确保以/结尾 |

**使用示例**：

```kotlin
// 创建SettingsDataStore实例
val settingsDataStore = SettingsDataStore(context)

// 监听API基础地址变化
LaunchedEffect(Unit) {
    settingsDataStore.apiBaseUrl.collect {apiUrl ->
        // 处理API地址变化
    }
}

// 保存API基础地址
suspend fun updateApiUrl(newUrl: String) {
    settingsDataStore.saveApiBaseUrl(newUrl)
}

// 验证URL格式
val isValid = settingsDataStore.isValidUrl(url)
```

### 2.3 数据模型

**主要数据模型**：

| 模型类 | 说明 | 主要字段 |
|--------|------|----------|
| `FeedResponse` | Feed响应数据 | `summary`、`feeds`、`count`、`error` |
| `Feed` | Feed数据项 | `labels`、`time`、`score`、`isRead` |
| `Labels` | Feed的元数据标签 | `category`、`content`、`link`、`podcastUrl`、`title`、`type`等 |
| `FeedRequest` | Feed请求参数 | `query`、`threshold`、`labelFilters`、`summarize`、`limit`、`start`、`end` |
| `PlaylistInfo` | 播放列表信息 | `currentIndex`、`totalCount`、`hasNext`、`hasPrevious`、`isRepeat`、`isShuffle` |

## 3. 数据流程

### 3.1 Feed数据获取流程

```
ViewModel → FeedRepository.getFeeds() → 检查缓存是否有效 → 
    是 → 返回缓存数据 → 
    否 → 构建网络请求 → 发送网络请求 → 处理响应 → 缓存数据 → 返回数据 → ViewModel → UI
```

### 3.2 设置数据流程

```
UI → ViewModel → SettingsDataStore.saveXXX() → DataStore → 持久化存储
DataStore → SettingsDataStore.XXX → Flow → ViewModel → UI
```

## 4. 缓存策略

### 4.1 缓存机制

- **缓存类型**：使用SharedPreferences存储Feed数据和元信息
- **缓存过期时间**：默认1小时
- **缓存键**：
  - `cached_feeds`：存储Feed列表
  - `cache_timestamp`：存储缓存时间戳
  - `read_feeds`：存储已读文章ID集合
  - `search_history`：存储搜索历史记录

### 4.2 缓存验证

```kotlin
private fun isCacheValid(): Boolean {
    val cacheTimestamp = sharedPreferences.getLong(CACHE_KEY_TIMESTAMP, 0)
    val currentTime = System.currentTimeMillis()
    val cacheAge = currentTime - cacheTimestamp
    val cacheExpiryTime = CACHE_EXPIRY_HOURS * 60 * 60 * 1000 // 转换为毫秒

    return cacheAge < cacheExpiryTime
}
```

## 5. 网络请求

### 5.1 ApiClient

**职责**：
- 创建和配置Retrofit实例
- 处理网络请求的拦截器和配置
- 支持动态API地址配置

**主要功能**：
- 创建Retrofit实例
- 配置OkHttp客户端
- 添加请求日志拦截器
- 支持代理配置

### 5.2 ApiService

**职责**：
- 定义API请求接口
- 支持动态URL
- 处理网络响应

**主要API接口**：

| 接口 | 方法签名 | 说明 |
|------|----------|------|
| 获取Feed列表 | `@POST fun getFeeds(@Url backendUrl: String, @Body body: FeedRequest): Response<FeedResponse>` | 动态URL获取Feed列表 |
| 获取GitHub最新发布 | `@GET fun getLatestRelease(@Url url: String): Response<GithubRelease>` | 获取GitHub最新发布信息 |

## 6. 最佳实践

### 6.1 使用Repository的最佳实践

1. **统一数据访问**：始终通过Repository访问数据，避免直接访问网络或本地存储
2. **合理使用缓存**：根据业务需求选择是否使用缓存
3. **处理异常情况**：妥善处理网络请求失败、缓存过期等异常情况
4. **使用协程处理异步操作**：Repository中的异步方法应使用`suspend`关键字，并在ViewModel中使用协程调用

### 6.2 使用SettingsDataStore的最佳实践

1. **使用Flow监听数据变化**：对于需要实时响应变化的设置，使用Flow监听
2. **在Compose中使用collectAsState**：在Compose组件中使用`collectAsState`将Flow转换为可组合状态
3. **验证输入数据**：在保存设置前，使用`isValidUrl`等方法验证输入数据的格式
4. **使用默认值**：为所有设置项提供合理的默认值

### 6.3 数据模型设计最佳实践

1. **使用不可变数据类**：数据模型应使用Kotlin的`data class`定义，且字段尽可能使用`val`（不可变）
2. **使用SerializedName注解**：对于需要序列化/反序列化的数据模型，使用`@SerializedName`注解指定字段映射关系
3. **添加计算属性**：对于需要动态计算的字段，使用计算属性（如`Feed`类中的`formattedTime`和`formattedTimeShort`）
4. **处理空值**：合理使用空安全特性，为可能为空的字段添加`?`标记

## 7. 测试策略

### 7.1 单元测试

- 测试Repository的核心逻辑，如缓存管理、数据转换等
- 使用MockK或Mockito模拟网络请求和本地存储
- 测试各种边界情况，如空数据、网络错误等

### 7.2 集成测试

- 测试Repository与网络层、存储层的交互
- 测试完整的数据流程

## 8. 扩展性设计

- **支持多种数据源**：Repository模式便于添加新的数据源（如数据库）
- **动态API配置**：支持运行时修改API地址，便于切换环境
- **可配置的缓存策略**：缓存过期时间等参数可配置

## 9. 总结

数据模块是ZenFeedApp的重要组成部分，采用了现代化的设计模式和技术栈。该模块提供了统一的数据访问接口，封装了数据来源的实现细节，便于上层组件使用。在开发过程中，应遵循Repository模式的设计原则，合理使用缓存和网络请求，确保数据的一致性和可靠性。