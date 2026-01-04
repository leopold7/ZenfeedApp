package com.ddyy.zenfeed.extension

import com.ddyy.zenfeed.data.Feed

/**
 * List扩展函数 - 提供列表操作的工具方法
 */

/**
 * 获取最近阅读的条目索引
 * 找到最后一个已读的条目（按列表顺序）
 */
fun List<Feed>.getLastReadFeedIndex(): Int? {
    return this.indexOfLast { it.isRead }.takeIf { it >= 0 }
}

/**
 * 检查是否有有效的播客URL
 */
fun List<Feed>.hasValidPodcast(): Boolean {
    return this.any { !it.labels.podcastUrl.isNullOrBlank() }
}

/**
 * 根据分类对feeds进行分组
 */
fun List<Feed>.groupByCategory(): Map<String, List<Feed>> {
    val grouped = this.groupBy { it.labels.category ?: "" }.toMutableMap()
    // 将"全部"类别也添加进去，确保"全部"列表覆盖任何可能存在的、分类为空字符串的列表
    grouped[""] = this
    return grouped.toMap()
}

/**
     * 根据分组模式对feeds进行分组
     * @param mode 分组模式，可以是 "category", "source", "category,source", "none"
     */
    fun List<Feed>.groupByMode(
        mode: String
    ): Map<String, List<Feed>> {
        return when (mode) {
            "source" -> {
                val grouped = this.groupBy { it.labels.source ?: "" }.toMutableMap()
                grouped[""] = this
                grouped.toMap()
            }
            "category,source" -> {
                val grouped = mutableMapOf<String, List<Feed>>()
                // 先添加分类，确保分类在前
                val categoryMap = this.groupBy { it.labels.category ?: "" }
                categoryMap.forEach { (category, feeds) ->
                    grouped[category] = feeds
                }
                // 再添加来源，确保来源在后
                val sourceMap = this.groupBy { it.labels.source ?: "" }
                sourceMap.forEach { (source, feeds) ->
                    // 确保来源不会覆盖分类
                    if (!grouped.containsKey(source)) {
                        grouped[source] = feeds
                    }
                }
                grouped[""] = this
                grouped.toMap()
            }
            "none" -> {
                mapOf("" to this)
            }
            else -> {
                val grouped = this.groupBy { it.labels.category ?: "" }.toMutableMap()
                grouped[""] = this
                grouped.toMap()
            }
        }
    }
