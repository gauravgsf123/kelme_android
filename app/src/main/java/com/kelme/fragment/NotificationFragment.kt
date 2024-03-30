package com.kelme.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.kelme.R
import com.kelme.activity.dashboard.DashboardActivity
import com.kelme.activity.dashboard.DashboardViewModal
import com.kelme.activity.login.LoginActivity
import com.kelme.adapter.NotificationAdapter
import com.kelme.databinding.FragmentNotificationBinding
import com.kelme.event.ClearEvent
import com.kelme.fragment.security.SecurityAlertsFragment
import com.kelme.interfaces.ItemClickListener
import com.kelme.model.NotificationModel
import com.kelme.model.request.NotificationDeleteRequest
import com.kelme.utils.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class NotificationFragment : Fragment(), RecyclerItemTouchHelper.RecyclerItemTouchHelperListener, NotificationAdapter.onClickListener  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    private lateinit var binding: FragmentNotificationBinding
    private lateinit var viewModal: DashboardViewModal
    private lateinit var notificationAdapter: NotificationAdapter
    private var list: ArrayList<NotificationModel> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notification, container, false)
        viewModal = ViewModelProvider(this, ViewModalFactory(activity?.application!!)).get(
            DashboardViewModal::class.java
        )
        setUI()
        setObserver()
        notificationList()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
        (activity as DashboardActivity?)?.run {
            setTitle("Notifications")
            hideNotificationIcon()
            showBackArrow()
            showClearBtn()
        }
    }

    override fun onStop() {
        super.onStop()
        (activity as DashboardActivity?)?.run {
            hideClearBtn()
        }
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ClearEvent) {
        notificationDeleteAll()
    }

    private fun setUI() {
        notificationAdapter = NotificationAdapter(requireContext(),list)
        binding.rvNotification.adapter = notificationAdapter
        (activity as DashboardActivity?)?.run {
            hideUnreadCount()
        }

        notificationAdapter.onItemClick(object : ItemClickListener {
            override fun onClick(position: Int, view: View?) {
                if(view!=null){
                    if(list[position].module_type=="2"||list[position].module_type=="3"||list[position].module_type=="7") {
                        if (list[position].longitude != "" && list[position].latitude != "") {
                            PrefManager.write(PrefManager.NLONGITUDE, list[position].longitude)
                            PrefManager.write(PrefManager.NLATITUDE, list[position].latitude)
                            PrefManager.write(PrefManager.NTITLE, list[position].message)
                            PrefManager.write(PrefManager.NMODULE, list[position].module_type)
                            PrefManager.write(PrefManager.NSAFETY, list[position].saftey_check)
                            PrefManager.write(PrefManager.NFIREBASEID, list[position].firebase_id)
                            (activity as DashboardActivity).replaceFragment(
                                MapsNotificationFragment(),
                                Bundle.EMPTY
                            )
                        }
                    }else if(list[position].module_type=="1"||list[position].module_type=="4"){
                        PrefManager.write(PrefManager.MODULEID, list[position].module_id)
                        (activity as DashboardActivity).replaceFragment(
                            SecurityAlertsFragment(),
                            Bundle.EMPTY
                        )
                    }
                }else {
                    if (list[position].attachment != "") {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(list[position].attachment))
                        startActivity(browserIntent)
                    }
                }
            }
        })
    }


    private fun setObserver() {
        viewModal.logout.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    ProgressDialog.hideProgressBar()
                    PrefManager.clearUserPref()
//                    Toast.makeText(
//                        requireContext(),
//                        response.message,
//                        Toast.LENGTH_SHORT
//                    ).show()
                    startActivity(
                        Intent(
                            activity,
                            LoginActivity::class.java
                        )
                    )
                    activity?.finish()
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(requireContext())
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
                    if (response.message == "240") {
                        PrefManager.clearUserPref()
                        startActivity(
                            Intent(
                                activity,
                                LoginActivity::class.java
                            )
                        )
                        activity?.finish()
                    }
                }
            }
        }

        viewModal.notificationList.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        startActivity(
                            Intent(
                                activity,
                                LoginActivity::class.java
                            )
                        )
                        activity?.finish()
                    } else {
                        ProgressDialog.hideProgressBar()
                        list.clear()

                        list = response.data?.notification_data as ArrayList<NotificationModel>
                        notificationAdapter.setNotificationList(list)
                        if (list.size == 0) {
//                        Toast.makeText(
//                            requireContext(),
//                            R.string.no_data,
//                            Toast.LENGTH_SHORT
//                        ).show()
                        }

                    }
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(requireContext())
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
                        if (response.message == "240") {
                            PrefManager.clearUserPref()
                            startActivity(
                                Intent(
                                    activity,
                                    LoginActivity::class.java
                                )
                            )
                            activity?.finish()
                    } else {
//                        Toast.makeText(
//                            requireContext(),
//                            response.message,
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }
                }
            }
        }

        viewModal.notificationDeleteAll.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        startActivity(
                            Intent(
                                activity,
                                LoginActivity::class.java
                            )
                        )
                        activity?.finish()
                    } else {
                        ProgressDialog.hideProgressBar()
//                    Toast.makeText(
//                        requireContext(),
//                        response.message,
//                        Toast.LENGTH_SHORT
//                    ).show()
                    }
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(requireContext())
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
                    if (response.message == "240") {
                        PrefManager.clearUserPref()
                        startActivity(
                            Intent(
                                activity,
                                LoginActivity::class.java
                            )
                        )
                        activity?.finish()
                    }else {
//                        Toast.makeText(
//                            requireContext(),
//                            response.message,
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }
                }
            }
        }
    }

    private fun notificationList()
    {
        viewModal.notificationList()
    }

    private fun notificationDelete(position: Int)
    {
        val request = NotificationDeleteRequest(list[position].incoming_notification_id.toInt())
        viewModal.notificationDelete(request)
    }

    private fun notificationDeleteAll() {
        val request = NotificationDeleteRequest(2)
        viewModal.notificationDeleteAll(request)
        list.clear()
        notificationAdapter.setNotificationList(list)
    }

    override fun onDeleteClick(position: Int)
    {
       notificationDelete(position)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int, position: Int)
    {
        TODO("Not yet implemented")
      //  notificationDelete(position)
    }
}