package com.avas.firebase_chatapp.VIEW.FRAGMENTS.MAIN.BASE.FRAGMENT_IN_VIEWPAGER


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.avas.firebase_chatapp.R
import com.avas.firebase_chatapp.VIEWMODEL.ProfileViewModel
import com.avas.firebase_chatapp.databinding.FragmentProfileBinding
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

private const val TAG = "Profile Fragment"

@AndroidEntryPoint
class ProfileFragment @Inject constructor() : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!


    var googleMap: GoogleMap? = null

    val profileViewModel: ProfileViewModel by viewModels()


    val navArgs: ProfileFragmentArgs by navArgs()

    companion object {
        private const val LOCATION_PERMISSION = 89
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        } //for showing the back button


        //if we are visiting our own profile, the send message button is gone
        binding.settingsSendMessage.isVisible =
            navArgs.userID != profileViewModel.getCurrentUserID()


        //for updating the ui with appropriate information
        profileViewModel.loadUserProfile(navArgs.userID).observe(viewLifecycleOwner) {
            binding.profileUsername.text = it?.username
            binding.profileBio.text =
                it?.bio ?: ""//means if bio is null, put nothing there
            binding.profilePhone.text = it?.phone ?: ""
            binding.profileAddress.text = it?.location?.substringBefore("latlng")
            Glide.with(binding.profileCardView).load(it?.profilePic)
                .error(R.drawable.ic_baseline_error_24)
                .placeholder(R.drawable.ic_profile)
                .into(binding.profileProfilePic)
            Glide.with(binding.profileCardView).load(it?.coverPic)
                .error(R.drawable.ic_baseline_error_24)
                .placeholder(R.drawable.coverimage)
                .into(binding.profileCoverPic)

            //to message that user
            binding.settingsSendMessage.setOnClickListener { _ ->
                val action =
                    ProfileFragmentDirections.actionProfileFragmentToMessageFragment(
                        navArgs.userID,
                        it.profilePic,
                        it.username
                    )
                findNavController().navigate(action)
            }

            //on click listener for profile pic
            binding.profileProfilePic.setOnClickListener { view ->

                //navigating to full image view fragment
                val action = ProfileFragmentDirections
                    .actionProfileFragmentToFullImageViewFragment(it?.profilePic)
                view.findNavController().navigate(action)

            }

            //on click listener for cover pic
            binding.profileCoverPic.setOnClickListener { view ->
                //navigating to full image view fragment
                val action = ProfileFragmentDirections
                    .actionProfileFragmentToFullImageViewFragment(it?.coverPic)
                view.findNavController().navigate(action)

            }

            //when user click the phone, it should open the dialog with the given number
            binding.profilePhone.setOnClickListener {
                //checking if the phone number contains only digits, if so open dialer so we can call it
                if (binding.profilePhone.text.matches("[0-9]+".toRegex())) {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:${binding.profilePhone.text}")
                    requireContext().startActivity(intent)
                } else {
                    Toast.makeText(context, "Phone number cannot be dialed", Toast.LENGTH_SHORT)
                        .show()
                }

            }

            //when user clicks the address, the REAL Google Map opens
            binding.profileAddress.setOnClickListener { _ ->
                try {
                    val addressOfUser: String = it?.location.toString()
                    val latitude = addressOfUser.substringAfter("latlng ").run {
                        this.substringBefore(":").toDouble()
                    }
                    val longitude = addressOfUser.substringAfter(":").toDouble()
                    //opening google maps
                    //geo: means google map's camera will be fixed to that position,
                    //?q (query)means a marker will be placed at that postiion and google maps display info about that
                    //we put the query as the address name rather than coordinates, as what is query will be displayed by the map
                    //and we want to display name of the place, not the coordinates
                    val uri = String.format(
                        Locale.ENGLISH,
                        "geo:$latitude,$longitude?q=$latitude,$longitude"
                    )
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                    requireContext().startActivity(intent)
                } catch (e: Exception) {
                    Log.d(TAG, "onViewCreated: Address to map: ${e.message} ${e.printStackTrace()}")
                    Toast.makeText(
                        context,
                        "This location cannot be viewed in Maps",
                        Toast.LENGTH_SHORT
                    ).show()
                }


            }

            //for google maps
            try {
                val addressOfUser: String = it?.location.toString()
                val latitude = addressOfUser.substringAfter("latlng ").run {
                    this.substringBefore(":").toDouble()
                }
                val longitude = addressOfUser.substringAfter(":").toDouble()

                googleMap?.clear()
                googleMap?.addMarker(
                    MarkerOptions().position(
                        LatLng(
                            latitude,
                            longitude
                        )
                    )
                )
                googleMap?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            latitude,
                            longitude
                        ), 15.3F
                    )
                )
            } catch (e: Exception) {
                Log.d(TAG, "onViewCreated: Address to map: ${e.message} ${e.printStackTrace()}")
            }

        }

        binding.profileMapView.onCreate(savedInstanceState)
        binding.profileMapView.getMapAsync {

            Log.d(TAG, "onMapReady: Map is not ready")
            if (it != null) {
                googleMap = it

                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    askForLocationPermission()
                } else {
                    it.isMyLocationEnabled = true
                    it.uiSettings.isMyLocationButtonEnabled = true
                }

                it.setOnMyLocationButtonClickListener {
                    if (!isLocationEnabled()) {
                        AlertDialog.Builder(
                            ContextThemeWrapper(
                                context,
                                R.style.MyAlertDialogStyle
                            )
                        )
                            .setMessage("Please make sure your Location Services are enabled")
                            .setPositiveButton(
                                "OK"
                            )
                            { dialog, _ ->
                                dialog.cancel()

                            }.show()
                    }
                    //returning false means the default behavior occurs, the camera moves to the user's current position
                    false

                }


            }


        }


    }

    //if the devices location services are enabled nibbers. VERY IMPORTANT
    private fun isLocationEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is new method provided in API 28
            val locationManager: LocationManager =
                requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.isLocationEnabled
        } else {
            //for older devices
            val mode = Settings.Secure.getInt(
                requireContext().contentResolver, Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF
            )
            (mode != Settings.Secure.LOCATION_MODE_OFF)
        }
    }


    //managing lifecycle of google mapview
    override fun onStart() {
        super.onStart()
        binding.profileMapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.profileMapView.onResume()
    }

    override fun onStop() {
        super.onStop()
        binding.profileMapView.onStop()
    }


    override fun onLowMemory() {
        super.onLowMemory()
        binding.profileMapView.onLowMemory()
    }

    override fun onPause() {
        super.onPause()
        binding.profileMapView.onPause()

    }


    //permission stuffs
    private fun askForLocationPermission() {
        if (shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            AlertDialog.Builder(requireContext())
                .setTitle("Permission needed")
                .setMessage("Location permission is needed to access your current location")
                .setPositiveButton("GRANT PERMISSION") { _, _ ->
                    //if clicked ok, it requests the permission
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION
                    )
                } //if clicked no, it dismisses the dialog
                .setNegativeButton(
                    "DECLINE"
                ) { dialog, _ ->
                    dialog.dismiss()
                    Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
                }.create().show()
        } else
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION
            )
    }


    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when (requestCode) {

            LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "Location permissions granted.", Toast.LENGTH_SHORT)
                        .show()

                    //enabling the my location button
                    googleMap?.isMyLocationEnabled = true
                    googleMap?.uiSettings?.isMyLocationButtonEnabled = true

                } else
                    Toast.makeText(context, "Locattion permission not granted.", Toast.LENGTH_SHORT)
                        .show()

            }


        }


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null


    }


}