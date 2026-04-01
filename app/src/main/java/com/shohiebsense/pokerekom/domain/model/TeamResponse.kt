package com.shohiebsense.pokerekom.domain.model

import com.google.gson.annotations.SerializedName

data class TeamResponse(
    @SerializedName("team_line")
    val teamLine: String?
)
