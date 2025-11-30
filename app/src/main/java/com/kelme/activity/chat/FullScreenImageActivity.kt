package com.kelme.activity.chat

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.kelme.R
import android.view.ScaleGestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.github.chrisbanes.photoview.PhotoViewAttacher

class FullScreenImageActivity : AppCompatActivity() {

    private var mScaleGestureDetector: ScaleGestureDetector? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cl_top)) { v, insets ->
            val statusBarTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            // extension from androidx.core.view: updatePadding
            v.updatePadding(top = statusBarTop)
            insets
        }
        val backArrow = findViewById<ImageView>(R.id.backArrow)

        mScaleGestureDetector = ScaleGestureDetector(this, ScaleListener())

        backArrow.setOnClickListener { onBackPressed() }

        val intent = intent
        val stringUrl = intent.getStringExtra("image")

        val ivBack = findViewById<ImageView>(R.id.backArrow)
        ivBack.setOnClickListener {
            onBackPressed()
        }

        val myImage = findViewById<ImageView>(R.id.myImage)
        Glide.with(this).load(stringUrl)
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_foreground)
            .into(myImage)

       // val mAttacher = PhotoViewAttacher(myImage)
       // mAttacher.update()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return mScaleGestureDetector!!.onTouchEvent(event!!)
    }

    private class ScaleListener : SimpleOnScaleGestureListener() {
        var mScaleFactor = 1.0f
        // when a scale gesture is detected, use it to resize the image
        override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
            mScaleFactor *= scaleGestureDetector.scaleFactor
            return true
        }
    }
}