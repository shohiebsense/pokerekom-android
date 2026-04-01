package com.shohiebsense.pokerekom.presentation.components

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import java.net.URLEncoder

@Composable
fun PokemonCard(
    name: String,
    imageUri: Uri?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val encodedName = URLEncoder.encode(name.trim().lowercase(), "utf-8")
    val remoteUrl = "https://api.shohiebsense.io/pokemon/$encodedName"
    val model = imageUri ?: remoteUrl

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(model)
                    .crossfade(true)
                    .build(),
                contentDescription = name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(120.dp)
                    .padding(8.dp),
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                },
                error = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error loading image",
                        tint = Color.LightGray
                    )
                }
            )

            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF37474F)
            )
        }
    }
}

@Composable
private fun CircularProgressIndicator(
    modifier: Modifier = Modifier,
    strokeWidth: androidx.compose.ui.unit.Dp = 4.dp
) {
    androidx.compose.material3.CircularProgressIndicator(
        modifier = modifier,
        strokeWidth = strokeWidth
    )
}
