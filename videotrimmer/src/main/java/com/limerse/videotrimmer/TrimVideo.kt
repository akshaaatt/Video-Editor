package com.limerse.videotrimmer

import android.app.Activity
import android.content.Intent
import androidx.annotation.Keep
import androidx.fragment.app.Fragment

@Keep
object TrimVideo {
    var VIDEO_TRIMMER_REQ_CODE = 324
    const val TRIM_VIDEO_OPTION = "trim_video_option"
    const val TRIM_VIDEO_URI = "trim_video_uri"
    const val TRIMMED_VIDEO_PATH = "trimmed_video_path"
    fun activity(uri: String?): ActivityBuilder {
        return ActivityBuilder(uri)
    }

    fun getTrimmedVideoPath(intent: Intent): String? {
        return intent.getStringExtra(TRIMMED_VIDEO_PATH)
    }

    class ActivityBuilder(private val videoUri: String?) {
        private val options: TrimVideoOptions = TrimVideoOptions()
        fun setTrimType(trimType: TrimType?): ActivityBuilder {
            options.trimType = trimType
            return this
        }

        fun setHideSeekBar(hide: Boolean): ActivityBuilder {
            options.hideSeekBar = hide
            return this
        }

        fun setCompressOption(compressOption: CompressOption?): ActivityBuilder {
            options.compressOption = compressOption
            return this
        }

        fun setFileName(fileName: String?): ActivityBuilder {
            options.fileName = fileName
            return this
        }

        fun setAccurateCut(accurate: Boolean): ActivityBuilder {
            options.accurateCut = accurate
            return this
        }

        fun setMinDuration(minDuration: Long): ActivityBuilder {
            options.minDuration = minDuration
            return this
        }

        fun setFixedDuration(fixedDuration: Long): ActivityBuilder {
            options.fixedDuration = fixedDuration
            return this
        }

        fun setMinToMax(min: Long, max: Long): ActivityBuilder {
            options.minToMax = longArrayOf(min, max)
            return this
        }

        fun setDestination(destination: String?): ActivityBuilder {
            options.destination = destination
            return this
        }

        fun start(activity: Activity) {
            validate()
            activity.startActivityForResult(getIntent(activity), VIDEO_TRIMMER_REQ_CODE)
        }

        fun start(fragment: Fragment) {
            validate()
            fragment.startActivityForResult(getIntent(fragment.activity), VIDEO_TRIMMER_REQ_CODE)
        }

        private fun validate() {
            if (videoUri == null) throw NullPointerException("VideoUri cannot be null.")
            require(videoUri.isNotEmpty()) { "VideoUri cannot be empty" }
            if (options.trimType == null) throw NullPointerException("TrimType cannot be null")
            require(options.minDuration >= 0) { "Cannot set min duration to a number < 1" }
            require(options.fixedDuration >= 0) { "Cannot set fixed duration to a number < 1" }
            require(!(options.trimType == TrimType.MIN_MAX_DURATION && options.minToMax == null)) {
                "Used trim type is TrimType.MIN_MAX_DURATION." +
                        "Give the min and max duration"
            }
            if (options.minToMax != null) {
                require(!(options.minToMax!![0] < 0 || options.minToMax!![1] < 0)) { "Cannot set min to max duration to a number < 1" }
                require(options.minToMax!![0] <= options.minToMax!![1]) { "Minimum duration cannot be larger than max duration" }
                require(options.minToMax!![0] != options.minToMax!![1]) { "Minimum duration cannot be same as max duration.Use Fixed duration" }
            }
        }

        private fun getIntent(activity: Activity?): Intent {
            val intent = Intent(activity, ActVideoTrimmer::class.java)
            intent.putExtra(TRIM_VIDEO_URI, videoUri)
            intent.putExtra(TRIM_VIDEO_OPTION, options)
            return intent
        }

        init {
            options.trimType = TrimType.DEFAULT
        }
    }
}