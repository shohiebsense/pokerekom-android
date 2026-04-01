package com.shohiebsense.pokerekom.presentation.recommendation

import android.net.Uri

sealed interface RecommendationUiState {
    data object Idle : RecommendationUiState
    data object Loading : RecommendationUiState
    data class Success(
        val team: List<String>,
        val imageUris: Map<String, Uri>
    ) : RecommendationUiState
    data class Error(val message: String) : RecommendationUiState
}
