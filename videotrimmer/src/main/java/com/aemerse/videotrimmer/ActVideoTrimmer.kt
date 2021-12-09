package com.aemerse.videotrimmer

import android.app.Dialog
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.*
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.aemerse.rangeseekbar.interfaces.OnRangeSeekbarChangeListener
import com.aemerse.rangeseekbar.interfaces.OnRangeSeekbarFinalValueListener
import com.aemerse.rangeseekbar.interfaces.OnSeekbarFinalValueListener
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.aemerse.rangeseekbar.widgets.CrystalRangeSeekbar
import com.aemerse.rangeseekbar.widgets.CrystalSeekbar
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.aemerse.videotrimmer.FileUtils.getPath
import java.io.File
import java.util.*

class ActVideoTrimmer : AppCompatActivity() {
    private var playerView: PlayerView? = null
    private var videoPlayer: SimpleExoPlayer? = null
    private var imagePlayPause: ImageView? = null
    private lateinit var imageViews: Array<ImageView>
    private var totalDuration: Long = 0
    private var dialog: Dialog? = null
    private var uri: Uri? = null
    private var txtStartDuration: TextView? = null
    private var txtEndDuration: TextView? = null
    private var seekbar: CrystalRangeSeekbar? = null
    private var lastMinValue: Long = 0
    private var lastMaxValue: Long = 0
    private var menuDone: MenuItem? = null
    private var seekbarController: CrystalSeekbar? = null
    private var isValidVideo = true
    private var isVideoEnded = false
    private var seekHandler: Handler? = null
    private var currentDuration: Long = 0
    private var lastClickedTime: Long = 0
    private var compressOption: CompressOption? = null
    private var outputPath: String? = null
    private var destinationPath: String? = null
    private var trimType = 0
    private var fixedGap: Long = 0
    private var minGap: Long = 0
    private var minFromGap: Long = 0
    private var maxToGap: Long = 0
    private var hidePlayerSeek = false
    private var isAccurateCut = false
    private var fileName: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_video_trimmer)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        setUpToolBar(supportActionBar, getString(R.string.txt_edt_video))
        toolbar.setNavigationOnClickListener { finish() }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        playerView = findViewById(R.id.player_view_lib)
        imagePlayPause = findViewById(R.id.image_play_pause)
        seekbar = findViewById(R.id.range_seek_bar)
        txtStartDuration = findViewById(R.id.txt_start_duration)
        txtEndDuration = findViewById(R.id.txt_end_duration)
        seekbarController = findViewById(R.id.seekbar_controller)
        val imageOne = findViewById<ImageView>(R.id.image_one)
        val imageTwo = findViewById<ImageView>(R.id.image_two)
        val imageThree = findViewById<ImageView>(R.id.image_three)
        val imageFour = findViewById<ImageView>(R.id.image_four)
        val imageFive = findViewById<ImageView>(R.id.image_five)
        val imageSix = findViewById<ImageView>(R.id.image_six)
        val imageSeven = findViewById<ImageView>(R.id.image_seven)
        val imageEight = findViewById<ImageView>(R.id.image_eight)
        imageViews = arrayOf(
            imageOne, imageTwo, imageThree,
            imageFour, imageFive, imageSix, imageSeven, imageEight
        )
        seekHandler = Handler(Looper.getMainLooper())
        initPlayer()
        setDataInView()
    }

    private fun setUpToolBar(actionBar: ActionBar?, title: String) {
        try {
            actionBar!!.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.title = title
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initPlayer() {
        try {
            videoPlayer = SimpleExoPlayer.Builder(this).build()
            playerView!!.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            playerView!!.player = videoPlayer
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MOVIE)
                    .build()
                videoPlayer!!.setAudioAttributes(audioAttributes, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setDataInView() {
        try {
            uri = Uri.parse(intent.getStringExtra(TrimVideo.TRIM_VIDEO_URI))
            uri = Uri.parse(getPath(this, uri!!))
            totalDuration = TrimmerUtils.getDuration(this, uri)
            imagePlayPause!!.setOnClickListener { onVideoClicked() }
            playerView!!.videoSurfaceView!!.setOnClickListener { onVideoClicked() }
            validate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun validate() {
        try {
            val trimVideoOptions: TrimVideoOptions =
                intent.getParcelableExtra(TrimVideo.TRIM_VIDEO_OPTION)!!
            trimType = TrimmerUtils.getTrimType(trimVideoOptions.trimType)
            destinationPath = trimVideoOptions.destination
            fileName = trimVideoOptions.fileName
            hidePlayerSeek = trimVideoOptions.hideSeekBar
            isAccurateCut = trimVideoOptions.accurateCut
            compressOption = trimVideoOptions.compressOption
            fixedGap = trimVideoOptions.fixedDuration
            fixedGap = if (fixedGap != 0L) fixedGap else totalDuration
            minGap = trimVideoOptions.minDuration
            minGap = if (minGap != 0L) minGap else totalDuration
            if (trimType == 3) {
                minFromGap = trimVideoOptions.minToMax!![0]
                maxToGap = trimVideoOptions.minToMax!![1]
                minFromGap = if (minFromGap != 0L) minFromGap else totalDuration
                maxToGap = if (maxToGap != 0L) maxToGap else totalDuration
            }
            if (destinationPath != null) {
                val outputDir = File(destinationPath!!)
                outputDir.mkdirs()
                destinationPath = outputDir.toString()
                require(outputDir.isDirectory) { "Destination file path error $destinationPath" }
            }
            buildMediaSource(uri)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    private fun onVideoClicked() {
        try {
            if (isVideoEnded) {
                seekTo(lastMinValue)
                videoPlayer!!.playWhenReady = true
                return
            }
            if (currentDuration - lastMaxValue > 0) seekTo(lastMinValue)
            videoPlayer!!.playWhenReady = !videoPlayer!!.playWhenReady
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun seekTo(sec: Long) {
        if (videoPlayer != null) videoPlayer!!.seekTo(sec * 1000)
    }

    private fun buildMediaSource(mUri: Uri?) {
        try {
            val dataSourceFactory: DataSource.Factory =
                DefaultDataSourceFactory(this, getString(R.string.app_name))
            val mediaSource: MediaSource =
                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                    MediaItem.fromUri(
                        mUri!!
                    )
                )
            videoPlayer!!.addMediaSource(mediaSource)
            videoPlayer!!.prepare()
            videoPlayer!!.playWhenReady = true
            videoPlayer!!.addListener(object : Player.EventListener {
                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    imagePlayPause!!.visibility = if (playWhenReady) View.GONE else View.VISIBLE
                }

                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_ENDED -> {
                            imagePlayPause!!.visibility = View.VISIBLE
                            isVideoEnded = true
                        }
                        Player.STATE_READY -> {
                            isVideoEnded = false
                            startProgress()
                        }
                        Player.STATE_BUFFERING -> {}
                        Player.STATE_IDLE -> {}
                        else -> {
                        }
                    }
                }
            })
            setImageBitmaps()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setImageBitmaps() {
        try {
            val diff = totalDuration / 8
            var sec = 1
            for (img in imageViews) {
                val interval = diff * sec * 1000000
                val options = RequestOptions().frame(interval)
                Glide.with(this)
                    .load(intent.getStringExtra(TrimVideo.TRIM_VIDEO_URI))
                    .apply(options)
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .into(img)
                if (sec < totalDuration) sec++
            }
            seekbar!!.visibility = View.VISIBLE
            txtStartDuration!!.visibility = View.VISIBLE
            txtEndDuration!!.visibility = View.VISIBLE
            seekbarController!!.setMaxValue(totalDuration.toFloat()).apply()
            seekbar!!.setMaxValue(totalDuration.toFloat()).apply()
            seekbar!!.setMaxStartValue(totalDuration.toFloat()).apply()
            lastMaxValue = when (trimType) {
                1 -> {
                    seekbar!!.setFixGap(fixedGap.toFloat()).apply()
                    totalDuration
                }
                2 -> {
                    seekbar!!.setMaxStartValue(minGap.toFloat())
                    seekbar!!.setGap(minGap.toFloat()).apply()
                    totalDuration
                }
                3 -> {
                    seekbar!!.setMaxStartValue(maxToGap.toFloat())
                    seekbar!!.setGap(minFromGap.toFloat()).apply()
                    maxToGap
                }
                else -> {
                    seekbar!!.setGap(2f).apply()
                    totalDuration
                }
            }
            if (hidePlayerSeek) seekbarController!!.visibility = View.GONE
            seekbar!!.setOnRangeSeekbarFinalValueListener(object : OnRangeSeekbarFinalValueListener {
                override fun finalValue(minValue: Number?, maxValue: Number?) {
                    if (!hidePlayerSeek) seekbarController!!.visibility = View.VISIBLE
                }

            })
            seekbar!!.setOnRangeSeekbarChangeListener(object : OnRangeSeekbarChangeListener {
                override fun valueChanged(minValue: Number?, maxValue: Number?) {
                    val minVal = minValue as Long
                    val maxVal = maxValue as Long
                    if (lastMinValue != minVal) {
                        seekTo(minValue)
                        if (!hidePlayerSeek) seekbarController!!.visibility = View.INVISIBLE
                    }
                    lastMinValue = minVal
                    lastMaxValue = maxVal
                    txtStartDuration!!.text = TrimmerUtils.formatSeconds(minVal)
                    txtEndDuration!!.text = TrimmerUtils.formatSeconds(maxVal)
                    if (trimType == 3) setDoneColor(minVal, maxVal)
                }
            })

            seekbarController!!.setOnSeekbarFinalValueListener(object : OnSeekbarFinalValueListener {
                override fun finalValue(value: Number?) {
                    val value1 = value as Long
                    if (value1 in (lastMinValue + 1) until lastMaxValue) {
                        seekTo(value1)
                        return
                    }
                    if (value1 > lastMaxValue) seekbarController!!.setMinStartValue(lastMaxValue.toFloat()).apply() else if (value1 < lastMinValue) {
                        seekbarController!!.setMinStartValue(lastMinValue.toFloat()).apply()
                        if (videoPlayer!!.playWhenReady) seekTo(lastMinValue)
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setDoneColor(minVal: Long, maxVal: Long) {
        try {
            if (menuDone == null) return
            if (maxVal - minVal <= maxToGap) {
                menuDone!!.icon.colorFilter = PorterDuffColorFilter(
                    ContextCompat.getColor(this, R.color.colorWhite), PorterDuff.Mode.SRC_IN
                )
                isValidVideo = true
            } else {
                menuDone!!.icon.colorFilter = PorterDuffColorFilter(
                    ContextCompat.getColor(this, R.color.colorWhiteLt), PorterDuff.Mode.SRC_IN
                )
                isValidVideo = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        videoPlayer!!.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (videoPlayer != null) videoPlayer!!.release()
        stopRepeatingTask()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_done, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menuDone = menu.findItem(R.id.action_done)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_done) {
            if (SystemClock.elapsedRealtime() - lastClickedTime < 800) return true
            lastClickedTime = SystemClock.elapsedRealtime()
            validateVideo()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun validateVideo() {
        if (isValidVideo) {
            var path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + ""
            if (destinationPath != null) path = destinationPath!!
            var fileNo = 0
            var fName = "trimmed_video_"
            if (fileName != null && fileName!!.isNotEmpty()) fName = fileName!!
            var newFile = File(
                path + File.separator +
                        fName + "." + TrimmerUtils.getFileExtension(this, uri!!)
            )
            while (newFile.exists()) {
                fileNo++
                newFile = File(
                    path + File.separator +
                            (fileName + fileNo) + "." + TrimmerUtils.getFileExtension(this, uri!!)
                )
            }
            outputPath = newFile.toString()

            videoPlayer!!.playWhenReady = false
            showProcessingDialog()
            val complexCommand: Array<String?> = when {
                compressOption != null -> compressionCommand
                isAccurateCut -> accurateBinary
                else -> {
                    arrayOf(
                        "-ss", TrimmerUtils.formatCSeconds(lastMinValue),
                        "-i", uri.toString(),
                        "-t",
                        TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue),
                        "-async", "1", "-strict", "-2", "-c", "copy", outputPath
                    )
                }
            }
            execFFmpegBinary(complexCommand, true)
        } else Toast.makeText(
            this,
            getString(R.string.txt_smaller) + " " + TrimmerUtils.getLimitedTimeFormatted(maxToGap),
            Toast.LENGTH_SHORT
        ).show()
    }

    //Default compression option
    private val compressionCommand: Array<String?> get() {
            val metaRetriever = MediaMetadataRetriever()
            metaRetriever.setDataSource(uri.toString())
            val height =
                metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            val width =
                metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            var w = if (TrimmerUtils.clearNull(width).isEmpty()) 0 else width!!.toInt()
            var h = height!!.toInt()

            //Default compression option
            return if (compressOption!!.widthHere != 0 || compressOption!!.heightHere != 0 || compressOption!!.bitRate != "0k") {
                arrayOf(
                    "-ss", TrimmerUtils.formatCSeconds(lastMinValue),
                    "-i", uri.toString(), "-s", compressOption!!.widthHere.toString() + "x" +
                            compressOption!!.heightHere,
                    "-r", compressOption!!.frameRate.toString(),
                    "-vcodec", "mpeg4", "-b:v",
                    compressOption!!.bitRate, "-b:a", "48000", "-ac", "2", "-ar",
                    "22050", "-t",
                    TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue), outputPath
                )
            } else if (w >= 800) {
                w /= 2
                h = height.toInt() / 2
                arrayOf(
                    "-ss", TrimmerUtils.formatCSeconds(lastMinValue),
                    "-i", uri.toString(),
                    "-s", w.toString() + "x" + h, "-r", "30",
                    "-vcodec", "mpeg4", "-b:v",
                    "1M", "-b:a", "48000", "-ac", "2", "-ar", "22050",
                    "-t",
                    TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue), outputPath
                )
            } else {
                arrayOf(
                    "-ss", TrimmerUtils.formatCSeconds(lastMinValue),
                    "-i", uri.toString(), "-s", w.toString() + "x" + h, "-r",
                    "30", "-vcodec", "mpeg4", "-b:v",
                    "400K", "-b:a", "48000", "-ac", "2", "-ar", "22050",
                    "-t",
                    TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue), outputPath
                )
            }
        }

    private fun execFFmpegBinary(command: Array<String?>, retry: Boolean) {
        try {
            FFmpeg.executeAsync(command) { executionId1: Long, returnCode: Int ->
                if (returnCode == Config.RETURN_CODE_SUCCESS) {
                    dialog!!.dismiss()
                    val intent = Intent()
                    intent.putExtra(TrimVideo.TRIMMED_VIDEO_PATH, outputPath)
                    setResult(RESULT_OK, intent)
                    finish()
                } else if (returnCode == Config.RETURN_CODE_CANCEL) {
                    if (dialog!!.isShowing) dialog!!.dismiss()
                } else {
                    if (retry && !isAccurateCut && compressOption == null) {
                        val newFile = File(outputPath!!)
                        if (newFile.exists()) newFile.delete()
                        execFFmpegBinary(accurateBinary, false)
                    } else {
                        if (dialog!!.isShowing) dialog!!.dismiss()
                        runOnUiThread {
                            Toast.makeText(
                                this@ActVideoTrimmer,
                                "Failed to trim",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val accurateBinary: Array<String?> get() = arrayOf(
            "-ss", TrimmerUtils.formatCSeconds(lastMinValue), "-i", uri.toString(), "-t",
            TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue),
            "-async", "1", outputPath
        )

    private fun showProcessingDialog() {
        try {
            dialog = Dialog(this)
            dialog!!.setCancelable(false)
            dialog!!.setContentView(R.layout.alert_convert)
            val txtCancel = dialog!!.findViewById<TextView>(R.id.txt_cancel)
            dialog!!.setCancelable(false)
            dialog!!.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            txtCancel.setOnClickListener {
                dialog!!.dismiss()
                FFmpeg.cancel()
            }
            dialog!!.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun startProgress() {
        updateSeekbar.run()
    }

    private fun stopRepeatingTask() {
        seekHandler!!.removeCallbacks(updateSeekbar)
    }

    private var updateSeekbar: Runnable = object : Runnable {
        override fun run() {
            try {
                currentDuration = videoPlayer!!.currentPosition / 1000
                if (!videoPlayer!!.playWhenReady) return
                if (currentDuration <= lastMaxValue) seekbarController!!.setMinStartValue(currentDuration.toFloat()).apply() else videoPlayer!!.playWhenReady = false
            } finally {
                seekHandler!!.postDelayed(this, 1000)
            }
        }
    }
}