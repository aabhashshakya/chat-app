package com.avas.firebase_chatapp.VIEW.FRAGMENTS.MAIN.BASE


import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.avas.firebase_chatapp.R
import com.avas.firebase_chatapp.VIEWMODEL.BaseViewModel
import com.avas.firebase_chatapp.databinding.FragmentBaseBinding
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint


const val TAG = "Base Fragment"

@AndroidEntryPoint
class BaseFragment : Fragment(R.layout.fragment_base) {

    private var _binding: FragmentBaseBinding? = null
    private val binding get() = _binding!!

    val baseViewModel: BaseViewModel by viewModels()

    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBaseBinding.bind(view)

        //hiding the action bar
        (activity as AppCompatActivity).supportActionBar?.hide()

        //setting up menu in toolbar
        binding.toolbarMain.inflateMenu(R.menu.menu_logout)
        binding.toolbarMain.setOnMenuItemClickListener {
            //log out the user
            if (it.itemId == R.id.logout_button_menu) {
                baseViewModel.logOutUser()
                Log.d(TAG, "onViewCreated: User has been logged out")
                findNavController().navigate(
                    R.id.welcomeFragment, null,
                    NavOptions.Builder().setPopUpTo(R.id.base_fragment, true).build()
                )

            }
            true
        }


        //configuring the ViewPager
        viewPagerAdapter =
            ViewPagerAdapter(childFragmentManager, lifecycle)//instantiating our view pager adapter
        //always use childFragmentManager here

        binding.viewPager.adapter = viewPagerAdapter //setting up the adapter for our ViewPager2

        //setting up out Tab Layout with ViewPager
        //We use TabLayoutMediator to set up our tabs title, icon,etc here
        //We passed our tabLayout, viewPager2 and a TabConfigurationStrategy here
        TabLayoutMediator(
            binding.tabLayout, binding.viewPager
        ) { tab, position ->
            when (position) {
                0 -> tab.text = "CHAT"
                1 -> tab.setText("SEARCH")
                2 -> tab.setText("SETTINGS")

            }
        }.attach()


        //ADD USER TO DATABASE IF NOT ALREADY//IF ALREADY EXISTS, IT WONT ADD
        baseViewModel.addUserToDatabase()


        //LOADING USER INFO IN THE TOOLBAR
        baseViewModel.loadUserProfile().observe(viewLifecycleOwner) {
            if (it != null) {
                binding.userName.text = it.username
                Glide.with(binding.root).load(it.profilePic).error(R.drawable.ic_baseline_error_24)
                    .placeholder(R.drawable.ic_profile).into(binding.profileImage)

                //on click listener for profile pic
                binding.profileImage.setOnClickListener { view ->
                    //navigating to full image view fragment
                    val action = BaseFragmentDirections
                        .actionBaseFragmentToProfileFragment(it.userID)
                    view.findNavController().navigate(action)

                }
            }
        }

        //every device has a unique token sent by the firebase. we need this token to send cloud messages(notification)
        //from firebase
        baseViewModel.saveFirebaseToken()


        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                //FAB button is only visible on the first page of the view pager
                binding.fab.isVisible = position == 0
            }

        })


        //on click listener on FAB button//go to next fragment in the viewpager
        binding.fab.setOnClickListener {
            binding.viewPager.setCurrentItem(binding.viewPager.currentItem + 1, true)
        }


    }


    override fun onResume() {
        super.onResume()
        //make user online if logged in
        baseViewModel.makeUserOnline()
    }

    //THE STATUS OFFLINE IS IN MAINACTIVITY's onPause() method


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }

}


