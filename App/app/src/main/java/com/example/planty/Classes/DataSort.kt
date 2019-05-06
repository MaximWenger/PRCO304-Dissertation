package com.example.planty.Classes

import android.util.Log
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.ArrayList

class DataSort {

    /**Converts given string to 2dp
     * @param string String to convert to 2dp
     * @return String: String now 2dp
     */
     fun convertTo2dp(string: String): String{ //Converts a decimal to 2dp, returning a string
        var origString = string.toDouble()
        val newString = BigDecimal(origString).setScale(2, RoundingMode.HALF_EVEN)//Round the confidence to 2dp
        return newString.toString()
    }


    /**Returns a list of three identified labels
     * Used to give the top three (Highest confidence) identifications
     * @param identifiedString
     * @param indexStart
     * @return  ArrayList<String> ,3 long of identified labels
     */
     fun getSingleIdent(identifiedString: ArrayList<String>, indexStart: Int) : ArrayList<String> { //Returns an ArrayList, 3 long from the provided ArrayList
        var orignalString = identifiedString
        var newString = ArrayList<String>()
        var counter = 0
        var index = indexStart
        for (item in orignalString){
            if (counter <= 2) {
                newString.add(counter, orignalString.get(index))
                index++
                counter++
            }
        }
        return newString
    }

    /**Compares the parameters to see if they contain one or another
     * @param string1 String 1, to find inside string 2
     * @param string2 string 2, to find inside string 1
     * @return Boolean
     */
     fun findIfDataContains(string1: String, string2: String): Boolean {
        if (string1.contains(string2) || string2.contains(string1)) {
            return true
        }
        return false
    }


}