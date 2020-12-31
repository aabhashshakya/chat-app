package com.avas.firebase_chatapp.NOTIFICATIONS


//this is the response that we receive from the FCM api
//if everthing went well, the success = 1
data class MyResponse(val success: Int = 0) {
}