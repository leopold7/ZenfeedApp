package com.ddyy.zenfeed.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ddyy.zenfeed.data.CategoryFilterConfig
import com.ddyy.zenfeed.data.ServerConfig
import com.ddyy.zenfeed.data.SettingsDataStore
import com.ddyy.zenfeed.data.StyleConfig
import com.ddyy.zenfeed.data.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 设置页面的UI状态
 */
data class SettingsUiState(
    val apiUrl: String = "",
    val backendUrl: String = "",
    val proxyEnabled: Boolean = false,
    val proxyType: String = "HTTP",
    val proxyHost: String = "",
    val proxyPort: Int = 8080,
    val proxyUsername: String = "",
    val proxyPassword: String = "",
    val themeMode: String = "system",
    val checkUpdateOnStart: Boolean = true,
    val updateBranch: String = SettingsDataStore.DEFAULT_UPDATE_BRANCH,
    val homeGroupingMode: String = "category",
    val categoryFilterConfigs: List<CategoryFilterConfig> = emptyList(),
    val imageCacheEnabled: Boolean = true,
    val aiApiUrl: String = "",
    val aiApiKey: String = "",
    val aiModelName: String = "",
    val aiPrompt: String = "",
    val serverConfigs: List<ServerConfig> = emptyList(),
    val markPodcastAsRead: Boolean = true,
    val playbackSpeed: Float = 1.0f,
    val blogAutoDownloadToLocal: Boolean = false,
    val titleFilterKeywords: String = "",
    val styleConfig: StyleConfig = StyleConfig(),
    val isLoading: Boolean = false,
    val message: String = "")

/**
 * 设置页面的ViewModel
 * 管理设置页面的状态和业务逻辑
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val settingsDataStore = SettingsDataStore(application.applicationContext)
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    private var currentInputApiUrl = ""
    private var currentInputBackendUrl = ""
    private var currentProxyEnabled = false
    private var currentInputProxyType = "HTTP"
    private var currentInputProxyHost = ""
    private var currentInputProxyPort = 8080
    private var currentInputProxyUsername = ""
    private var currentInputProxyPassword = ""
    private var currentThemeMode = "system"
    private var currentCheckUpdateOnStart = true
    private var currentUpdateBranch = SettingsDataStore.DEFAULT_UPDATE_BRANCH
    private var currentHomeGroupingMode = "category"
    private var currentCategoryFilterConfigs = mutableListOf<CategoryFilterConfig>()
    private var currentImageCacheEnabled = true
    private var currentAiApiUrl = ""
    private var currentAiApiKey = ""
    private var currentAiModelName = ""
    private var currentAiPrompt = ""
    private var currentMarkPodcastAsRead = true
    private var currentPlaybackSpeed = 1.0f
    private var currentBlogAutoDownloadToLocal = false
    private var currentTitleFilterKeywords = ""
    private var currentStyleConfig = StyleConfig()

    init {
        // 初始化时加载当前设置
        loadCurrentSettings()
    }
    
    /**
     * 加载当前设置
     */
    private fun loadCurrentSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // 加载所有设置
            settingsDataStore.apiBaseUrl.collect { apiUrl ->
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val backendUrl = settingsDataStore.backendUrl.first()
                val proxyEnabled = settingsDataStore.proxyEnabled.first()
                val proxyType = settingsDataStore.proxyType.first()
                val proxyHost = settingsDataStore.proxyHost.first()
                val proxyPort = settingsDataStore.proxyPort.first()
                val proxyUsername = settingsDataStore.proxyUsername.first()
                val proxyPassword = settingsDataStore.proxyPassword.first()
                val themeMode = settingsDataStore.themeMode.first()
                val checkUpdateOnStart = settingsDataStore.checkUpdateOnStart.first()
                val updateBranch = settingsDataStore.updateBranch.first()
                val homeGroupingMode = settingsDataStore.homeGroupingMode.first()
                val categoryFilterConfigs = settingsDataStore.categoryFilterConfigs.first()
                val imageCacheEnabled = settingsDataStore.imageCacheEnabled.first()
                val serverConfigs = settingsDataStore.serverConfigs.first()
                val aiApiUrl = settingsDataStore.aiApiUrl.first()
                val aiApiKey = settingsDataStore.aiApiKey.first()
                val aiModelName = settingsDataStore.aiModelName.first()
                val aiPrompt = settingsDataStore.aiPrompt.first()
                val markPodcastAsRead = settingsDataStore.markPodcastAsRead.first()
                val playbackSpeed = settingsDataStore.playbackSpeed.first()
                val blogAutoDownloadToLocal = settingsDataStore.blogAutoDownloadToLocal.first()
                val titleFilterKeywords = settingsDataStore.titleFilterKeywords.first()
                val styleConfig = settingsDataStore.styleConfig.first()

                _uiState.value = _uiState.value.copy(
                    apiUrl = apiUrl,
                    backendUrl = backendUrl,
                    proxyEnabled = proxyEnabled,
                    proxyType = proxyType,
                    proxyHost = proxyHost,
                    proxyPort = proxyPort,
                    proxyUsername = proxyUsername,
                    proxyPassword = proxyPassword,
                    themeMode = themeMode,
                        checkUpdateOnStart = checkUpdateOnStart,
                        updateBranch = updateBranch,
                        homeGroupingMode = homeGroupingMode,
                    categoryFilterConfigs = categoryFilterConfigs,
                    imageCacheEnabled = imageCacheEnabled,
                    serverConfigs = serverConfigs,
                    aiApiUrl = aiApiUrl,
                    aiApiKey = aiApiKey,
                    aiModelName = aiModelName,
                    aiPrompt = aiPrompt,
                    markPodcastAsRead = markPodcastAsRead,
                    playbackSpeed = playbackSpeed,
                    blogAutoDownloadToLocal = blogAutoDownloadToLocal,
                    titleFilterKeywords = titleFilterKeywords,
                    styleConfig = styleConfig,
                    isLoading = false
                )

                // 更新当前分组模式
                currentHomeGroupingMode = homeGroupingMode
                currentCategoryFilterConfigs = categoryFilterConfigs.toMutableList()
                currentImageCacheEnabled = imageCacheEnabled

                if (currentInputApiUrl.isEmpty()) {
                    currentInputApiUrl = apiUrl
                }
                if (currentInputBackendUrl.isEmpty()) {
                    currentInputBackendUrl = backendUrl
                }
                currentProxyEnabled = proxyEnabled
                if (currentInputProxyType.isEmpty()) {
                    currentInputProxyType = proxyType
                }
                if (currentInputProxyHost.isEmpty()) {
                    currentInputProxyHost = proxyHost
                }
                currentInputProxyPort = proxyPort
                if (currentInputProxyUsername.isEmpty()) {
                    currentInputProxyUsername = proxyUsername
                }
                if (currentInputProxyPassword.isEmpty()) {
                    currentInputProxyPassword = proxyPassword
                }
                if (currentAiApiUrl.isEmpty()) {
                    currentAiApiUrl = aiApiUrl
                }
                if (currentAiApiKey.isEmpty()) {
                    currentAiApiKey = aiApiKey
                }
                if (currentAiModelName.isEmpty()) {
                    currentAiModelName = aiModelName
                }
                if (currentAiPrompt.isEmpty()) {
                    currentAiPrompt = aiPrompt
                }
                currentMarkPodcastAsRead = markPodcastAsRead
                currentPlaybackSpeed = playbackSpeed
                currentBlogAutoDownloadToLocal = blogAutoDownloadToLocal
                currentTitleFilterKeywords = titleFilterKeywords
                currentStyleConfig = styleConfig
                currentThemeMode = themeMode
                currentCheckUpdateOnStart = checkUpdateOnStart
            }
        }
    }
    
    /**
     * 更新输入的API地址
     * @param url 用户输入的API地址
     */
    fun updateApiUrl(url: String) {
        currentInputApiUrl = url
    }
    
    /**
     * 更新输入的后端URL
     * @param url 用户输入的后端URL
     */
    fun updateBackendUrl(url: String) {
        currentInputBackendUrl = url
    }
    
    /**
     * 更新代理启用状态
     */
    fun updateProxyEnabled(enabled: Boolean) {
        currentProxyEnabled = enabled
    }
    
    /**
     * 更新代理类型
     */
    fun updateProxyType(type: String) {
        currentInputProxyType = type
    }
    
    /**
     * 更新代理主机地址
     */
    fun updateProxyHost(host: String) {
        currentInputProxyHost = host
    }
    
    /**
     * 更新代理端口
     */
    fun updateProxyPort(port: Int) {
        currentInputProxyPort = port
    }
    
    /**
     * 更新代理用户名
     */
    fun updateProxyUsername(username: String) {
        currentInputProxyUsername = username
    }
    
    /**
     * 更新代理密码
     */
    fun updateProxyPassword(password: String) {
        currentInputProxyPassword = password
    }
    
    /**
     * 更新主题模式
     * @param mode 主题模式，可以是 "light", "dark", "system"
     */
    fun updateThemeMode(mode: String) {
        currentThemeMode = mode
    }
    
    /**
     * 更新启动时检查更新的设置
     * @param enabled 是否启用
     */
    fun updateCheckUpdateOnStart(enabled: Boolean) {
        currentCheckUpdateOnStart = enabled
    }
    
    /**
     * 更新更新分支设置
     * @param branch 更新分支，可以是 "master", "dev"
     */
    fun updateUpdateBranch(branch: String) {
        currentUpdateBranch = branch
    }
    
    /**
     * 更新首页分组模式
     * @param mode 分组模式，可以是 "category", "source", "category,source", "none"
     */
    fun updateHomeGroupingMode(mode: String) {
        currentHomeGroupingMode = mode
    }
    
    /**
     * 更新分类过滤配置列表
     * @param configs 分类过滤配置列表
     */
    fun updateCategoryFilterConfigs(configs: List<CategoryFilterConfig>) {
        currentCategoryFilterConfigs = configs.toMutableList()
        // 更新 UI 状态，确保页面显示最新配置
        _uiState.value = _uiState.value.copy(
            categoryFilterConfigs = currentCategoryFilterConfigs
        )
    }
    
    /**
     * 更新图片缓存启用状态
     * @param enabled 是否文章显示图片
     */
    fun updateImageCacheEnabled(enabled: Boolean) {
        currentImageCacheEnabled = enabled
    }
    
    /**
     * 更新AI API地址
     * @param url AI API地址
     */
    fun updateAiApiUrl(url: String) {
        currentAiApiUrl = url
    }
    
    /**
     * 更新AI API密钥
     * @param apiKey AI API密钥
     */
    fun updateAiApiKey(apiKey: String) {
        currentAiApiKey = apiKey
    }
    
    /**
     * 更新AI模型名称
     * @param modelName AI模型名称
     */
    fun updateAiModelName(modelName: String) {
        currentAiModelName = modelName
    }
    
    /**
     * 更新AI提示词
     * @param prompt AI提示词
     */
    fun updateAiPrompt(prompt: String) {
        currentAiPrompt = prompt
    }
    
    /**
     * 更新自动标记博客已读设置
     * @param enabled 是否启用
     */
    fun updateMarkPodcastAsRead(enabled: Boolean) {
        currentMarkPodcastAsRead = enabled
    }

    /**
     * 更新博客倍速播放设置
     * @param speed 播放速度（0.5f - 2.0f）
     */
    fun updatePlaybackSpeed(speed: Float) {
        currentPlaybackSpeed = speed
    }

    fun updateBlogAutoDownloadToLocal(enabled: Boolean) {
        currentBlogAutoDownloadToLocal = enabled
    }

    /**
     * 更新标题过滤关键词
     * @param keywords 标题过滤关键词
     */
    fun updateTitleFilterKeywords(keywords: String) {
        currentTitleFilterKeywords = keywords
    }

    /**
     * 保存标题过滤关键词设置
     */
    fun saveTitleFilterSettings() {
        viewModelScope.launch {
            try {
                settingsDataStore.saveTitleFilterKeywords(currentTitleFilterKeywords)
                _uiState.value = _uiState.value.copy(titleFilterKeywords = currentTitleFilterKeywords)
            } catch (e: Exception) {
                showMessage("保存标题过滤设置失败：${e.message}")
            }
        }
    }

    /**
     * 更新样式配置
     * @param config 样式配置
     */
    fun updateStyleConfig(config: StyleConfig) {
        currentStyleConfig = config
    }

    /**
     * 保存样式配置设置
     */
    fun saveStyleConfigSettings() {
        viewModelScope.launch {
            try {
                settingsDataStore.saveStyleConfig(currentStyleConfig)
                settingsDataStore.saveImageCacheEnabled(currentImageCacheEnabled)
                _uiState.value = _uiState.value.copy(styleConfig = currentStyleConfig)
            } catch (e: Exception) {
                showMessage("保存样式设置失败：${e.message}")
            }
        }
    }

    fun saveImageCacheEnabled() {
        viewModelScope.launch {
            try {
                settingsDataStore.saveImageCacheEnabled(currentImageCacheEnabled)
                _uiState.value = _uiState.value.copy(imageCacheEnabled = currentImageCacheEnabled)
            } catch (e: Exception) {
                showMessage("保存图片缓存设置失败：${e.message}")
            }
        }
    }

    fun saveMarkPodcastAsRead() {
        viewModelScope.launch {
            try {
                settingsDataStore.saveMarkPodcastAsRead(currentMarkPodcastAsRead)
                _uiState.value = _uiState.value.copy(markPodcastAsRead = currentMarkPodcastAsRead)
            } catch (e: Exception) {
                showMessage("保存博客设置失败：${e.message}")
            }
        }
    }

    /**
     * 保存博客设置（包含自动已读和倍速设置）
     */
    fun saveBlogSettings() {
        viewModelScope.launch {
            try {
                settingsDataStore.saveMarkPodcastAsRead(currentMarkPodcastAsRead)
                settingsDataStore.savePlaybackSpeed(currentPlaybackSpeed)
                settingsDataStore.saveBlogAutoDownloadToLocal(currentBlogAutoDownloadToLocal)
                _uiState.value = _uiState.value.copy(
                    markPodcastAsRead = currentMarkPodcastAsRead,
                    playbackSpeed = currentPlaybackSpeed,
                    blogAutoDownloadToLocal = currentBlogAutoDownloadToLocal
                )
            } catch (e: Exception) {
                showMessage("保存博客设置失败：${e.message}")
            }
        }
    }

    /**
     * 保存API地址设置
     */
    fun saveApiUrl() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val trimmedUrl = currentInputApiUrl.trim()
                
                // 验证URL格式
                if (trimmedUrl.isEmpty()) {
                    showMessage("请输入API服务器地址")
                    return@launch
                }
                
                if (!settingsDataStore.isValidUrl(trimmedUrl)) {
                    showMessage("请输入有效的API服务器地址（需以 http:// 或 https:// 开头）")
                    return@launch
                }
                
                // 格式化URL并保存
                val formattedUrl = settingsDataStore.formatUrl(trimmedUrl)
                settingsDataStore.saveApiBaseUrl(formattedUrl)
                
                showMessage("API地址设置已保存")
                
            } catch (e: Exception) {
                showMessage("保存失败：${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    /**
     * 保存后端URL设置
     */
    fun saveBackendUrl() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val trimmedUrl = currentInputBackendUrl.trim()
                
                // 验证URL格式
                if (trimmedUrl.isEmpty()) {
                    showMessage("请输入后端URL")
                    return@launch
                }
                
                // 格式化URL并保存
                settingsDataStore.saveBackendUrl(trimmedUrl)
                
                showMessage("后端URL设置已保存")
                
            } catch (e: Exception) {
                showMessage("保存失败：${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    /**
     * 保存所有设置
     */
    fun saveAllSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val trimmedApiUrl = currentInputApiUrl.trim()
                val trimmedBackendUrl = currentInputBackendUrl.trim()
                
                // 验证API URL格式
                if (trimmedApiUrl.isEmpty()) {
                    showMessage("请输入API服务器地址")
                    return@launch
                }
                
                if (!settingsDataStore.isValidUrl(trimmedApiUrl)) {
                    showMessage("请输入有效的API服务器地址（需以 http:// 或 https:// 开头）")
                    return@launch
                }
                
                // 验证后端URL
                if (trimmedBackendUrl.isEmpty()) {
                    showMessage("请输入后端URL")
                    return@launch
                }
                
                // 验证代理设置
                if (currentProxyEnabled) {
                    val trimmedProxyHost = currentInputProxyHost.trim()
                    if (trimmedProxyHost.isEmpty()) {
                        showMessage("启用代理时，请输入代理主机地址")
                        return@launch
                    }
                    if (currentInputProxyPort <= 0 || currentInputProxyPort > 65535) {
                        showMessage("请输入有效的代理端口（1-65535）")
                        return@launch
                    }
                }
                
                // 保存所有设置
                val formattedApiUrl = settingsDataStore.formatUrl(trimmedApiUrl)
                settingsDataStore.saveApiBaseUrl(formattedApiUrl)
                settingsDataStore.saveBackendUrl(trimmedBackendUrl)
                settingsDataStore.saveProxySettings(
                    enabled = currentProxyEnabled,
                    type = currentInputProxyType,
                    host = currentInputProxyHost.trim(),
                    port = currentInputProxyPort,
                    username = currentInputProxyUsername.trim(),
                    password = currentInputProxyPassword
                )
                
                // 刷新API客户端以应用新的设置
                ApiClient.refreshApiService(getApplication())
                
                showMessage("所有设置已保存")
                
            } catch (e: Exception) {
                showMessage("保存失败：${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    /**
     * 保存代理设置
     */
    fun saveProxySettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // 验证代理设置
                if (currentProxyEnabled) {
                    val trimmedProxyHost = currentInputProxyHost.trim()
                    if (trimmedProxyHost.isEmpty()) {
                        showMessage("启用代理时，请输入代理主机地址")
                        return@launch
                    }
                    if (currentInputProxyPort <= 0 || currentInputProxyPort > 65535) {
                        showMessage("请输入有效的代理端口（1-65535）")
                        return@launch
                    }
                }
                
                // 保存代理设置
                settingsDataStore.saveProxySettings(
                    enabled = currentProxyEnabled,
                    type = currentInputProxyType,
                    host = currentInputProxyHost.trim(),
                    port = currentInputProxyPort,
                    username = currentInputProxyUsername.trim(),
                    password = currentInputProxyPassword
                )
                
                // 刷新API客户端以应用新的代理设置
                ApiClient.refreshApiService(getApplication())
                
                if (currentProxyEnabled) {
                    showMessage("代理设置已保存并启用")
                } else {
                    showMessage("代理已禁用")
                }
                
            } catch (e: Exception) {
                showMessage("保存代理设置失败：${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    /**
     * 保存主题设置
     */
    fun saveThemeSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // 保存主题设置
                settingsDataStore.saveThemeMode(currentThemeMode)
                showMessage("主题设置已保存")
                
            } catch (e: Exception) {
                showMessage("保存主题设置失败：${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    /**
     * 保存启动时检查更新的设置
     */
    fun saveCheckUpdateOnStart() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                settingsDataStore.saveCheckUpdateOnStart(currentCheckUpdateOnStart)
                showMessage("设置已保存")
                
            } catch (e: Exception) {
                showMessage("保存失败：${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    /**
     * 保存更新分支设置
     */
    fun saveUpdateBranch() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                settingsDataStore.saveUpdateBranch(currentUpdateBranch)
                showMessage("设置已保存")
                
            } catch (e: Exception) {
                showMessage("保存失败：${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    /**
     * 保存首页分组模式
     */
    fun saveHomeGroupingMode() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                settingsDataStore.saveHomeGroupingMode(currentHomeGroupingMode)
                showMessage("设置已保存")
                
            } catch (e: Exception) {
                showMessage("保存失败：${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    /**
     * 保存分类过滤配置
     */
    fun saveCategoryFilterSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                settingsDataStore.saveCategoryFilterConfigs(currentCategoryFilterConfigs)
                settingsDataStore.saveImageCacheEnabled(currentImageCacheEnabled)
                settingsDataStore.saveMarkPodcastAsRead(currentMarkPodcastAsRead)
                
                showMessage("个性化设置已保存")
                
            } catch (e: Exception) {
                showMessage("保存失败：${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    /**
     * 保存分类过滤配置（在新页面使用）
     */
    fun saveCategoryFilterConfigs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                settingsDataStore.saveCategoryFilterConfigs(currentCategoryFilterConfigs)
                // 更新 UI 状态，确保页面显示最新配置
                _uiState.value = _uiState.value.copy(
                    categoryFilterConfigs = currentCategoryFilterConfigs
                )
                showMessage("设置已保存")
                
            } catch (e: Exception) {
                showMessage("保存失败：${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    /**
     * 保存AI模型配置
     */
    fun saveAiSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val trimmedApiUrl = currentAiApiUrl.trim()
                
                // 验证AI API URL格式
                if (trimmedApiUrl.isEmpty()) {
                    showMessage("请输入AI API地址")
                    return@launch
                }
                
                if (!settingsDataStore.isValidUrl(trimmedApiUrl)) {
                    showMessage("请输入有效的AI API地址（需以 http:// 或 https:// 开头）")
                    return@launch
                }
                
                // 验证API密钥
                if (currentAiApiKey.trim().isEmpty()) {
                    showMessage("请输入AI API密钥")
                    return@launch
                }
                
                // 验证模型名称
                if (currentAiModelName.trim().isEmpty()) {
                    showMessage("请输入AI模型名称")
                    return@launch
                }
                
                // 保存AI模型配置
                val formattedApiUrl = settingsDataStore.formatUrl(trimmedApiUrl)
                settingsDataStore.saveAiSettings(
                    apiUrl = formattedApiUrl,
                    apiKey = currentAiApiKey.trim(),
                    modelName = currentAiModelName.trim()
                )
                
                // 保存AI提示词
                settingsDataStore.saveAiPrompt(currentAiPrompt.trim())
                
                showMessage("AI模型配置已保存")
                
            } catch (e: Exception) {
                showMessage("保存失败：${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    /**
     * 重置所有设置到默认值
     */
    fun resetAllSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                settingsDataStore.resetAllSettings()
                currentInputApiUrl = SettingsDataStore.DEFAULT_API_BASE_URL
                currentInputBackendUrl = SettingsDataStore.DEFAULT_BACKEND_URL
                currentProxyEnabled = SettingsDataStore.DEFAULT_PROXY_ENABLED
                currentInputProxyType = SettingsDataStore.DEFAULT_PROXY_TYPE
                currentInputProxyHost = SettingsDataStore.DEFAULT_PROXY_HOST
                currentInputProxyPort = SettingsDataStore.DEFAULT_PROXY_PORT
                currentInputProxyUsername = SettingsDataStore.DEFAULT_PROXY_USERNAME
                currentInputProxyPassword = SettingsDataStore.DEFAULT_PROXY_PASSWORD
                currentThemeMode = SettingsDataStore.DEFAULT_THEME_MODE
            currentCheckUpdateOnStart = SettingsDataStore.DEFAULT_CHECK_UPDATE_ON_START
            currentUpdateBranch = SettingsDataStore.DEFAULT_UPDATE_BRANCH
            currentHomeGroupingMode = SettingsDataStore.DEFAULT_HOME_GROUPING_MODE
            currentCategoryFilterConfigs = mutableListOf()
            currentImageCacheEnabled = SettingsDataStore.DEFAULT_IMAGE_CACHE_ENABLED
            currentAiApiUrl = SettingsDataStore.DEFAULT_AI_API_URL
            currentAiApiKey = SettingsDataStore.DEFAULT_AI_API_KEY
            currentAiModelName = SettingsDataStore.DEFAULT_AI_MODEL_NAME
            currentAiPrompt = SettingsDataStore.DEFAULT_AI_PROMPT
            currentMarkPodcastAsRead = SettingsDataStore.DEFAULT_MARK_PODCAST_AS_READ
            currentPlaybackSpeed = SettingsDataStore.DEFAULT_PLAYBACK_SPEED

            // 刷新API客户端以应用重置的设置
                ApiClient.refreshApiService(getApplication())
                
                showMessage("已重置为默认设置")
                
            } catch (e: Exception) {
                showMessage("重置失败：${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    /**
     * 重置单服务器配置（仅重置API地址和后端URL）
     */
    fun resetSingleServerSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // 仅重置API地址和后端URL
                settingsDataStore.resetApiBaseUrl()
                settingsDataStore.resetBackendUrl()
                currentInputApiUrl = SettingsDataStore.DEFAULT_API_BASE_URL
                currentInputBackendUrl = SettingsDataStore.DEFAULT_BACKEND_URL
                
                // 刷新API客户端以应用重置的设置
                ApiClient.refreshApiService(getApplication())
                
                showMessage("服务器配置已重置")
                
            } catch (e: Exception) {
                showMessage("重置失败：${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    /**
     * 获取当前输入的AI提示词（用于UI显示）
     */
    fun getCurrentInputAiPrompt(): String = currentAiPrompt
    
    /**
     * 保存多服务器配置列表
     * @param serverConfigs 服务器配置列表
     */
    fun saveServerConfigs(serverConfigs: List<ServerConfig>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                settingsDataStore.saveServerConfigs(serverConfigs)
                showMessage("多服务器配置已保存")
                
                // 更新UI状态
                _uiState.value = _uiState.value.copy(serverConfigs = serverConfigs)
            } catch (e: Exception) {
                showMessage("保存多服务器配置失败：${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    /**
     * 显示消息
     * @param message 要显示的消息
     */
    private fun showMessage(message: String) {
        _uiState.value = _uiState.value.copy(message = message)
    }
    
    /**
     * 清除消息
     */
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = "")
    }
    
    /**
     * 获取当前输入的API URL（用于UI显示）
     */
    fun getCurrentInputApiUrl(): String = currentInputApiUrl
    
    /**
     * 获取当前输入的后端URL（用于UI显示）
     */
    fun getCurrentInputBackendUrl(): String = currentInputBackendUrl
    
    /**
     * 获取当前输入的AI API URL（用于UI显示）
     */
    fun getCurrentInputAiApiUrl(): String = currentAiApiUrl
    
    /**
     * 获取当前输入的AI API密钥（用于UI显示）
     */
    fun getCurrentInputAiApiKey(): String = currentAiApiKey
    
    /**
     * 获取当前输入的AI模型名称（用于UI显示）
     */
    fun getCurrentInputAiModelName(): String = currentAiModelName
}
