package com.kelme.fragment.profile
/**
 * Created by Gaurav Kumar on 13/07/21.
 */
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kelme.R
import com.kelme.databinding.BottomSheetMenuForProfileImageBinding

class BottomSheetOptionsForProfileImageFragment : BottomSheetDialogFragment() {
    private lateinit var binding:BottomSheetMenuForProfileImageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_menu_for_profile_image, container, false)//BottomSheetMenuForProfileImageBinding.inflate(inflater,container,false)
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

            mListener?.onItemClickProfile("open_camera")
        }

        binding.tvOpenGallery.setOnClickListener {
            dismissAllowingStateLoss()

            mListener?.onItemClickProfile("open_gallery")
        }
    }

    interface ItemClickListener {
        fun onItemClickProfile(item: String)
    }

    companion object {
        private var mListener: ItemClickListener? = null

        @JvmStatic
        fun newInstance(bundle: Bundle,mListener : ItemClickListener): BottomSheetOptionsForProfileImageFragment {
            val fragment = BottomSheetOptionsForProfileImageFragment()
            fragment.arguments = bundle
            Companion.mListener = mListener
            return fragment
        }
    }
}