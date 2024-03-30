package com.kelme.dialogs

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.kelme.R

open class BaseDialogFragment : DialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogStyle)
    }
}