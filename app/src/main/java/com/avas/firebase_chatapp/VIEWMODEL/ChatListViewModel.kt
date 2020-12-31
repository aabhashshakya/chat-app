package com.avas.firebase_chatapp.VIEWMODEL

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.avas.firebase_chatapp.MODEL.ChatList
import com.avas.firebase_chatapp.MODEL.User
import com.avas.firebase_chatapp.REPOSITORY.FirebaseQueryLiveData
import com.avas.firebase_chatapp.VIEW.CHAT_LIST
import com.avas.firebase_chatapp.VIEW.USERS
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


private const val TAG = "ChatListViewModel"

class ChatListViewModel @ViewModelInject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseDatabase
) : ViewModel() {


    private fun getChatUID(receiverID: String): String {
        return if (auth.currentUser!!.uid < receiverID) {
            auth.currentUser!!.uid + " + " + receiverID
        } else
            receiverID + " + " + auth.currentUser!!.uid

    }

    fun retrieveChatListOfUsers(): LiveData<ArrayList<User>> {
        val chatListOfPeopleID = ArrayList<ChatList>()
        //first retrieving the ids of the people that the user has messaged
        val queryLiveData =
            FirebaseQueryLiveData(db.getReference(CHAT_LIST).child(auth.currentUser!!.uid))
        val liveChatListOfPeopleFullProfile = Transformations.switchMap(queryLiveData) {
            if (it.exists()) {
                chatListOfPeopleID.clear()
                for (personWeHaveMessaged in it.children) {
                    val person = personWeHaveMessaged.getValue(ChatList::class.java)
                    chatListOfPeopleID.add(person!!)
                }
            } else {
                chatListOfPeopleID.clear()
            }
            Log.d(
                TAG,
                "retrieveChatListOfUsers: People we have messaged = ${chatListOfPeopleID.size}"
            )
            //retrieving the full profile of the users based of the above list of people the user has messaged
            val queryAllUsers = FirebaseQueryLiveData(db.getReference(USERS))
            val chatListOfPeopleFullProfile = ArrayList<User>()
            val liveChatListOfPeopleFullProfile =
                Transformations.switchMap(queryAllUsers) { snapshot ->
                    if (snapshot?.exists() == true) {
                        chatListOfPeopleFullProfile.clear()
                        for (person in snapshot.children) {
                            val user = person.getValue(User::class.java)
                            for (personWeHaveMessaged in chatListOfPeopleID) {
                                //only the profile of the users that we have messaged will be fetched
                                if (user!!.userID == personWeHaveMessaged.id) {
                                    chatListOfPeopleFullProfile.add(user)
                                }
                            }

                        }

                    }

                    Log.d(
                        TAG,
                        "retrieveChatListOfUsers: People we have messaged : Full profile = ${chatListOfPeopleFullProfile.size}"
                    )
                    MutableLiveData(chatListOfPeopleFullProfile)

                }


            liveChatListOfPeopleFullProfile


        }
        return liveChatListOfPeopleFullProfile
    }


}