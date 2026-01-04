package com.ddyy.zenfeed.data

import com.google.gson.annotations.SerializedName

data class CategoryFilterConfig(
    @SerializedName("category_name")
    val categoryName: String,
    @SerializedName("show_in_all")
    val showInAll: Boolean = true,
    @SerializedName("show_group")
    val showGroup: Boolean = true,
    @SerializedName("sort_order")
    val sortOrder: Int = 0
)
