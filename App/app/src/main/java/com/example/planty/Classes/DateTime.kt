package com.example.planty.Classes

import java.text.SimpleDateFormat
import java.util.*

class DateTime {

         fun getDateTime(): String{ //Returns DateTime
        val dateFormat = SimpleDateFormat("dd/M/yyy hh:mm:ss")
        val dateTime = dateFormat.format(Date())
        return dateTime
    }
}