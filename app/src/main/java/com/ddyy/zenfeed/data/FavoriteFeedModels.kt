package com.ddyy.zenfeed.data

data class FavoriteFeedEntry(
    val feedId: String,
    val favoritedAt: Long,
    val feed: Feed
)

fun Feed.toStableFeedId(): String = "${labels.title.orEmpty()}-$time-${serverId.orEmpty()}"

