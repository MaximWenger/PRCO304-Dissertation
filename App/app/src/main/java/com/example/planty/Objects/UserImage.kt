package com.example.planty.Objects

class UserImage (val userID: String, val imageLoc: String?, val dateTime: String) {
    constructor(): this ("", "", "")
}