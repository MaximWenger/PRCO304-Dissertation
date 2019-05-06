package com.example.planty.Classes

import java.text.SimpleDateFormat
import java.util.*

class DateTime {


    companion object {//self contained object, does not need an instance of the class to run
        /**Returns DateTime
         * @return String
         */
        fun getDateTime(): String { //Returns DateTime
            val dateFormat = SimpleDateFormat("dd/M/yyy hh:mm:ss")
            val dateTime = dateFormat.format(Date())
            return dateTime
        }
    }
}