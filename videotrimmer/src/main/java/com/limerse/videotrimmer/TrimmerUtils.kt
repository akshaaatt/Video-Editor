package com.limerse.videotrimmer

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.ContextCompat
import java.io.File

object TrimmerUtils {
    fun formatCSeconds(timeInSeconds: Long): String {
        val hours = timeInSeconds / 3600
        val secondsLeft = timeInSeconds - hours * 3600
        val minutes = secondsLeft / 60
        val seconds = secondsLeft - minutes * 60
        var formattedTime = ""
        if (hours < 10) formattedTime += "0"
        formattedTime += "$hours:"
        if (minutes < 10) formattedTime += "0"
        formattedTime += "$minutes:"
        if (seconds < 10) formattedTime += "0"
        formattedTime += seconds
        return formattedTime
    }

    @JvmStatic
    fun getColor(context: Context?, color: Int): Int {
        return ContextCompat.getColor(context!!, color)
    }

    fun getDuration(context: Activity?, videoPath: Uri?): Long {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, videoPath)
            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val timeInMillisec = time!!.toLong()
            retriever.release()
            return timeInMillisec / 1000
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    fun getDurationMillis(context: Activity?, videoPath: Uri?): Long {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, videoPath)
            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val timeInMillisec = time!!.toLong()
            retriever.release()
            return timeInMillisec
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    fun getTrimType(trimType: TrimType?): Int {
        return when (trimType) {
            TrimType.FIXED_DURATION -> 1
            TrimType.MIN_DURATION -> 2
            TrimType.MIN_MAX_DURATION -> 3
            else -> 0
        }
    }

    fun getFileExtension(context: Context, uri: Uri): String {
        try {
            val extension: String?
            extension = if (uri.scheme != null && uri.scheme == ContentResolver.SCHEME_CONTENT) {
                val mime = MimeTypeMap.getSingleton()
                mime.getExtensionFromMimeType(context.contentResolver.getType(uri))
            } else MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path!!)).toString())
            return if (extension == null || extension.isEmpty()) ".mp4" else extension
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "mp4"
    }

    fun getFrameBySec(context: Activity?, videoPath: Uri?, millies: Long): Bitmap? {
        try {
            val formatted = millies.toString() + "000000"
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, videoPath)
            val bitmap = retriever.getFrameAtTime(formatted.toLong())
            retriever.release()
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getFrameRate(context: Activity?, videoPath: Uri?): Int {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, videoPath)
            val frameRate =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)!!
                    .toInt()
            retriever.release()
            return frameRate
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 30
    }

    fun getBitRate(context: Activity?, videoPath: Uri?): Int {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, videoPath)
            val bitRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)!!
                .toInt()
            retriever.release()
            return bitRate
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 15
    }

    fun getVideoWidthHeight(context: Activity?, videoPath: Uri?): IntArray? {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, videoPath)
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)!!
                .toInt()
            val height =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!!
                    .toInt()
            retriever.release()
            return intArrayOf(width, height)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun formatSeconds(timeInSeconds: Long): String {
        val hours = timeInSeconds / 3600
        val secondsLeft = timeInSeconds - hours * 3600
        val minutes = secondsLeft / 60
        val seconds = secondsLeft - minutes * 60
        var formattedTime = ""
        if (hours < 10 && hours != 0L) {
            formattedTime += "0"
            formattedTime += "$hours:"
        }
        if (minutes < 10) formattedTime += "0"
        formattedTime += "$minutes:"
        if (seconds < 10) formattedTime += "0"
        formattedTime += seconds
        return formattedTime
    }

    fun getLimitedTimeFormatted(secs: Long): String {
        val hours = secs / 3600
        val secondsLeft = secs - hours * 3600
        val minutes = secondsLeft / 60
        val seconds = secondsLeft - minutes * 60
        val time: String
        time = when {
            hours != 0L -> {
                hours.toString() + " Hrs " + (if (minutes != 0L) "$minutes Mins " else "") +
                        if (seconds != 0L) "$seconds Secs " else ""
            }
            minutes != 0L -> minutes.toString() + " Mins " + (if (seconds != 0L) "$seconds Secs " else "")
            else -> "$seconds Secs "
        }
        return time
    }

    fun clearNull(value: String?): String {
        return value?.trim { it <= ' ' } ?: ""
    }
}