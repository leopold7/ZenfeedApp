package com.ddyy.zenfeed.ui.favorites

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ddyy.zenfeed.data.BlogOfflineAudioCache
import com.ddyy.zenfeed.data.FavoriteFeedEntry
import com.ddyy.zenfeed.data.FavoritesRepository
import com.ddyy.zenfeed.data.Feed
import com.ddyy.zenfeed.data.SettingsDataStore
import com.ddyy.zenfeed.data.toStableFeedId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {
    private val favoritesRepository = FavoritesRepository.getInstance(application.applicationContext)
    private val settingsDataStore = SettingsDataStore(application.applicationContext)
    private val offlineAudioCache = BlogOfflineAudioCache(application.applicationContext)
    private val downloadJobs = mutableMapOf<String, Job>()

    var favoriteEntries: List<FavoriteFeedEntry> by mutableStateOf(emptyList())
        private set

    val favoriteFeeds: List<Feed>
        get() = favoriteEntries.sortedByDescending { it.favoritedAt }.map { it.feed }

    init {
        reload()
        viewModelScope.launch {
            settingsDataStore.blogAutoDownloadToLocal.collectLatest { enabled ->
                if (enabled) ensureOfflineCacheForFavorites()
            }
        }
    }

    fun reload() {
        favoriteEntries = favoritesRepository.getAll().sortedByDescending { it.favoritedAt }
    }

    fun isFavorited(feed: Feed): Boolean {
        val id = feed.toStableFeedId()
        return favoriteEntries.any { it.feedId == id }
    }

    fun toggleFavorite(feed: Feed) {
        val id = feed.toStableFeedId()
        val existed = favoriteEntries.any { it.feedId == id }
        if (existed) {
            downloadJobs.remove(id)?.cancel()
            favoriteEntries = favoritesRepository.remove(id).sortedByDescending { it.favoritedAt }
            val podcastUrl = feed.labels.podcastUrl.orEmpty()
            if (podcastUrl.isNotBlank()) {
                offlineAudioCache.deleteFor(podcastUrl, feed.serverId)
            }
            val localPath = feed.labels.localPodcastPath.orEmpty()
            if (localPath.isNotBlank()) {
                try {
                    File(localPath).delete()
                } catch (_: Exception) {
                }
            }
        } else {
            val favoritedAt = System.currentTimeMillis()
            val updated = favoritesRepository.upsert(feed, favoritedAt = favoritedAt)
            favoriteEntries = updated.sortedByDescending { it.favoritedAt }
            maybeAutoDownloadOffline(feed, favoritedAt)
        }
    }

    fun clearAll() {
        downloadJobs.values.forEach { it.cancel() }
        downloadJobs.clear()
        favoritesRepository.clear()
        favoriteEntries = emptyList()
        offlineAudioCache.clearCache()
    }

    private fun maybeAutoDownloadOffline(feed: Feed, favoritedAt: Long) {
        val podcastUrl = feed.labels.podcastUrl.orEmpty()
        if (podcastUrl.isBlank()) return

        viewModelScope.launch {
            val enabled = settingsDataStore.blogAutoDownloadToLocal.first()
            if (!enabled) return@launch

            try {
                val localFile = withContext(Dispatchers.IO) {
                    offlineAudioCache.downloadIfNeeded(podcastUrl, feed.serverId)
                }
                val stillFavorited = withContext(Dispatchers.IO) { favoritesRepository.isFavorited(feed) }
                if (!stillFavorited) {
                    offlineAudioCache.deleteFor(podcastUrl, feed.serverId)
                    return@launch
                }
                val currentPath = feed.labels.localPodcastPath.orEmpty()
                if (currentPath != localFile.absolutePath) {
                    val updatedFeed = feed.copy(
                        labels = feed.labels.copy(localPodcastPath = localFile.absolutePath)
                    )
                    favoriteEntries = withContext(Dispatchers.IO) {
                        favoritesRepository
                            .upsert(updatedFeed, favoritedAt = favoritedAt)
                            .sortedByDescending { it.favoritedAt }
                    }
                }
            } catch (e: Exception) {
                Log.e("FavoritesViewModel", "离线下载失败", e)
            }
        }
    }

    private fun ensureOfflineCacheForFavorites() {
        favoriteEntries.forEach { entry ->
            val feed = entry.feed
            val feedId = entry.feedId
            val podcastUrl = feed.labels.podcastUrl.orEmpty()
            if (podcastUrl.isBlank()) return@forEach

            val localPath = feed.labels.localPodcastPath.orEmpty()
            if (localPath.isNotBlank()) {
                try {
                    val file = File(localPath)
                    if (file.exists() && file.length() > 0) return@forEach
                } catch (_: Exception) {
                }
            }

            if (downloadJobs.containsKey(feedId)) return@forEach

            downloadJobs[feedId] = viewModelScope.launch {
                try {
                    val localFile = withContext(Dispatchers.IO) {
                        offlineAudioCache.downloadIfNeeded(podcastUrl, feed.serverId)
                    }
                    val stillFavorited = withContext(Dispatchers.IO) { favoritesRepository.isFavorited(feedId) }
                    if (!stillFavorited) {
                        offlineAudioCache.deleteFor(podcastUrl, feed.serverId)
                        return@launch
                    }

                    val updatedFeed = feed.copy(
                        labels = feed.labels.copy(localPodcastPath = localFile.absolutePath)
                    )
                    favoriteEntries = withContext(Dispatchers.IO) {
                        favoritesRepository
                            .upsert(updatedFeed, favoritedAt = entry.favoritedAt)
                            .sortedByDescending { it.favoritedAt }
                    }
                } catch (_: Exception) {
                } finally {
                    downloadJobs.remove(feedId)
                }
            }
        }
    }
}
