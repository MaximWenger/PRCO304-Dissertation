package com.example.planty.classes

class Branch (val address: String, val latitude: Double, val longitude: Double, val branchName: String) {
    constructor(): this("", 0.0, 0.0, "")
}