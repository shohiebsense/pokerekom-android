package com.shohiebsense.pokerekom.data.local

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageCache @Inject constructor(
    private val context: Context
) {
    private fun cacheDir(): File {
        val d = File(context.cacheDir, "pokemon_images")
        if (!d.exists()) d.mkdirs()
        return d
    }

    private fun normalizedFilename(pokemonName: String, ext: String): String {
        val safe = pokemonName.trim().lowercase()
            .replace(' ', '_')
            .replace('/', '_')
        val enc = URLEncoder.encode(safe, "utf-8")
        return "$enc.$ext"
    }

    fun getCachedFile(pokemonName: String): File? {
        val dir = cacheDir()
        val safe = pokemonName.trim().lowercase()
        listOf("png", "jpg", "jpeg", "webp").forEach { ext ->
            val f = File(dir, normalizedFilename(safe, ext))
            if (f.exists()) return f
        }
        return null
    }

    fun getCachedUri(pokemonName: String): Uri? {
        return getCachedFile(pokemonName)?.let { Uri.fromFile(it) }
    }

    fun saveImage(pokemonName: String, bytes: ByteArray, contentType: String?): File {
        val ext = when {
            contentType == null -> "png"
            contentType.contains("png") -> "png"
            contentType.contains("webp") -> "webp"
            contentType.contains("jpeg") || contentType.contains("jpg") -> "jpg"
            else -> "png"
        }
        val dir = cacheDir()
        val tmp = File(dir, "tmp_${System.currentTimeMillis()}")
        FileOutputStream(tmp).use { it.write(bytes) }
        val final = File(dir, normalizedFilename(pokemonName, ext))
        if (final.exists()) final.delete()
        if (!tmp.renameTo(final)) {
            final.outputStream().use { out -> tmp.inputStream().use { it.copyTo(out) } }
            tmp.delete()
        }
        return final
    }

    fun clearCache() {
        cacheDir().listFiles()?.forEach { it.delete() }
    }
}
