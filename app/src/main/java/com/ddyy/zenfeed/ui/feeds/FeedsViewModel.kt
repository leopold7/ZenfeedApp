package com.ddyy.zenfeed.ui.feeds

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ddyy.zenfeed.data.CategoryFilterConfig
import com.ddyy.zenfeed.data.Feed
import com.ddyy.zenfeed.data.FeedRepository
import com.ddyy.zenfeed.data.ServerConfig
import com.ddyy.zenfeed.data.SettingsDataStore
import com.ddyy.zenfeed.extension.filterByTitleKeywords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

sealed interface FeedsUiState {
    data class Success(val feeds: List<Feed>, val allFeeds: List<Feed>, val categories: List<String>) : FeedsUiState
    data object Error : FeedsUiState
    data object Loading : FeedsUiState
}

/**
 * Feeds页面的ViewModel
 * 继承AndroidViewModel以获取Application context用于API配置
 */
class FeedsViewModel(application: Application) : AndroidViewModel(application) {

    // 用于存储每个分类列表的滚动位置 <Category, Pair<Index, Offset>>
    val scrollPositions = mutableMapOf<String, Pair<Int, Int>>()
    
    private val feedRepository = FeedRepository.getInstance(application.applicationContext)
    private val settingsDataStore = SettingsDataStore(application.applicationContext)

    var feedsUiState: FeedsUiState by mutableStateOf(FeedsUiState.Loading)
        private set
    
    // 下拉刷新状态
    var isRefreshing: Boolean by mutableStateOf(false)
        private set
    
    // 背景刷新状态（用于在有缓存数据时显示顶部刷新指示器）
    var isBackgroundRefreshing: Boolean by mutableStateOf(false)
        private set
    
    // 新增内容数量状态（用于toast提醒和触发滚动）
    var newContentCount: Int by mutableStateOf(0)
        private set
    
    // 是否需要滚动到顶部的状态（刷新获得新内容时触发）
    var shouldScrollToTop: Boolean by mutableStateOf(false)
        private set
    
    // 刷新完成但没有新内容的状态（用于显示"没有新内容"提示）
    var shouldShowNoNewContent: Boolean by mutableStateOf(false)
        private set
    
    // 网络请求错误消息状态（用于显示toast提示）
    var errorMessage: String by mutableStateOf("")
        private set
    
    // 原始的完整Feed列表
    private var allFeeds: List<Feed> = emptyList()
    
    // 公共访问器，用于获取完整的Feed列表
    val fullFeeds: List<Feed> get() = allFeeds
    
    // 已读文章的标识符集合（使用标题+时间作为唯一标识）
    private val readFeedIds = mutableSetOf<String>()
    
    // 当前选中的分类，空字符串表示显示全部
    var selectedCategory: String by mutableStateOf("")
        private set

    // 当前选中的时间范围（小时）
    var selectedTimeRangeHours: Int by mutableStateOf(24) // 默认24小时
        private set

    // 当前的搜索查询
    var searchQuery: String by mutableStateOf("")
        private set

    // 搜索历史记录
    var searchHistory: List<String> by mutableStateOf(emptyList())
        private set

    // 搜索阈值，用于控制语义搜索的相关性要求
    var searchThreshold: Float by mutableFloatStateOf(0.55f)
    
    // 搜索结果限制数量，默认500，范围1-500
    var searchLimit: Int by mutableIntStateOf(500)

    // 缓存大小状态
    var cacheSize: String by mutableStateOf("0.00 MB")
        private set

    // 首页分组模式
    var groupingMode: String by mutableStateOf("category") // 默认按分类
    
    // 分类过滤配置列表
    var categoryFilterConfigs: List<CategoryFilterConfig> by mutableStateOf(emptyList())
    
    // 是否文章显示图片
    var imageCacheEnabled: Boolean by mutableStateOf(true) // 默认文章显示图片
    
    // 服务器配置列表
    var serverConfigs: List<ServerConfig> by mutableStateOf(emptyList())
        private set
    
    // 标题过滤关键词
    var titleFilterKeywords: String by mutableStateOf("")
        private set

    // 样式配置
    var styleConfig: com.ddyy.zenfeed.data.StyleConfig by mutableStateOf(com.ddyy.zenfeed.data.StyleConfig())
        private set

    // 监听分组模式变化，更新Feed列表
    private val _groupingModeFlow = MutableStateFlow(groupingMode)
    
    // 更新分组模式
    fun updateGroupingMode(mode: String) {
        groupingMode = mode
        _groupingModeFlow.value = mode
    }

    init {
        // 持续监听分组模式变化
        viewModelScope.launch {
            settingsDataStore.homeGroupingMode.collect {
                if (groupingMode != it) {
                    groupingMode = it
                    _groupingModeFlow.value = it
                    Log.d("FeedsViewModel", "分组模式已更新: $it")
                }
            }
        }
        
        // 持续监听分类过滤配置变化
        viewModelScope.launch {
            settingsDataStore.categoryFilterConfigs.collect {
                if (categoryFilterConfigs != it) {
                    categoryFilterConfigs = it
                    updateFilteredFeeds()
                    Log.d("FeedsViewModel", "分类过滤配置已更新: ${it.size} 个配置")
                }
            }
        }
        
        // 持续监听图片缓存设置变化
        viewModelScope.launch {
            settingsDataStore.imageCacheEnabled.collect {
                if (imageCacheEnabled != it) {
                    imageCacheEnabled = it
                    Log.d("FeedsViewModel", "图片缓存设置已更新: $it")
                }
            }
        }
        
        // 监听_groupingModeFlow变化，更新Feed列表
        viewModelScope.launch {
            _groupingModeFlow.collect {
                updateFilteredFeeds()
            }
        }
        
        // 监听已读状态变化
        viewModelScope.launch {
            feedRepository.readStateChanged.collect {
                loadReadFeedIds()
                updateFilteredFeeds()
            }
        }
        
        // 持续监听服务器配置变化
        viewModelScope.launch {
            settingsDataStore.serverConfigs.collect {
                if (serverConfigs != it) {
                    serverConfigs = it
                    Log.d("FeedsViewModel", "服务器配置已更新: ${it.size} 个服务器")
                    // 服务器配置变化时，刷新Feed列表
                    getFeeds(forceRefresh = true)
                }
            }
        }
        
        // 持续监听标题过滤关键词变化
        viewModelScope.launch {
            settingsDataStore.titleFilterKeywords.collect {
                if (titleFilterKeywords != it) {
                    titleFilterKeywords = it
                    updateFilteredFeeds()
                    Log.d("FeedsViewModel", "标题过滤关键词已更新: $it")
                }
            }
        }

        // 持续监听样式配置变化
        viewModelScope.launch {
            settingsDataStore.styleConfig.collect {
                if (styleConfig != it) {
                    styleConfig = it
                    Log.d("FeedsViewModel", "样式配置已更新: $it")
                }
            }
        }

        // 加载初始数据和设置
        viewModelScope.launch {
            // 1. 从DataStore获取初始设置
            val initialConfigs = settingsDataStore.categoryFilterConfigs.first()
            val initialGroupingMode = settingsDataStore.homeGroupingMode.first()
            val initialImageCacheEnabled = settingsDataStore.imageCacheEnabled.first()
            val initialServerConfigs = settingsDataStore.serverConfigs.first()
            val initialTitleFilterKeywords = settingsDataStore.titleFilterKeywords.first()
            val initialStyleConfig = settingsDataStore.styleConfig.first()

            categoryFilterConfigs = initialConfigs
            groupingMode = initialGroupingMode
            imageCacheEnabled = initialImageCacheEnabled
            serverConfigs = initialServerConfigs
            titleFilterKeywords = initialTitleFilterKeywords
            styleConfig = initialStyleConfig
            _groupingModeFlow.value = initialGroupingMode

            Log.d("FeedsViewModel", "初始分类过滤配置已加载: ${initialConfigs.size} 个配置")
            Log.d("FeedsViewModel", "初始分组模式已加载: $initialGroupingMode")
            Log.d("FeedsViewModel", "初始图片缓存设置已加载: $initialImageCacheEnabled")
            Log.d("FeedsViewModel", "初始服务器配置已加载: ${initialServerConfigs.size} 个服务器")
            Log.d("FeedsViewModel", "初始标题过滤关键词已加载: $initialTitleFilterKeywords")
            Log.d("FeedsViewModel", "初始样式配置已加载: $initialStyleConfig")

            loadReadFeedIds()
            loadSearchHistory()
            loadCachedFeeds()
            updateCacheSize()
            getFeeds()
        }
    }
    
    /**
     * 更新分类过滤配置中某个分类的设置
     */
    fun updateCategoryConfig(categoryName: String, showInAll: Boolean? = null, showGroup: Boolean? = null) {
        val updatedConfigs = categoryFilterConfigs.map { config ->
            if (config.categoryName == categoryName) {
                config.copy(
                    showInAll = showInAll ?: config.showInAll,
                    showGroup = showGroup ?: config.showGroup
                )
            } else {
                config
            }
        }
        categoryFilterConfigs = updatedConfigs
    }
    
    /**
     * 保存分类过滤配置
     */
    fun saveCategoryFilterConfigs() {
        viewModelScope.launch {
            settingsDataStore.saveCategoryFilterConfigs(categoryFilterConfigs)
            Log.d("FeedsViewModel", "分类过滤配置已保存: ${categoryFilterConfigs.size} 个配置")
        }
    }

    /**
     * 从持久化存储加载已读文章ID集合
     */
    private fun loadReadFeedIds() {
        val persistedReadIds = feedRepository.getReadFeedIds()
        Log.d("FeedsViewModel", "从持久化存储加载已读状态，数量: ${persistedReadIds.size}")
        readFeedIds.clear()
        readFeedIds.addAll(persistedReadIds)
        Log.d("FeedsViewModel", "已加载已读状态，共 ${readFeedIds.size} 条")
    }
    
    /**
     * 加载缓存的Feed列表（应用启动时调用）
     */
    private fun loadCachedFeeds() {
        viewModelScope.launch {
            feedsUiState = FeedsUiState.Loading
            val cachedFeeds = feedRepository.getCachedFeeds()
            if (cachedFeeds != null && cachedFeeds.isNotEmpty()) {
                allFeeds = cachedFeeds
                updateFilteredFeeds()
                Log.d("FeedsViewModel", "已加载缓存数据，共 ${cachedFeeds.size} 条")
            }
        }
    }
    
    /**
     * 获取Feed列表（从网络获取新数据）
     */
    fun getFeeds(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            // 清除之前的错误消息状态
            errorMessage = ""
            
            // 如果当前没有数据或强制刷新，显示加载状态
            if (allFeeds.isEmpty() || forceRefresh) {
                feedsUiState = FeedsUiState.Loading
            } else {
                // 如果有缓存数据，显示背景刷新状态
                isBackgroundRefreshing = true
            }

            val result = feedRepository.getFeeds(useCache = !forceRefresh, hours = selectedTimeRangeHours, query = searchQuery, threshold = if (searchQuery.isNotEmpty()) searchThreshold else null, limit = searchLimit)
            if (result.isSuccess) {
                val feedResponse = result.getOrNull()
                val newFeeds = feedResponse?.feeds ?: emptyList()
                // 检查是否有网络错误信息（即使请求成功返回了缓存数据）
                if (feedResponse?.error != null) {
                    errorMessage = feedResponse.error
                }
                if (newFeeds != allFeeds || forceRefresh) { // 数据不同或强制刷新时更新
                    allFeeds = newFeeds
                    updateFilteredFeeds()
                    Log.d("FeedsViewModel", "已更新网络数据，共 ${newFeeds.size} 条")
                }
            } else {
                // 网络请求失败，设置错误消息状态
                errorMessage = result.exceptionOrNull()?.message ?: "网络请求失败"
                
                // 如果网络获取失败但没有缓存数据，显示错误状态
                if (allFeeds.isEmpty()) {
                    feedsUiState = FeedsUiState.Error
                }
            }

            // 关闭背景刷新状态
            isBackgroundRefreshing = false
        }
    }
    
    /**
     * 下拉刷新获取Feed列表
     */
    fun refreshFeeds() {
        viewModelScope.launch {
            // 清除之前的错误消息状态
            errorMessage = ""
            isRefreshing = true
            val result = feedRepository.getFeeds(useCache = false, hours = selectedTimeRangeHours, query = searchQuery, threshold = if (searchQuery.isNotEmpty()) searchThreshold else null, limit = searchLimit) // 强制从网络获取
            if (result.isSuccess) {
                val feedResponse = result.getOrNull()
                val newFeeds = feedResponse?.feeds ?: emptyList()
                
                // 检查是否有网络错误信息（即使请求成功返回了缓存数据）
                if (feedResponse?.error != null) {
                    errorMessage = feedResponse.error
                }

                // 检测新增内容数量
                val newContentCount = detectNewContent(allFeeds, newFeeds)

                // 更新Feed列表
                allFeeds = newFeeds
                updateFilteredFeeds()

                // 如果有新内容，设置滚动到顶部状态和新增数量
                if (newContentCount > 0) {
                    this@FeedsViewModel.newContentCount = newContentCount
                    shouldScrollToTop = true
                    shouldShowNoNewContent = false
                    Log.d("FeedsViewModel", "刷新完成，发现 $newContentCount 条新内容，总共 ${newFeeds.size} 条")
                } else {
                    // 没有新内容时，设置显示"没有新内容"提示的状态
                    shouldShowNoNewContent = true
                    Log.d("FeedsViewModel", "刷新完成，无新内容，总共 ${newFeeds.size} 条")
                }
            } else {
                // 刷新失败，设置错误消息状态
                errorMessage = result.exceptionOrNull()?.message ?: "刷新失败"
                
                // 刷新失败时保持当前数据不变
                if (allFeeds.isEmpty()) {
                    feedsUiState = FeedsUiState.Error
                }
            }
            isRefreshing = false
        }
    }
    
    /**
     * 选择分类进行筛选
     */
    fun selectCategory(category: String) {
        selectedCategory = category
        // 这里不再调用 updateFilteredFeeds()，因为UI层会处理筛选
    }

    /**
     * 选择时间范围
     */
    fun selectTimeRange(hours: Int) {
        if (selectedTimeRangeHours != hours) {
            selectedTimeRangeHours = hours
            // 强制刷新数据
            getFeeds(forceRefresh = true)
            Log.d("FeedsViewModel", "时间范围已更改为: $hours 小时")
        }
    }

    /**
     * 搜索Feeds
     */
    fun searchFeeds(query: String) {
        if (searchQuery != query || query.isNotEmpty()) { // 允许重复搜索相同内容
            searchQuery = query
            addSearchHistory(query) // 添加到历史记录
            getFeeds(forceRefresh = true)
            Log.d("FeedsViewModel", "开始搜索: $query, 阈值: $searchThreshold")
        }
    }


    /**
     * 添加到搜索历史
     */
    private fun addSearchHistory(query: String) {
        if (query.isBlank()) return
        val newHistory = searchHistory.toMutableList()
        newHistory.remove(query) // 移除旧的重复项
        newHistory.add(0, query) // 添加到最前面
        searchHistory = newHistory.take(10) // 最多保存10条
        feedRepository.saveSearchHistory(searchHistory)
        Log.d("FeedsViewModel", "添加搜索历史: $query, 当前历史: $searchHistory")
    }

    /**
     * 清除搜索历史
     */
    fun clearSearchHistory() {
        searchHistory = emptyList()
        feedRepository.saveSearchHistory(emptyList())
        Log.d("FeedsViewModel", "搜索历史已清除")
    }

    /**
     * 更新缓存大小显示
     */
    fun updateCacheSize() {
        val sizeInBytes = feedRepository.getCacheSize()
        cacheSize = formatCacheSize(sizeInBytes)
        Log.d("FeedsViewModel", "缓存大小已更新: $cacheSize")
    }

    /**
     * 清理缓存
     */
    fun clearCache() {
        viewModelScope.launch {
            feedRepository.clearCache()
            updateCacheSize()
            // 清理缓存后，可能需要重新加载数据或重置状态
            allFeeds = emptyList()
            readFeedIds.clear()
            getFeeds(forceRefresh = true)
            Log.d("FeedsViewModel", "缓存已清理，并重新加载数据")
        }
    }

    /**
     * 格式化缓存大小
     */
    private fun formatCacheSize(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1 -> String.format(Locale.US, "%.2f MB", mb)
            kb >= 1 -> String.format(Locale.US, "%.2f KB", kb)
            else -> String.format(Locale.US, "%d B", bytes)
        }
    }

    /**
     * 加载搜索历史
     */
    private fun loadSearchHistory() {
        searchHistory = feedRepository.getSearchHistory()
        Log.d("FeedsViewModel", "已加载搜索历史: $searchHistory")
    }
    
    /**
     * 标记文章为已读
     */
    fun markFeedAsRead(feed: Feed) {
        val feedId = "${feed.labels.title ?: ""}-${feed.time}-${feed.serverId ?: ""}"
        if (!readFeedIds.contains(feedId)) {
            readFeedIds.add(feedId)
            // 立即持久化到存储
            viewModelScope.launch {
                feedRepository.addReadFeedId(feedId)
            }
            updateFilteredFeeds() // 重新更新UI以反映阅读状态变化
            Log.d("FeedsViewModel", "标记文章为已读: ${feed.labels.title ?: "未知标题"}")
        }
    }
    
    /**
     * 标记文章为未读
     */
    fun markFeedAsUnread(feed: Feed) {
        val feedId = "${feed.labels.title ?: ""}-${feed.time}-${feed.serverId ?: ""}"
        if (readFeedIds.contains(feedId)) {
            readFeedIds.remove(feedId)
            // 立即从持久化存储中移除
            viewModelScope.launch {
                feedRepository.removeReadFeedId(feedId)
            }
            updateFilteredFeeds() // 重新更新UI以反映阅读状态变化
            Log.d("FeedsViewModel", "标记文章为未读: ${feed.labels.title ?: "未知标题"}")
        }
    }
    
    /**
     * 检测新增内容数量
     * 通过比较新旧Feed列表，找出真正新增的内容
     */
    private fun detectNewContent(oldFeeds: List<Feed>, newFeeds: List<Feed>): Int {
        if (oldFeeds.isEmpty()) return 0 // 初次加载不算新增
        
        // 创建旧Feed的唯一标识符集合（使用标题+时间+serverId作为唯一标识）
        val oldFeedIds = oldFeeds.map { "${it.labels.title ?: ""}-${it.time}-${it.serverId ?: ""}" }.toSet()
        
        // 计算新Feed中不在旧Feed集合中的数量
        val newContentCount = newFeeds.count { feed ->
            val feedId = "${feed.labels.title ?: ""}-${feed.time}-${feed.serverId ?: ""}"
            !oldFeedIds.contains(feedId)
        }
        
        return newContentCount
    }
    
    /**
     * 清除滚动到顶部状态（UI处理完滚动后调用）
     */
    fun clearScrollToTopState() {
        shouldScrollToTop = false
        newContentCount = 0
    }
    
    /**
     * 清除"没有新内容"提示状态（UI显示完提示后调用）
     */
    fun clearNoNewContentState() {
        shouldShowNoNewContent = false
    }
    
    /**
     * 清除错误消息状态（UI显示完错误提示后调用）
     */
    fun clearErrorMessage() {
        errorMessage = ""
    }
    
    /**
     * 检查文章是否已读
     */
    private fun isFeedRead(feed: Feed): Boolean {
        val feedId = "${feed.labels.title ?: ""}-${feed.time}-${feed.serverId ?: ""}"
        val isRead = readFeedIds.contains(feedId)
        return isRead
    }
    
    /**
     * 更新筛选后的Feed列表
     */
    private fun updateFilteredFeeds() {
        val feedsWithReadStatus = allFeeds
            .map { feed ->
                val isRead = isFeedRead(feed)
                feed.copy(isRead = isRead)
            }
            .sortedWith(compareByDescending<Feed> {
                parseTimeToLong(it.time)
            }.thenBy {
                it.labels.title ?: "未知标题"
            })
        
        val feedsWithTitleFilter = feedsWithReadStatus.filter { feed ->
            feed.filterByTitleKeywords(titleFilterKeywords)
        }
        
        fun getConfigForCategory(category: String?): CategoryFilterConfig? {
            if (category == null) return null
            return categoryFilterConfigs.find { it.categoryName == category }
        }
        
        fun shouldShowGroup(group: String): Boolean {
            val config = getConfigForCategory(group)
            val result = config?.showGroup ?: true
            return result
        }
        
        fun shouldIncludeFeedInAll(feed: Feed): Boolean {
            val categoryConfig = getConfigForCategory(feed.labels.category)
            val sourceConfig = feed.labels.source?.let { getConfigForCategory(it) }
            
            val categoryShowInAll = categoryConfig?.showInAll ?: true
            val sourceShowInAll = sourceConfig?.showInAll ?: true
            
            val result = categoryShowInAll && sourceShowInAll
            return result
        }
        
        val filteredAllFeeds = feedsWithReadStatus.filter { feed ->
            shouldIncludeFeedInAll(feed) && feed.filterByTitleKeywords(titleFilterKeywords)
        }
        
        // 计算每个分类下的文章数量
        val categoryArticleCounts = mutableMapOf<String, Int>()
        allFeeds.forEach { feed ->
            // 统计分类文章数量
            feed.labels.category?.let {
                categoryArticleCounts[it] = categoryArticleCounts.getOrDefault(it, 0) + 1
            }
            // 统计来源文章数量
            feed.labels.source?.let {
                categoryArticleCounts[it] = categoryArticleCounts.getOrDefault(it, 0) + 1
            }
        }
        
        // 辅助函数：按sortOrder和名称排序，sortOrder=0的移动到末尾
        fun sortCategories(categories: List<String>): List<String> {
            return categories.sortedWith(
                compareBy<String> { category ->
                    // sortOrder=0的移动到末尾，其他按sortOrder排序
                    val sortOrder = getConfigForCategory(category)?.sortOrder ?: 0
                    if (sortOrder == 0) Int.MAX_VALUE else sortOrder
                }.thenBy { it }
            )
        }
        
        // 辅助函数：判断一个名称是否是分类（而不是来源）
        fun isCategory(name: String): Boolean {
            return allFeeds.any { it.labels.category == name }
        }
        
        // 辅助函数：合并并排序categories和sources
        fun sortCategoriesAndSources(categoryList: List<String>, sourceList: List<String>): List<String> {
            val allItems = categoryList + sourceList
            
            // 检查是否所有项目的sortOrder都是0
            val allZeroSortOrder = allItems.all { 
                (getConfigForCategory(it)?.sortOrder ?: 0) == 0 
            }
            
            if (allZeroSortOrder) {
                // 所有sortOrder都是0，按先分类后来源，再按名称排序
                return allItems.sortedWith(
                    compareBy<String> { item ->
                        // 分类在前，来源在后
                        if (isCategory(item)) 0 else 1
                    }.thenBy { it }
                )
            } else {
                // 有部分sortOrder>0，按sortOrder排序，未排序的放到后面
                return allItems.sortedWith(
                    compareBy<String> { item ->
                        val sortOrder = getConfigForCategory(item)?.sortOrder ?: 0
                        // sortOrder=0的放到后面
                        if (sortOrder == 0) Int.MAX_VALUE else sortOrder
                    }.thenBy { item ->
                        // 相同sortOrder的，分类在前，来源在后
                        if (isCategory(item)) 0 else 1
                    }.thenBy { it }
                )
            }
        }
        
        val categories = when (groupingMode) {
            "source" -> {
                allFeeds
                    .mapNotNull { it.labels.source }
                    .filter { shouldShowGroup(it) && categoryArticleCounts.getOrDefault(it, 0) > 0 }
                    .distinct()
                    .let { sortCategories(it) }
            }
            "category,source" -> {
                val categoryList = allFeeds
                    .mapNotNull { it.labels.category }
                    .filter { shouldShowGroup(it) && categoryArticleCounts.getOrDefault(it, 0) > 0 }
                    .distinct()
                
                val sourceList = allFeeds
                    .mapNotNull { it.labels.source }
                    .filter { shouldShowGroup(it) && categoryArticleCounts.getOrDefault(it, 0) > 0 && it !in categoryList }
                    .distinct()
                
                // 统一排序，不再确保分类在前，来源在后
                sortCategoriesAndSources(categoryList, sourceList)
            }
            "none" -> {
                emptyList()
            }
            else -> {
                allFeeds
                    .mapNotNull { it.labels.category }
                    .filter { shouldShowGroup(it) && categoryArticleCounts.getOrDefault(it, 0) > 0 }
                    .distinct()
                    .let { sortCategories(it) }
            }
        }
        
        feedsUiState = FeedsUiState.Success(
            feeds = feedsWithTitleFilter,
            allFeeds = filteredAllFeeds,
            categories = categories
        )
        
        Log.d("FeedsViewModel", "Feed列表已按 $groupingMode 分组，总文章数=${feedsWithReadStatus.size}, 全部分组文章数=${filteredAllFeeds.size}, 分类数=${categories.size}")
    }
    
    /**
     * 解析时间字符串为长整型时间戳，支持多种格式包括纳秒和时区
     */
    @OptIn(ExperimentalTime::class)
    private fun parseTimeToLong(timeString: String): Long {
        return try {
            // kotlinx-datetime 的 Instant.parse() 可以自动处理各种 ISO 8601 格式
            // 包括纳秒精度和时区信息，如: 2025-08-11T08:14:51.583598089+08:00
            Instant.parse(timeString).toEpochMilliseconds()
        } catch (_: Exception) {
            Log.d("FeedsViewModel", "kotlinx-datetime 解析失败: $timeString, 尝试备用方案")
            
            // 备用方案1：尝试使用 Android Time 类
            try {
                val time = android.text.format.Time()
                if (time.parse3339(timeString)) {
                    val millis = time.toMillis(false)
                    Log.d("FeedsViewModel", "时间解析成功: $timeString -> $millis (使用 Time.parse3339)")
                    return millis
                }
            } catch (_: Exception) {
                Log.d("FeedsViewModel", "Time.parse3339 也解析失败: $timeString")
            }
            
            // 备用方案2：基于数字提取的排序值
            try {
                val cleanTime = timeString.replace(Regex("[^\\d]"), "").take(14)
                val result = if (cleanTime.length >= 8) {
                    cleanTime.toLongOrNull() ?: timeString.hashCode().toLong()
                } else {
                    timeString.hashCode().toLong()
                }
                Log.d("FeedsViewModel", "时间解析使用数字提取: $timeString -> 清理后: $cleanTime -> 结果: $result")
                result
            } catch (_: Exception) {
                val hashResult = timeString.hashCode().toLong()
                Log.w("FeedsViewModel", "时间解析最终使用散列值: $timeString -> $hashResult")
                hashResult
            }
        }
    }
}