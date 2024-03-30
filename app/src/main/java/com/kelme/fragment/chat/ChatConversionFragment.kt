package com.kelme.fragment.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kelme.app.BaseFragment
import com.kelme.databinding.FragmentChatConversionBinding


class ChatConversionFragment : BaseFragment() {
    private lateinit var binding:FragmentChatConversionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentChatConversionBinding.inflate(inflater,container,false)
        return binding.root
    }


}