package com.kelme.dialogs

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import com.kelme.R
import com.kelme.databinding.PositiveBtnDialogLikeIosBinding

class SingleButtonDialogLikeIos(
    @param:SuppressLint("SupportAnnotationUsage") @param:StringRes private val title: String,
    @param:SuppressLint("SupportAnnotationUsage") @param:StringRes private val description: String,
    private val mListener: OnClickListener?
) : BaseDialogFragment() {
    private val onClickListener = View.OnClickListener { v ->
        if (mListener != null) {
            if (v.id == R.id.text_positive) {
                dismiss()
               // mListener.onConfirmationDialogButtonClick(DialogInterface.BUTTON_POSITIVE)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            LayoutInflater.from(activity).inflate(R.layout.positive_btn_dialog_like_ios, null)
        val binding: PositiveBtnDialogLikeIosBinding = DataBindingUtil.bind(view)!!
        binding.textTitle.text = title
        binding.textDescription.text = description
        binding.textPositive.setOnClickListener(onClickListener)
        if (dialog != null) {
            if (dialog!!.window != null) {
                dialog!!.setCancelable(false)
                dialog!!.setCanceledOnTouchOutside(false)
                dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                if (TextUtils.isEmpty(title)) binding.textTitle.visibility =
                    View.GONE else binding.textTitle.visibility = View.VISIBLE
            }
        }
        return binding.root
    }

    interface OnClickListener {
        fun onConfirmationDialogButtonClick(which: Int)
    }
}