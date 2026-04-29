package com.rnandresy.lol.model

data class Badge(
    val id: String = "",
    val name: String = "",           // lowercase unique
    val displayName: String = "",
    val colorHex: String = "#7C4DFF",
    val createdBy: String = "",
    val createdAt: Long = 0L
)