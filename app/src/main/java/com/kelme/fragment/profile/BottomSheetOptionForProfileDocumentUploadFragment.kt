package com.kelme.fragment.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kelme.R
import com.kelme.databinding.FragmentBottomSheetOptionForDocumentUploadBinding

class BottomSheetOptionForProfileDocumentUploadFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentBottomSheetOptionForDocumentUploadBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  DataBindingUtil.inflate(inflater, R.layout.fragment_bottom_sheet_option_for_document_upload, container, false)//BottomSheetMenuForProfileImageBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
    }

    private fun setUpViews() {
        // We can have cross button on the top right corner for providing elemnet to dismiss the bottom sheet
        binding.tvCancel.setOnClickListener { dismissAllowingStateLoss() }
        binding.tvOpenCamera.setOnClickListener {
            dismissAllowingStateLoss()

            mListener.onItemClick("open_camera")
        }

        binding.tvOpenGallery.setOnClickListener {
            dismissAllowingStateLoss()

            mListener?.onItemClick("open_gallery")
        }

        binding.tvOpenDocument.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }

    interface ItemClickListener {
        fun onItemClick(item: String)
    }

    companion object {
        private lateinit var mListener: ItemClickListener

        @JvmStatic
        fun newInstance(bundle: Bundle, mListener : ItemClickListener): BottomSheetOptionForProfileDocumentUploadFragment {
            val fragment = BottomSheetOptionForProfileDocumentUploadFragment()
            fragment.arguments = bundle
            Companion.mListener = mListener
            return fragment
        }
    }
}