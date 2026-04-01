package com.shohiebsense.pokerekom.domain.model

data class Pokemon(
    val name: String,
    val imageUrl: String
) {
    val normalizedName: String
        get() = name.trim().lowercase()
}
