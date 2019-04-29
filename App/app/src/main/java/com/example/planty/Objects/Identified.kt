package com.example.planty.Objects

class Identified (val dateTime: String, val plantName: String, val baseID: String, val identifiedImage:String, val userDesc: String) {
    constructor(): this("", "", "", "", "")
}