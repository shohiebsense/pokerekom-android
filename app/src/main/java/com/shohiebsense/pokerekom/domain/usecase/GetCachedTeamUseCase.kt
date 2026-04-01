package com.shohiebsense.pokerekom.domain.usecase

import com.shohiebsense.pokerekom.data.repository.PokemonRepository
import com.shohiebsense.pokerekom.domain.model.Result
import javax.inject.Inject

class GetCachedTeamUseCase @Inject constructor(
    private val repository: PokemonRepository
) {
    suspend operator fun invoke(): Result<List<String>?> {
        return try {
            val team = repository.loadCachedTeam()
            Result.Success(team)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
