package com.avas.firebase_chatapp.MODEL


data class User(
    val userID: String,
    val username: String,
    val email: String,
    val profilePic: String, //profile pic and cover pic are https address stored in Firebase Storage
    val coverPic: String,
    val online: Boolean = true,
    val bio: String = "Hello it me.",
    val location: String = "No address specified",
    val phone: String = "No phone number specified",


    ) {
    //this class should have a empty constructor for this to be insertable in Firebase Database
    //In kotlin, a secondary constructor must always call the primary constructor
    //so below, we have created a parameter-less(empty) constructor, that calls the primary constructor that sets every value to null

    constructor() : this("", "", "", "", "", false, "", "", "")

    //an alternative to creating this empty constructor would be to set the default values to ALL the variables in the primary
    //constructor to ""


}