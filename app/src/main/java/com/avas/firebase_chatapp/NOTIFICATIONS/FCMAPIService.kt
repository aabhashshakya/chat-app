package com.avas.firebase_chatapp.NOTIFICATIONS

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

private const val FCM_SERVER_KEY: String = "YOUR API_KEY HERE"

interface FCMAPIService {


    //You get the authorization key from Firebase Console -> Settings -> FCM -> Server key
    @Headers(
        "Content-Type:application/json",
        "Authorization:key= $FCM_SERVER_KEY"
    )


    @POST("fcm/send")
    fun sendNotifications(@Body body: SendNotification): Call<MyResponse>


}