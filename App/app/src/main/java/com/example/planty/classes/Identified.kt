package com.example.planty.classes

class Identified (val userID: String, val dateTime: String, val plantName: String, val baseID: String, val identifiedImage:String) {
    constructor(): this("", "", "", "", "")
}