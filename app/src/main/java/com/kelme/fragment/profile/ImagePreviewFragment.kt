package com.kelme.fragment.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.kelme.R
import com.kelme.app.BaseFragment
import com.kelme.databinding.FragmentImagePreviewBinding
import com.kelme.model.response.DocumentData
import com.kelme.utils.Constants

class ImagePreviewFragment : BaseFragment() {
    private lateinit var binding:FragmentImagePreviewBinding
    private lateinit var documentData:DocumentData


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            documentData = it.getParcelable(Constants.DOCUMENT_DATA)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_image_preview,container,false)
        Glide.with(this).load(documentData.document).into(binding.imageview)
        return binding.root
    }

}