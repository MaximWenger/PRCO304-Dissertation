package com.example.planty.classes

class User (val username: String, val role: String, val dateTime: String) {
    constructor(): this("", "", "")
}