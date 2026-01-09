package com.kelme.fragment

import android.Manifest
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.kelme.R
import com.kelme.activity.dashboard.DashboardActivity
import com.kelme.activity.dashboard.DashboardViewModal
import com.kelme.activity.login.LoginActivity
import com.kelme.adapter.StringRecyclerAdapter
import com.kelme.databinding.FragmentCheckInBinding
import com.kelme.databinding.PopupDistanceBinding
import com.kelme.interfaces.ItemClickListener
import com.kelme.interfaces.MyLocationCallback
import com.kelme.model.request.CheckInRequest
import com.kelme.utils.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class CheckInFragment : Fragment(), OnMapReadyCallback,GoogleMap.OnMarkerClickListener, MyLocationCallback {

    private lateinit var binding: FragmentCheckInBinding
    private lateinit var viewModal: DashboardViewModal

    private var map: GoogleMap? = null
    private lateinit var placesClient: PlacesClient
    private var latitude = ""
    private var longitude = ""
    private var street = ""
    private var postalCode = ""
    private var city = ""
    private var radius = "500"
    private var type = "hospital"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_check_in, container, false)

        viewModal = ViewModelProvider(this, ViewModalFactory(activity?.application!!)).get(
            DashboardViewModal::class.java)

        setUI()
        setObserver()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        mapFragment?.getMapAsync {
            map = it
            map?.mapType = GoogleMap.MAP_TYPE_NORMAL
            map?.uiSettings?.isZoomControlsEnabled = true
            map?.uiSettings?.isZoomGesturesEnabled = true

            // map?.setPadding(0, 0, 0, 130) //change padding

            map?.setOnCameraIdleListener {
                val latLng: LatLng = map?.cameraPosition!!.target
                latitude = latLng.latitude.toString()
                longitude = latLng.longitude.toString()
                Log.e("TAG", "initializeMap: $latitude $longitude")
                val address = Utils.getAddressFromLatLngAsAddress(requireActivity(), latLng)
                street = address?.getAddressLine(0).toString()
                postalCode = address?.postalCode.toString()
                city = address?.locality.toString()

                //  map?.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            }
        }
     //   initializePlace()
    }

    override fun onResume() {
        super.onResume()
        (activity as DashboardActivity?)?.run {
            setTitle("Check In")
            hideNotificationIcon()
            showBackArrow()
            hideUnreadCount()
        }
    }

    private fun setUI() {
        binding.tvCheckIn.setOnClickListener {
            checkIn()
        }
        binding.tvByName.setOnClickListener {
//            if (TextUtils.isEmpty(binding.etSearch.text)) {
//                Toast.makeText(requireContext(), "Enter Search Type", Toast.LENGTH_SHORT).show()
//            } else {
            val AUTOCOMPLETE_REQUEST_CODE = 1

            // Set the fields to specify which types of place data to
            // return after the user has made a selection.

            // Set the fields to specify which types of place data to
            // return after the user has made a selection.
            val fields = listOf(Place.Field.ID, Place.Field.DISPLAY_NAME,Place.Field.LOCATION,Place.Field.ADDRESS_COMPONENTS)

            // Start the autocomplete intent.

            // Start the autocomplete intent.
//            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
//                .build(requireContext())
//            startActivityForResult(intent, 1)

//            initializePlace()
//            }
//            binding.cl1.visibility = View.GONE
//            binding.cl2.visibility = View.VISIBLE
        }
//        binding.tvCancel.setOnClickListener {
//            binding.cl2.visibility = View.GONE
//            binding.cl1.visibility = View.VISIBLE
//        }
        binding.tvByDistance.setOnClickListener {
          //  popupRadiusFilter(binding.root)
        }
        binding.tvSearch.setOnClickListener {
//            if (TextUtils.isEmpty(binding.etSearch.text)) {
//                Toast.makeText(requireContext(), "Enter Search Type", Toast.LENGTH_SHORT).show()
//            } else {
//                type = binding.etSearch.text.toString()
//                loadNearByPlaces()
//            }
        }
    }

//    private fun setUpMapIfNeeded() {
//        // Do a null check to confirm that we have not already instantiated the map.
//        val mMap
//        if (mMap == null) {
//            // Try to obtain the map from the SupportMapFragment.
//            mMap =
//                (getSupportFragmentManager().findFragmentById(R.id.map) as SupportMapFragment).getMap()
//            mMap.setMyLocationEnabled(true)
//            // Check if we were successful in obtaining the map.
//            if (mMap != null) {
//                mMap.setOnMyLocationChangeListener(OnMyLocationChangeListener { arg0 -> mMap.addMarker(
//                    MarkerOptions().position(LatLng(arg0.latitude, arg0.longitude))
//                        .title("It's Me!")
//                )
//                })
//            }
//        }
//    }

//    private fun initializePlace(){
//        if (!Places.isInitialized()) {
//            Places.initialize(requireContext(), getString(R.string.google_maps_key), Locale.getDefault());
//        }
//        // Initialize the AutocompleteSupportFragment.
//        val autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
//                as AutocompleteSupportFragment
//
//        // Specify the types of place data to return.
//        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG))
//
//        // Set up a PlaceSelectionListener to handle the response.
//        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
//            override fun onPlaceSelected(place: Place) {
//
//                val cameraPosition = CameraPosition.Builder()
//                    .target(place.latLng!!) // Sets the center of the map to location user
//                    .zoom(17f) // Sets the zoom
//                    .bearing(90f) // Sets the orientation of the camera to east
//                    .tilt(40f) // Sets the tilt of the camera to 30 degrees
//                    .build() // Creates a CameraPosition from the builder
//                map?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
//                //  Log.i("TAG", "Place: ${place.name}, ${place.id}")
//            }
//
//            override fun onError(status: Status) {
//              //  Toast.makeText(requireContext(),"Location could not be found",Toast.LENGTH_SHORT).show()
//                // Log.i("TAG", "An error occurred: $status")
//            }
//        })
//    }


    private fun initializeMap() { }

    private fun setUpMapIfNeeded(mapFragment: SupportMapFragment?) {
        // Do a null check to confirm that we have not already instantiated the map.

            // Check if we were successful in obtaining the map.
        map?.setOnMyLocationChangeListener(OnMyLocationChangeListener { arg0 -> map?.addMarker(
            MarkerOptions().position(LatLng(arg0.latitude, arg0.longitude))
                .title("It's Me!")
        )
        })
    }

    private fun initializePlaceSdk() {
        // Construct a PlacesClient
        Places.initialize(requireContext(), getString(R.string.google_maps_key))
        placesClient = Places.createClient(requireContext())

        // Use fields to define the data types to return.
        val placeFields: List<Place.Field> =
            listOf(Place.Field.DISPLAY_NAME, Place.Field.TYPES, Place.Field.LOCATION)

        // Use the builder to create a FindCurrentPlaceRequest.
        val request: FindCurrentPlaceRequest = FindCurrentPlaceRequest.newInstance(placeFields)

        // Call findCurrentPlace and handle the response (first check that the user has granted permission).
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {

            val placeResponse = placesClient.findCurrentPlace(request)
            placeResponse.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val response = task.result
                    for (placeLikelihood: PlaceLikelihood in response?.placeLikelihoods
                        ?: emptyList()) {
                        Log.i(
                            "CheckIn",
                            "Place '${placeLikelihood.place.displayName}' has type: ${placeLikelihood.place.placeTypes}"
                        )
                        if (placeLikelihood.place.placeTypes.toString().contains("FOOD")) {
                            val marker = map?.addMarker(
                                MarkerOptions()
                                    .position(placeLikelihood.place.location!!)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.food))
                                    .anchor(0.5f, 0.5f)
                                    .title(placeLikelihood.place.displayName)
                                //.snippet(snippet)
                            )
                            marker?.tag = placeLikelihood.place.displayName
                        }
                        if (placeLikelihood.place.placeTypes.toString().contains("RESTAURANT")) {
                            val marker = map?.addMarker(
                                MarkerOptions()
                                    .position(placeLikelihood.place.location!!)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.hotel))
                                    .anchor(0.5f, 0.5f)
                                    .title(placeLikelihood.place.displayName)
                                //.snippet(snippet)
                            )
                            marker?.tag = placeLikelihood.place.displayName
                        }
                        if (placeLikelihood.place.placeTypes.toString().contains("PETROL")) {
                            val marker = map?.addMarker(
                                MarkerOptions()
                                    .position(placeLikelihood.place.location!!)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.petrol))
                                    .anchor(0.5f, 0.5f)
                                    .title(placeLikelihood.place.displayName)
                                //.snippet(snippet)
                            )
                            marker?.tag = placeLikelihood.place.displayName
                        }
                    }
                } else {
                    val exception = task.exception
                    if (exception is ApiException) {
                        Log.e("CheckIn", "Place not found: ${exception.statusCode}")
                    }
                }
            }
        } else {
            // A local method to request required permissions;
            // See https://developer.android.com/training/permissions/requesting
            // getLocationPermission()
        }
    }


    override fun onMapReady(p0: GoogleMap) {
        map = p0
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        val success: Boolean = map?.setMapStyle(
            MapStyleOptions(
                resources
                    .getString(R.string.style_json)
            )
        )!!

//        if (!success) {
//            // Log.e(TAG, "Style parsing failed.")
//        }

        map?.isMyLocationEnabled = true

        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()

        val location: Location? =
            locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false)!!)
        if (location != null) {

            Log.e("TAG", "onMapReady: "+ location.latitude.toString() +" "+location.longitude.toString())
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        location.latitude,
                        location.longitude
                    ), 16f
                )
            )
            val cameraPosition = CameraPosition.Builder()
                .target(
                    LatLng(
                        location.latitude,
                        location.longitude
                    )
                ) // Sets the center of the map to location user
                .zoom(16f) // Sets the zoom
                //.bearing(90f) // Sets the orientation of the camera to east
                .tilt(40f) // Sets the tilt of the camera to 30 degrees
                .build() // Creates a CameraPosition from the builder
            map?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
//            val address = Utils.getAddressFromLatLngAsAddress(requireActivity(), latLng)
//            street = address?.getAddressLine(0).toString()
//            postalCode = address?.postalCode.toString()
//            city = address?.locality.toString()
            //loadNearByPlaces(location.latitude, location.longitude)
            initializePlaceSdk()
        }else {
            if (activity is DashboardActivity)
            {
                (activity as DashboardActivity).setLocationCallback(this)
            }
        }
    }

//    private fun loadNearByPlaces() {
//        //You Can change this type at your own will, e.g hospital, cafe, restaurant.... and see how it all works
//        val googlePlacesUrl = StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?")
//        googlePlacesUrl.append("location=").append(latitude).append(",").append(longitude)
//        googlePlacesUrl.append("&radius=").append(radius)
//        googlePlacesUrl.append("&types=").append(type)
//        googlePlacesUrl.append("&sensor=true")
//        googlePlacesUrl.append("&key=" + resources.getString(R.string.google_maps_key))
//
//        val jsonObjectRequest = JsonObjectRequest(
//            Request.Method.GET, googlePlacesUrl.toString(), null,
//            { response ->
//                Log.e("volley success", response.toString())
//                Utils.hideKeyboard(requireContext(), binding.root)
//                parseLocationResult(response)
//            },
//            { error ->
//                Utils.hideKeyboard(requireContext(), binding.root)
//             //   Toast.makeText(requireContext(), "Failed to fetch the records", Toast.LENGTH_SHORT)
//             //       .show()
//                Log.e("volley failed", error.toString())
//            }
//        )
//        // Instantiate the RequestQueue.
//        val queue = Volley.newRequestQueue(requireContext())
//        queue.add(jsonObjectRequest)
//    }

    private fun parseLocationResult(result: JSONObject) {
        map?.clear()
        var id: String
        var place_id: String
        val placeName: String? = null
        var reference: String
        var icon: String
        val vicinity: String? = null
        var latitude: Double
        var longitude: Double
        try {
            val jsonArray = result.getJSONArray("results")
            if (result.getString("status").equals("OK", true)) {
                map?.clear()
                for (i in 0 until jsonArray.length()) {
                    val place = jsonArray.getJSONObject(i)

                    latitude = place.getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lat")
                    longitude = place.getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lng")
                    val types = place.getJSONArray("types")
                    if (types.toString().contains(type)) {
                        val markerOptions = MarkerOptions()
                        val latLng = LatLng(latitude, longitude)
                        markerOptions.position(latLng)
                        markerOptions.title("$placeName : $vicinity")
                        map?.addMarker(markerOptions)
                    } else {
//                        Toast.makeText(
//                            requireContext(), "No Record Found!!!",
//                            Toast.LENGTH_LONG
//                        ).show()
                    }
                }
            } else if (result.getString("status").equals("ZERO_RESULTS", ignoreCase = true)) {
//                Toast.makeText(
//                    requireContext(), "No Record Found!!!",
//                    Toast.LENGTH_LONG
//                ).show()
            }

        } catch (e: JSONException) {
            e.printStackTrace()
            Log.e("parseLocationResult", "parseLocationResult: Error=" + e.message)
        }
    }

//    private fun popupRadiusFilter(view: View?) {
//        val inflater =
//            activity?.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//        val popupBinding: PopupDistanceBinding =
//            DataBindingUtil.inflate(inflater, R.layout.popup_distance, null, false)
//        val popupWindow = PopupWindow(
//            popupBinding.root,
//            ConstraintLayout.LayoutParams.WRAP_CONTENT,
//            ConstraintLayout.LayoutParams.WRAP_CONTENT
//        )
//        popupWindow.isFocusable = true
//        popupWindow.isOutsideTouchable = true
//        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        popupWindow.elevation = 5.0f
//        popupWindow.showAsDropDown(binding.tvByDistance, 0, 0)
//        val list: ArrayList<String> = ArrayList()
//        list.add("Upto 500 M")
//        list.add("Upto 1 KM")
////        list.add("Upto 5 Km")
////        list.add("Upto 10 Km")
////        list.add("Upto 20 Km")
//        val adapter = StringRecyclerAdapter(
//            context = requireContext(),
//            list = list
//        )
//        popupBinding.rvDistance.setHasFixedSize(true)
//        popupBinding.rvDistance.layoutManager = GridLayoutManager(
//            requireContext(),
//            1,
//            RecyclerView.VERTICAL,
//            false
//        )
//        popupBinding.rvDistance.adapter = adapter
//
//        adapter.onItemClick(object : ItemClickListener {
//            override fun onClick(position: Int, view: View?) {
//                //Toast.makeText(requireContext(), list[position], Toast.LENGTH_SHORT).show()
//                when (position) {
//                    0 -> radius = "500"
//                    1 -> radius = "1000"
////                    2 -> radius = "5000"
////                    3 -> radius = "10000"
////                    4 -> radius = "20000"
//                }
//                binding.tvByDistance.text = radius
//                popupWindow.dismiss()
//            }
//        })
//
//    } //eof popup


    private fun setObserver() {
        viewModal.logout.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
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
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(requireContext())
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
//                    Toast.makeText(
//                        requireContext(),
//                        response.message,
//                        Toast.LENGTH_SHORT
//                    ).show()
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
                else -> {
                    ProgressDialog.hideProgressBar()
                }
            }
        }

        viewModal.checkIn.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
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
                        viewModal.logout()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            response.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun checkIn() {
        if(city==""){city="null"}
        if(postalCode==""){postalCode="null"}
        if(street==""){street="null"}
        val request = CheckInRequest(
            city,
            latitude,
            longitude,
            postalCode,
            street
        )
        viewModal.checkIn(request)
    }

    override fun onLocationSuccess(latlng: LatLng)
    {
        map?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                latlng, 15f
            )
        )
        val cameraPosition = CameraPosition.Builder()
            .target(
                latlng
            ) // Sets the center of the map to location user
            .zoom(15f) // Sets the zoom
            .bearing(360f) // Sets the orientation of the camera to North
            .tilt(40f) // Sets the tilt of the camera to 30 degrees
            .build() // Creates a CameraPosition from the builder

        map?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        map?.setOnMarkerClickListener(this)
    }

    override fun onMarkerClick(p0: Marker): Boolean {
       // TODO("Not yet implemented")
        return false
    }

//     override fun onActivityResult(
//        requestCode: Int,
//        resultCode: Int,
//        @Nullable data: Intent?
//    ) {
//        if (requestCode == 1) {
//            when (resultCode) {
//                RESULT_OK -> {
//                    val place = Autocomplete.getPlaceFromIntent(data!!)
//                    Log.i("TAG", "Place: " + place.name + ", " + place.id+ ", " + place.latLng)
//                    val cameraPosition = CameraPosition.Builder()
//                        .target(place.latLng!!) // Sets the center of the map to location user
//                        .zoom(14f) // Sets the zoom
//                        .bearing(90f) // Sets the orientation of the camera to east
//                        .tilt(40f) // Sets the tilt of the camera to 30 degrees
//                        .build() // Creates a CameraPosition from the builder
//                    map?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
//                    loadNearByPlaces()
//                    binding.tv1.text = place.name
//                    //  Log.i("TAG", "Place: ${place.name}, ${place.id}")
//                }
//                AutocompleteActivity.RESULT_ERROR -> {
//                    // TODO: Handle the error.
//                    val status = Autocomplete.getStatusFromIntent(data!!)
//                    Log.i("TAG", status.statusMessage!!)
//                }
//                RESULT_CANCELED -> {
//                    // The user canceled the operation.
//                }
//            }
//            return
//        }
//        super.onActivityResult(requestCode, resultCode, data)
//    }
}
