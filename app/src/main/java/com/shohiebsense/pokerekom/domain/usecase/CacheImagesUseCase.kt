package com.shohiebsense.pokerekom.domain.usecase

import com.shohiebsense.pokerekom.data.repository.PokemonRepository
import com.shohiebsense.pokerekom.domain.model.Result
import javax.inject.Inject

class CacheImagesUseCase @Inject constructor(
    private val repository: PokemonRepository
) {
    suspend operator fun invoke(team: List<String>, maxRetries: Int = 3): Result<Unit> {
        return try {
            repository.ensureImagesCached(team, maxRetries)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
