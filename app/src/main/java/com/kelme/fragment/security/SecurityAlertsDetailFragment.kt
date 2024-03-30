package com.kelme.fragment.security

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.kelme.R
import com.kelme.activity.ShowMapEventLocationActivity
import com.kelme.activity.ShowVideoImageActivity
import com.kelme.activity.dashboard.DashboardActivity
import com.kelme.activity.login.LoginActivity
import com.kelme.databinding.FragmentSecurityAlertsDetailBinding
import com.kelme.fragment.country_detail_tab.OverAllRiskLevelFragment
import com.kelme.model.SecurityAlertDetailsModel
import com.kelme.model.SubCategory
import com.kelme.model.request.SafetyCheckAlertRequest
import com.kelme.model.request.SecurityAlertDetailsRequest
import com.kelme.model.response.CountryRiskLevelData
import com.kelme.model.response.SecrityAlertListData
import com.kelme.utils.*

class SecurityAlertsDetailFragment : Fragment() {

    private lateinit var binding: FragmentSecurityAlertsDetailBinding
    private lateinit var viewModal: SecurityViewModal

    private var securityAlertModel = ""
    private lateinit var countryRiskLevelData: CountryRiskLevelData

    private var listSubCategory: ArrayList<SubCategory> = ArrayList()
    private var listSecurityAlert: ArrayList<SecrityAlertListData> = ArrayList()
    private lateinit var model: SecurityAlertDetailsModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            securityAlertModel = it.getString(Constants.SECURITY_ALERT_MODEL)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_security_alerts_detail,
                container,
                false
            )

        viewModal = ViewModelProvider(this, ViewModalFactory(activity?.application!!)).get(
            SecurityViewModal::class.java
        )

        setUI()
        setObserver()
        securityAlertList()
        setListener()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        (activity as DashboardActivity?)?.run {
            setTitle("")
            hideNotificationIcon()
            showBackArrow()
            showSearchBar()
            hideSearchBar()
            //setPositiveToolbarPadding()
            hideFilter()
        }
    }

    private fun setUI() {
        binding.tvCountry.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable(Constants.DATA, countryRiskLevelData)
            bundle.putParcelableArrayList(
                Constants.SECURITY_ALERT_DATA_LIST,
                listSecurityAlert as ArrayList<out Parcelable>
            )
            bundle.putParcelableArrayList(
                Constants.RISK_LIST,
                listSubCategory as ArrayList<out Parcelable>
            )
            (activity as DashboardActivity).replaceFragment(OverAllRiskLevelFragment(), bundle)
        }

        binding.tvLocation.setOnClickListener {
            val intent = Intent(context, ShowMapEventLocationActivity::class.java)
            intent.putExtra("Risk_type", model.risk_type_id)
            intent.putExtra("Risk_category", model.risk_category)
            intent.putExtra("Location", model.location)
            startActivity(intent)
        }
    }

    private fun setObserver() {
        viewModal.logout.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        binding.loader.visibility = View.INVISIBLE
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
                    } else {
                        binding.loader.visibility = View.INVISIBLE
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
                }
                is Resource.Loading -> {
                    binding.loader.visibility = View.VISIBLE
                }
                is Resource.Error -> {
                    binding.loader.visibility = View.INVISIBLE
//                    Toast.makeText(
//                        requireContext(),
//                        response.message,
//                        Toast.LENGTH_SHORT
//                    ).show()
                    if (response.message == "Your session has been expired, Please login again.") {
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

        viewModal.securityAlertDetails.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        binding.loader.visibility = View.INVISIBLE
                        model = response.data as SecurityAlertDetailsModel
                        (activity as DashboardActivity?)?.run {
                            // setBackground(R.color.country_outlook_background)
                            //  setTitle(model.title)
                        }
                        binding.tvTime.text =
                            Utils.convertTimeStampToDate(model.created_at.toLong(), "dd MMM yyyy")
                        binding.tvCountry.text = model.country_name
                        binding.tvLocation.text = model.location
                        binding.tvCategory.text = model.category_name
                        binding.tvTitleData.text = model.title

                        if (model.background.isEmpty()) {
                            binding.clBackground.visibility = View.GONE
                        } else {
                            binding.clBackground.visibility = View.VISIBLE
                            // binding.tvBackground.text = model.background
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                binding.tvBackground.text =
                                    (Html.fromHtml(model.background, Html.FROM_HTML_MODE_COMPACT))
                            } else {
                                binding.tvBackground.text = (Html.fromHtml(model.background))
                            }
                        }

                        if (model.risk_description.isEmpty()) {
                            binding.clEventOverview.visibility = View.GONE
                        } else {
                            binding.clEventOverview.visibility = View.VISIBLE
                            // binding.tvEventOverview.text = model.risk_description
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                binding.tvEventOverview.text =
                                    (Html.fromHtml(
                                        model.risk_description,
                                        Html.FROM_HTML_MODE_COMPACT
                                    ))
                            } else {
                                binding.tvEventOverview.text =
                                    (Html.fromHtml(model.risk_description))
                            }
                        }

                        if (model.analysis.isEmpty()) {
                            binding.clAnalysis.visibility = View.GONE
                        } else {
                            binding.clAnalysis.visibility = View.VISIBLE
                            // binding.tvAnalysis.text = model.analysis
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                binding.tvAnalysis.text =
                                    (Html.fromHtml(model.analysis, Html.FROM_HTML_MODE_COMPACT))
                            } else {
                                binding.tvAnalysis.text = (Html.fromHtml(model.analysis))
                            }
                        }

                        if (model.security_advice.isEmpty()) {
                            binding.clSecurityAdvice.visibility = View.GONE
                        } else {
                            binding.clSecurityAdvice.visibility = View.VISIBLE
                            // binding.tvSecurityAdvice.text = model.security_advice
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                binding.tvSecurityAdvice.text =
                                    (Html.fromHtml(
                                        model.security_advice,
                                        Html.FROM_HTML_MODE_COMPACT))
                            } else {
                                binding.tvSecurityAdvice.text =
                                    (Html.fromHtml(model.security_advice))
                            }
                        }

                        if (model.intel_gathering.isEmpty()) {
                            binding.clIntelGathering.visibility = View.GONE
                        } else {
                            binding.clIntelGathering.visibility = View.VISIBLE
                            //binding.tvIntelGathering.text = model.intel_gathering
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                binding.tvIntelGathering.text =
                                    (Html.fromHtml(
                                        model.intel_gathering,
                                        Html.FROM_HTML_MODE_COMPACT
                                    ))
                            } else {
                                binding.tvIntelGathering.text =
                                    (Html.fromHtml(model.intel_gathering))
                            }
                        }

                        if (model.forcast.isEmpty()) {
                            binding.clforcast.visibility = View.GONE
                        } else {
                            binding.clforcast.visibility = View.VISIBLE
                            // binding.tvforcast.text = model.forcast
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                binding.tvforcast.text =
                                    (Html.fromHtml(model.forcast, Html.FROM_HTML_MODE_COMPACT))
                            } else {
                                binding.tvforcast.text = (Html.fromHtml(model.forcast))
                            }
                        }

                        binding.tvRiskLevel.text = model.risk_type
                        if (model.risk_type == "High Risk") {
                            binding.tvRiskLevel.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.high
                                )
                            )
                        }
                        if (model.risk_type == "Low Risk") {
                            binding.tvRiskLevel.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.low
                                )
                            )
                        }
                        if (model.risk_type == "Minimal Risk") {
                            binding.tvRiskLevel.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.minimal
                                )
                            )
                        }
                        if (model.risk_type == "Moderate Risk") {
                            binding.tvRiskLevel.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.moderate
                                )
                            )
                        }

                        if (model.safety_check == "1") {
                            binding.clSafeDanger.visibility = View.VISIBLE
                        } else {
                            binding.clSafeDanger.visibility = View.GONE
                        }

                        if (model.media_file.isEmpty()) {
                            binding.clImageVideo.visibility = View.GONE
                        } else {
                            binding.clImageVideo.visibility = View.VISIBLE

                            if (model.media_file.endsWith(".jpg") || model.media_file.endsWith(".jpeg") || model.media_file.endsWith(
                                    ".png")
                            ) {
                                binding.ivVideoPlay.visibility = View.GONE
                                Glide.with(this)
                                    .load(model.media_file)
                                    .listener(object : RequestListener<Drawable?> {
                                        override fun onResourceReady(
                                            resource: Drawable?,
                                            model: Any?,
                                            target: Target<Drawable?>?,
                                            dataSource: DataSource?,
                                            isFirstResource: Boolean
                                        ): Boolean {
                                            binding.progress.visibility = View.GONE
                                            return false
                                        }

                                        override fun onLoadFailed(
                                            e: GlideException?,
                                            model: Any?,
                                            target: Target<Drawable?>?,
                                            isFirstResource: Boolean
                                        ): Boolean {
                                            binding.progress.visibility = View.GONE
                                            return false
                                        }
                                    })
                                    .into(binding.image)
                            } else if (model.media_file.endsWith(".mov") || model.media_file.endsWith(
                                    ".mp4"
                                )
                            ) {
                                binding.ivVideoPlay.visibility = View.VISIBLE
                                val bm = retrieveVideoFrameFromVideo(model.media_file)
                                binding.image.setImageBitmap(bm)

                            }
                            /* val requestOptions = RequestOptions()
                        requestOptions.isMemoryCacheable
                        Glide.with(this).setDefaultRequestOptions(requestOptions).load(model.media_file)
                            .into(binding.image)*/
                            Log.d("file_url", model.media_file)
                        }

                        countryRiskLevelData = CountryRiskLevelData(
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            null,
                            null,
                            null,
                        )
                        countryRiskLevelData.riskTypeName = model.risk_type_name
                        countryRiskLevelData.countryName = model.country_name
                        countryRiskLevelData.countryFlag = model.country_flag
                        countryRiskLevelData.countryId = model.country_id

                        listSubCategory.clear()
                        listSubCategory = model.sub_category as ArrayList<SubCategory>
                        listSecurityAlert.clear()
                        listSubCategory.forEach {
                            val data = SecrityAlertListData(
                                model.security_alert_id,
                                it.country_management_id,
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                ""
                            )
                            listSecurityAlert.add(data)
                        }
                    }
                }
                 is Resource.Loading -> {
                     binding.loader.visibility = View.VISIBLE
                }
                is Resource.Error -> {
                    binding.loader.visibility = View.INVISIBLE
                    if (response.message == "240") {
                        viewModal.logout()
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

        viewModal.safetyCheckAlerts.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        binding.loader.visibility = View.INVISIBLE
                    Toast.makeText(
                        requireContext(),
                        response.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    }
                }
                is Resource.Loading -> {
                  //  binding.loader.visibility = View.VISIBLE
                }
                is Resource.Error -> {
                    binding.loader.visibility = View.INVISIBLE
                    if (response.message == "240") {
                        viewModal.logout()
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
    }

    fun retrieveVideoFrameFromVideo(videoPath: String?): Bitmap? {
        var bitmap: Bitmap? = null
        var mediaMetadataRetriever: MediaMetadataRetriever? = null
        try {
            mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(videoPath, HashMap<String, String>())
            bitmap = mediaMetadataRetriever.frameAtTime
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaMetadataRetriever?.release()
        }
        return bitmap
    }

    private fun securityAlertList() {
        val request = SecurityAlertDetailsRequest(
            securityAlertModel.toInt()
        )
        viewModal.securityAlertDetails(request)
        PrefManager.write(PrefManager.MODULEID, "")
    }

    private fun setListener() {
        binding.image.setOnClickListener {
            callVideoImageView()
        }
        binding.ivVideoPlay.setOnClickListener {
            callVideoImageView()
        }

        binding.cvIAmSafe.setOnClickListener {
            val request = SafetyCheckAlertRequest(
                securityAlertModel,
                "1",
                ""+PrefManager.read(PrefManager.SLATITUDE,""),
                ""+PrefManager.read(PrefManager.SLONGITUDE,"")
            )
            viewModal.safetyCheckAlert(request)
        }

        binding.cvInDanger.setOnClickListener {
            val request = SafetyCheckAlertRequest(
                securityAlertModel,
                "2",
                ""+PrefManager.read(PrefManager.SLATITUDE,""),
                ""+PrefManager.read(PrefManager.SLONGITUDE,"")
            )
            viewModal.safetyCheckAlert(request)
        }
    }

    private fun callVideoImageView(){
        val intent = Intent(context, ShowVideoImageActivity::class.java)
        intent.putExtra(Constants.DATA, model.media_file)
        startActivity(intent)
    }

    override fun onStop() {
        super.onStop()
        (activity as DashboardActivity?)?.run {
            showSearchBar() }
    }

}