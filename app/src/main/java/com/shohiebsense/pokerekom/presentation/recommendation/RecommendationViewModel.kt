package com.shohiebsense.pokerekom.presentation.recommendation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shohiebsense.pokerekom.data.local.ImageCache
import com.shohiebsense.pokerekom.domain.usecase.CacheImagesUseCase
import com.shohiebsense.pokerekom.domain.usecase.GetCachedTeamUseCase
import com.shohiebsense.pokerekom.domain.usecase.GetRecommendationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class RecommendationViewModel @Inject constructor(
    private val getRecommendationUseCase: GetRecommendationUseCase,
    private val getCachedTeamUseCase: GetCachedTeamUseCase,
    private val cacheImagesUseCase: CacheImagesUseCase,
    private val imageCache: ImageCache
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecommendationUiState>(RecommendationUiState.Idle)
    val uiState: StateFlow<RecommendationUiState> = _uiState.asStateFlow()

    init {
        loadCachedTeam()
    }

    private fun loadCachedTeam() {
        viewModelScope.launch {
            val cachedResult = getCachedTeamUseCase()
            cachedResult.onSuccess { cached ->
                cached?.let { team ->
                    _uiState.value = RecommendationUiState.Success(
                        team = team,
                        imageUris = loadCachedUris(team)
                    )
                    cacheImagesInBackground(team)
                }
            }
        }
    }

    fun requestRecommendation(payloadJson: String? = "{}") {
        if (_uiState.value is RecommendationUiState.Loading) return
        
        viewModelScope.launch {
            _uiState.value = RecommendationUiState.Loading
            val result = getRecommendationUseCase(payloadJson)
            
            result.onSuccess { team ->
                _uiState.value = RecommendationUiState.Success(
                    team = team,
                    imageUris = loadCachedUris(team)
                )
                cacheImagesInBackground(team)
            }.onError { error ->
                _uiState.value = RecommendationUiState.Error(
                    message = error.message ?: "Unknown error"
                )
            }
        }
    }

    private fun cacheImagesInBackground(team: List<String>) {
        viewModelScope.launch {
            cacheImagesUseCase(team)
            val currentState = _uiState.value
            if (currentState is RecommendationUiState.Success) {
                _uiState.value = currentState.copy(
                    imageUris = loadCachedUris(team)
                )
            }
        }
    }

    private suspend fun loadCachedUris(team: List<String>): Map<String, Uri> = withContext(Dispatchers.IO) {
        buildMap {
            for (name in team) {
                val normalized = name.trim().lowercase()
                imageCache.getCachedFile(normalized)?.let { file ->
                    put(normalized, Uri.fromFile(file))
                }
            }
        }
    }
}
