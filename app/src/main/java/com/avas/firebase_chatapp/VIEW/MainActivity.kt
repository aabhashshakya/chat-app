package com.avas.firebase_chatapp.VIEW

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.avas.firebase_chatapp.R
import com.avas.firebase_chatapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "Main Activity"

const val USERS = "Users"
const val CHATS = "Chats"
const val CHAT_LIST = "ChatList"
const val IMAGE_MESSAGE = "Sent a photo"
const val CHAT_IMAGES_STORAGE = "Chat Images"
const val TOKEN = "Token"

@AndroidEntryPoint

class MainActivity : AppCompatActivity() {


    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var db: FirebaseDatabase
    private lateinit var binding: ActivityMainBinding

    private var user: FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)



        user = auth.currentUser
        if (user == null) {
            //if user not logged in goto welcome fragment

            navGraph.startDestination = R.id.welcomeFragment
            Log.d(TAG, "User: User is not logged in. Navigation to welcome fragment")

        } else {
            navGraph.startDestination = R.id.base_fragment
        }
        navController.graph = navGraph

        //just deleting the cache
        externalCacheDir?.deleteRecursively()


    }

    //THE STATUS ONLINE IS IN BASE FRAGMENTS's onResume() method
    override fun onPause() {
        super.onPause()
        //updating our status to online
        if (auth.currentUser != null) {
            db.getReference(USERS).child(auth.currentUser!!.uid).child("online").setValue(false)

        }
    }

}









