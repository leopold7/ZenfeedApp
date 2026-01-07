package com.ddyy.zenfeed.extension

import com.ddyy.zenfeed.data.Feed

fun Feed.filterByTitleKeywords(keywords: String): Boolean {
    if (keywords.isBlank()) {
        return true
    }

    val title = this.labels.title ?: return true

    val keywordList = keywords.split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    if (keywordList.isEmpty()) {
        return true
    }

    val titleLower = title.lowercase()

    return keywordList.none { keyword ->
        titleLower.contains(keyword.lowercase())
    }
}