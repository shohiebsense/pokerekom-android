package com.shohiebsense.pokerekom.domain.usecase

import com.shohiebsense.pokerekom.data.repository.PokemonRepository
import com.shohiebsense.pokerekom.domain.model.Result
import javax.inject.Inject

class GetRecommendationUseCase @Inject constructor(
    private val repository: PokemonRepository
) {
    suspend operator fun invoke(payloadJson: String? = "{}"): Result<List<String>> {
        return try {
            val team = repository.fetchTeam(payloadJson)
            Result.Success(team)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
