package com.kelme.fragment.country_detail_tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.kelme.R
import com.kelme.activity.dashboard.DashboardActivity
import com.kelme.adapter.AlertAdapter
import com.kelme.app.BaseFragment
import com.kelme.databinding.FragmentAlertBinding
import com.kelme.fragment.security.SecurityAlertsDetailFragment
import com.kelme.interfaces.ItemClickListener
import com.kelme.model.AlertModel
import com.kelme.model.response.SecrityAlertListData
import com.kelme.utils.Constants

class AlertFragment(var bundle: Bundle) : BaseFragment() {
    private lateinit var binding: FragmentAlertBinding
    private lateinit var data: ArrayList<SecrityAlertListData>
    private lateinit var adapter: AlertAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        data = bundle.getParcelableArrayList("data")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_alert, container, false)

        adapter = AlertAdapter()
        binding.recyclerviewAlert.adapter = adapter
        adapter.setItems(data)

        adapter.onItemClick(object : ItemClickListener {
            override fun onClick(position: Int, view: View?) {
                val bundle = Bundle()
                bundle.putString(Constants.SECURITY_ALERT_MODEL, data[position].securityAlertId)
                (activity as DashboardActivity).addFragment(
                    SecurityAlertsDetailFragment(),
                    bundle
                )
            }
        })
        return binding.root
    }


}