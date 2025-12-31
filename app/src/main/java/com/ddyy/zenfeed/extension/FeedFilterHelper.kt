package com.ddyy.zenfeed.extension

import com.ddyy.zenfeed.data.Feed

/**
 * 过滤辅助对象 - 提供分组过滤的通用逻辑
 */
object FeedFilterHelper {
    
    fun shouldShowGroup(
        group: String,
        filterType: String,
        blacklist: Set<String>,
        whitelist: Set<String>
    ): Boolean {
        if (group.isBlank()) return false
        
        return when (filterType) {
            "blacklist" -> blacklist.none { it in group }
            "whitelist" -> whitelist.isEmpty() || whitelist.any { it in group }
            else -> true
        }
    }
    
    fun shouldIncludeFeedInAll(
        feed: Feed,
        mode: String,
        filterType: String,
        blacklist: Set<String>,
        whitelist: Set<String>
    ): Boolean {
        return when (mode) {
            "source" -> {
                val group = feed.labels.source ?: ""
                if (group.isBlank()) return true
                when (filterType) {
                    "blacklist" -> blacklist.none { it in group }
                    "whitelist" -> whitelist.isEmpty() || whitelist.any { it in group }
                    else -> true
                }
            }
            "category,source" -> {
                val category = feed.labels.category ?: ""
                val source = feed.labels.source ?: ""
                if (category.isBlank() && source.isBlank()) return true
                when (filterType) {
                    "blacklist" -> blacklist.none { it in category } && blacklist.none { it in source }
                    "whitelist" -> whitelist.isEmpty() || whitelist.any { it in category } || whitelist.any { it in source }
                    else -> true
                }
            }
            else -> {
                val group = feed.labels.category ?: ""
                if (group.isBlank()) return true
                when (filterType) {
                    "blacklist" -> blacklist.none { it in group }
                    "whitelist" -> whitelist.isEmpty() || whitelist.any { it in group }
                    else -> true
                }
            }
        }
    }
}
