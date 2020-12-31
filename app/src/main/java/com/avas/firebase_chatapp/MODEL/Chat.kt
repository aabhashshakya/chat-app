package com.avas.firebase_chatapp.MODEL

data class Chat(
    val senderID: String = "",
    val receiverID: String = "",
    val textMessage: String = "",
    val imageUrl: String = "",
    val seen: Boolean = false,
    val messageID: String = "",

    ) {


}