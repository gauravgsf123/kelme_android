package com.kelme.activity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.kelme.R
import com.kelme.app.BaseActivity
import com.kelme.databinding.ActivityShowVideoImageBinding
import com.kelme.utils.Constants
import com.kelme.utils.Utils

class ShowVideoImageActivity : BaseActivity() {

    private lateinit var binding: ActivityShowVideoImageBinding
    private var url = ""

    private lateinit var simpleExoPlayer: SimpleExoPlayer
    private lateinit var mediaDataSourceFactory: DataSource.Factory

    private var isVideo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_show_video_image)
        initializerControl()
        setListener()
    }

    override fun initializerControl() {
        url = intent.extras?.getString(Constants.DATA).toString()
        Log.e("URL", url)

        if (url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png")
        )
        {
            binding.imageView.visibility = View.VISIBLE
            binding.playerView.visibility = View.GONE
            Utils.loadImage(this, binding.imageView, url)
        }
        else {
            binding.imageView.visibility = View.GONE
            binding.playerView.visibility = View.VISIBLE
            isVideo = true
        }
    }

    private fun setListener() {
        Utils.enableBackButton(this, binding.backArrow)
    }


    private fun initializePlayer() {
        mediaDataSourceFactory = DefaultDataSourceFactory(
            this, Util.getUserAgent(
                this,
                "mediaPlayerSample"
            )
        )
        val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(
            MediaItem.fromUri(url)
        )
        val mediaSourceFactory: MediaSourceFactory = DefaultMediaSourceFactory(
            mediaDataSourceFactory
        )

        simpleExoPlayer = SimpleExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()

        simpleExoPlayer.addMediaSource(mediaSource)

        binding.playerView.setShutterBackgroundColor(Color.TRANSPARENT)
        binding.playerView.player = simpleExoPlayer
        binding.playerView.requestFocus()

        simpleExoPlayer.prepare()
        simpleExoPlayer.playWhenReady = true
    }


    private fun releasePlayer() {
        simpleExoPlayer.release()
    }

    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23 && isVideo) initializePlayer()
    }

    public override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 && isVideo) initializePlayer()
    }

    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23 && isVideo) releasePlayer()
    }

    public override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23 && isVideo) releasePlayer()
    }
}