package com.rnandresy.lol.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

// ⚠️ Remplace par tes vraies valeurs Cloudinary
private const val CLOUD_NAME    = "di6bq2h1d"
private const val UPLOAD_PRESET = "postaskip"

enum class MediaType { IMAGE, VIDEO }

object CloudinaryUploader {

    private const val BOUNDARY   = "AskipBoundary"
    private const val LINE_END   = "\r\n"
    private const val TWO_DASHES = "--"

    /**
     * Upload un fichier image ou vidéo depuis un Uri Android.
     * Retourne l'URL publique (secure_url) ou lance une exception.
     * Pas de dépendance externe — uniquement HttpURLConnection.
     */
    fun init(context: Context) {
        // Official Cloudinary SDK initialization
        val config = mapOf(
            "cloud_name" to "di6bq2h1d",
            "secure" to true
        )
        com.cloudinary.android.MediaManager.init(context, config)
    }
    suspend fun upload(
        context: Context,
        uri: Uri,
        mediaType: MediaType = MediaType.IMAGE,
        onProgress: ((Int) -> Unit)? = null
    ): String = withContext(Dispatchers.IO) {

        val resource = if (mediaType == MediaType.VIDEO) "video" else "image"
        val apiUrl   = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/$resource/upload"

        // Lit les bytes du fichier
        val bytes = context.contentResolver
            .openInputStream(uri)
            ?.use { it.readBytes() }
            ?: error("Impossible de lire le fichier")

        val mime = context.contentResolver.getType(uri)
            ?: if (mediaType == MediaType.VIDEO) "video/mp4" else "image/jpeg"

        val fileName = "upload_${System.currentTimeMillis()}"

        // Construit la requête multipart
        val url  = URL(apiUrl)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            doInput        = true
            doOutput       = true
            useCaches      = false
            requestMethod  = "POST"
            connectTimeout = 30_000
            readTimeout    = 120_000
            setRequestProperty("Connection", "Keep-Alive")
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$BOUNDARY")
        }

        DataOutputStream(conn.outputStream).use { out ->
            // Champ upload_preset
            out.writeBytes("$TWO_DASHES$BOUNDARY$LINE_END")
            out.writeBytes("Content-Disposition: form-data; name=\"upload_preset\"$LINE_END")
            out.writeBytes(LINE_END)
            out.writeBytes(UPLOAD_PRESET)
            out.writeBytes(LINE_END)

            // Champ file
            out.writeBytes("$TWO_DASHES$BOUNDARY$LINE_END")
            out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"$LINE_END")
            out.writeBytes("Content-Type: $mime$LINE_END")
            out.writeBytes(LINE_END)

            // Écrit les bytes avec progression
            val chunkSize   = 8192
            var uploaded    = 0
            var offset      = 0
            while (offset < bytes.size) {
                val end = minOf(offset + chunkSize, bytes.size)
                out.write(bytes, offset, end - offset)
                uploaded += end - offset
                offset    = end
                val percent = (uploaded * 100 / bytes.size)
                onProgress?.invoke(percent)
            }

            out.writeBytes(LINE_END)
            out.writeBytes("$TWO_DASHES$BOUNDARY$TWO_DASHES$LINE_END")
            out.flush()
        }

        val responseCode = conn.responseCode
        if (responseCode != HttpURLConnection.HTTP_OK) {
            val err = conn.errorStream?.bufferedReader()?.readText() ?: "Erreur inconnue"
            error("Upload échoué ($responseCode): $err")
        }

        val response = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
        conn.disconnect()

        JSONObject(response).getString("secure_url")
    }

    /** Raccourci pour une image */
    suspend fun uploadImage(
        context: Context,
        uri: Uri,
        onProgress: ((Int) -> Unit)? = null
    ): String = upload(context, uri, MediaType.IMAGE, onProgress)

    /** Raccourci pour une vidéo */
    suspend fun uploadVideo(
        context: Context,
        uri: Uri,
        onProgress: ((Int) -> Unit)? = null
    ): String = upload(context, uri, MediaType.VIDEO, onProgress)
}