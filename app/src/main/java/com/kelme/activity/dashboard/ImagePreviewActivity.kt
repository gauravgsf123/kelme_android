package com.kelme.activity.dashboard

import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.kelme.R
import com.kelme.app.BaseActivity
import com.kelme.databinding.ActivityImagePreviewBinding
import com.kelme.model.response.DocumentData
import com.kelme.utils.Constants

class ImagePreviewActivity : BaseActivity() {

    private lateinit var binding: ActivityImagePreviewBinding
    private lateinit var documentData: DocumentData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_preview)

        documentData = intent.extras?.getParcelable<DocumentData>(Constants.DATA)!!
        Glide.with(this).load(documentData.document).into(binding.imageView)
        binding.backArrow.setOnClickListener { onBackPressed() }
    }

    override fun initializerControl() {

    }
}