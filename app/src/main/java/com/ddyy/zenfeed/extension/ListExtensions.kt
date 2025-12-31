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
 * @param mode 分组模式，可以是 "category", "source", "category,source"
 * @param filterType 过滤类型，可以是 "none", "blacklist", "whitelist"
 * @param blacklist 分类黑名单，不包含在结果中的分类
 * @param whitelist 分类白名单，只包含在结果中的分类
 * @param filterIncludeAll 是否在"全部"分组中应用过滤
 */
fun List<Feed>.groupByMode(
    mode: String,
    filterType: String = "none",
    blacklist: Set<String> = emptySet(),
    whitelist: Set<String> = emptySet(),
    filterIncludeAll: Boolean = true
): Map<String, List<Feed>> {
    return when (mode) {
        "source" -> {
            buildGroupedMap(
                builderAction = {
                    this@groupByMode.groupBy { it.labels.source ?: "" }
                        .forEach { (source, feeds) ->
                            if (FeedFilterHelper.shouldShowGroup(source, filterType, blacklist, whitelist)) {
                                put(source, feeds)
                            }
                        }
                },
                mode = mode,
                filterType = filterType,
                blacklist = blacklist,
                whitelist = whitelist,
                filterIncludeAll = filterIncludeAll
            )
        }
        "category,source" -> {
            buildGroupedMap(
                builderAction = {
                    this@groupByMode.groupBy { it.labels.category ?: "" }
                        .forEach { (category, feeds) ->
                            if (FeedFilterHelper.shouldShowGroup(category, filterType, blacklist, whitelist)) {
                                put(category, feeds)
                            }
                        }
                    
                    this@groupByMode.groupBy { it.labels.source ?: "" }
                        .forEach { (source, feeds) ->
                            if (FeedFilterHelper.shouldShowGroup(source, filterType, blacklist, whitelist)) {
                                put(source, feeds)
                            }
                        }
                },
                mode = mode,
                filterType = filterType,
                blacklist = blacklist,
                whitelist = whitelist,
                filterIncludeAll = filterIncludeAll
            )
        }
        else -> {
            buildGroupedMap(
                builderAction = {
                    this@groupByMode.groupBy { it.labels.category ?: "" }
                        .forEach { (category, feeds) ->
                            if (FeedFilterHelper.shouldShowGroup(category, filterType, blacklist, whitelist)) {
                                put(category, feeds)
                            }
                        }
                },
                mode = mode,
                filterType = filterType,
                blacklist = blacklist,
                whitelist = whitelist,
                filterIncludeAll = filterIncludeAll
            )
        }
    }
}

/**
 * 构建分组Map，自动添加"全部"类别
 */
private fun List<Feed>.buildGroupedMap(
    builderAction: MutableMap<String, List<Feed>>.() -> Unit,
    mode: String,
    filterType: String,
    blacklist: Set<String>,
    whitelist: Set<String>,
    filterIncludeAll: Boolean
): Map<String, List<Feed>> {
    val grouped = mutableMapOf<String, List<Feed>>()
    grouped.builderAction()
    
    val allFeeds = if (filterIncludeAll && filterType != "none") {
        this.filter { feed ->
            FeedFilterHelper.shouldIncludeFeedInAll(feed, mode, filterType, blacklist, whitelist)
        }
    } else {
        this
    }
    
    grouped[""] = allFeeds
    return grouped.toMap()
}
