package com.avas.firebase_chatapp.VIEWMODEL

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.avas.firebase_chatapp.MODEL.User
import com.avas.firebase_chatapp.REPOSITORY.BaseRepository
import com.avas.firebase_chatapp.REPOSITORY.FirebaseQueryLiveData
import com.avas.firebase_chatapp.VIEW.USERS
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

private const val TAG = "ProfileViewModel"

class BaseViewModel @ViewModelInject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseDatabase,
    private val repository: BaseRepository
) : ViewModel() {

    fun loadUserProfile(): LiveData<User> {
        val userLiveDataSnapshot =
            FirebaseQueryLiveData(db.getReference(USERS).child(auth.currentUser!!.uid))
        val userProfileLiveData = Transformations.switchMap(userLiveDataSnapshot) {


            var user = User()
            if (it.exists()) {
                Log.d(TAG, "loadUserProfile: Getting the user profile")
                user = it.getValue(User::class.java)!!
            }
            MutableLiveData(user)


        }
        return userProfileLiveData


    }

    fun makeUserOnline() {
        repository.makeUserOnline()

    }

    fun logOutUser() {
        repository.logOutUser()

    }

    fun addUserToDatabase() {
        repository.addUserToDatabase()
    }

    fun saveFirebaseToken() {
        repository.saveFirebaseToken()
    }

}