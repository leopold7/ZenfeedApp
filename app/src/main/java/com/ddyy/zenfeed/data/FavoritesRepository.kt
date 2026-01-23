package com.ddyy.zenfeed.data

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FavoritesRepository private constructor(private val context: Context) {
    private val gson = Gson()
    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val lock = Any()

    companion object {
        private const val PREFS_NAME = "favorite_feeds"
        private const val FAVORITES_KEY = "PERSON_FEED_FAVORITES"

        @Volatile
        private var instance: FavoritesRepository? = null

        fun getInstance(context: Context): FavoritesRepository {
            return instance ?: synchronized(this) {
                instance ?: FavoritesRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    fun getAll(): List<FavoriteFeedEntry> {
        synchronized(lock) {
            val json = sharedPreferences.getString(FAVORITES_KEY, null) ?: return emptyList()
            return try {
                val type = object : TypeToken<List<FavoriteFeedEntry>>() {}.type
                gson.fromJson<List<FavoriteFeedEntry>>(json, type) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    fun isFavorited(feedId: String): Boolean = getAll().any { it.feedId == feedId }

    fun isFavorited(feed: Feed): Boolean = isFavorited(feed.toStableFeedId())

    fun upsert(feed: Feed, favoritedAt: Long = System.currentTimeMillis()): List<FavoriteFeedEntry> {
        synchronized(lock) {
            val feedId = feed.toStableFeedId()
            val current = getAll().toMutableList()
            val index = current.indexOfFirst { it.feedId == feedId }
            val entry = FavoriteFeedEntry(feedId = feedId, favoritedAt = favoritedAt, feed = feed)
            if (index >= 0) {
                current[index] = entry
            } else {
                current.add(entry)
            }
            saveAllLocked(current)
            return current
        }
    }

    fun remove(feedId: String): List<FavoriteFeedEntry> {
        synchronized(lock) {
            val current = getAll().toMutableList()
            current.removeAll { it.feedId == feedId }
            saveAllLocked(current)
            return current
        }
    }

    fun clear() {
        synchronized(lock) {
            sharedPreferences.edit { remove(FAVORITES_KEY) }
        }
    }

    fun clearAllLocalPodcastPaths(): List<FavoriteFeedEntry> {
        synchronized(lock) {
            val current = getAll()
            if (current.isEmpty()) return emptyList()

            val updated = current.map { entry ->
                val labels = entry.feed.labels
                if (labels.localPodcastPath.isNullOrBlank()) {
                    entry
                } else {
                    entry.copy(feed = entry.feed.copy(labels = labels.copy(localPodcastPath = null)))
                }
            }
            saveAllLocked(updated)
            return updated
        }
    }

    fun updateLocalPodcastPath(feedId: String, localPodcastPath: String?): List<FavoriteFeedEntry> {
        synchronized(lock) {
            val current = getAll().toMutableList()
            val index = current.indexOfFirst { it.feedId == feedId }
            if (index < 0) return current

            val entry = current[index]
            val labels = entry.feed.labels
            val updatedFeed = entry.feed.copy(
                labels = labels.copy(localPodcastPath = localPodcastPath)
            )
            current[index] = entry.copy(feed = updatedFeed)
            saveAllLocked(current)
            return current
        }
    }

    private fun saveAllLocked(entries: List<FavoriteFeedEntry>) {
        sharedPreferences.edit {
            putString(FAVORITES_KEY, gson.toJson(entries))
        }
    }
}
