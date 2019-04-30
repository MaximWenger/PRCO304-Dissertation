package com.example.planty.Classes

import android.util.Log
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import java.lang.Exception

 class CloudVisionData {//Used to confirm and sort the data, returned by Google Cloud Vision
     private val toRemoveLibrary = arrayOf("Petal","Plant", "Yellow", "Flower", "Flowering Plant", "Spring", "Wildflower", "Blue", "Botany", "Spring", "Perennial Plant", "Annual Plant") //Used to hold strings of identification labels to be removed
     private val toConfirmPlantLibrary = arrayOf("Petal","Plant", "Flower", "Flowering Plant", "Wildflower") //Used to confirm the identification is a plant
     private val baseIdentLibrary = arrayOf("Tree", "Flower", "Shrub", "Herb")


     /**Confirms if the photo is a plant, comparing the labels against the library
      * @param list List of CloudVision labels
      * @return Boolean
      */
     fun confirmPlant(list: MutableList<FirebaseVisionImageLabel>):Boolean {//Check that atleast one of the unsorted labels matches toConfirmPlantLibrary (Confirming the picture is a plant)
         val plantLibrary = 1
         for (label in list) {//For each item in list
             val text = label.text //Get the label
             if (compareLabel(text, plantLibrary)){
                 return true //return true, the image DOES look to be a plant
             }
         }
         return false //Return false, the image does NOT look to be a plant
     }

     /**Returns the baseIdentLibrary
      * @return String: BaseIdentLibrary
      */
     fun getBaseIdentLibrary(): Array<String> {
         return baseIdentLibrary
     }

     /** Returns a list of filtered lables (Removing labels, found in the removeLibrary)
      * @param list list of unfiltered labels
      * @return List of filtered lables
      */
     fun imageDataFilter(list: MutableList<FirebaseVisionImageLabel>): MutableList<FirebaseVisionImageLabel> { //Returns sorted list, removing specific words
         val removeLibrary = 0
         val sortedList = mutableListOf<FirebaseVisionImageLabel>() //Used to store all sorted identifications
             for (label in list) {
                 val text = label.text
                  if (!compareLabel(text, removeLibrary)) { //If the label has been found within the reserved words. remove it
                     try {
                         sortedList.add(label) //Add the identification to the sortedList
                         Log.d("IdentifyActivity", "Test SIZE = ${sortedList.size}")
                     } catch (e: Exception) {
                         Log.d("IdentifyActivity", "ERROR = ${e.message}")
                     }
                 }
             }
             return sortedList.asReversed()//Must reverse the sortedList, to maintain highest confidence towards the start of the new list
     }

     /**Returns a baseIdentification
      * @param list List of labels from CloudVision
      *@return base Identification
      */
     fun baseImageIdentFilter(list: MutableList<FirebaseVisionImageLabel>): String { //Return base image identification
         val identLibrary = 2
         var baseIdent = ""
         for (label in list){
             val text = label.text
             if (compareLabel(text, identLibrary)){
                 baseIdent = text
             }
         }
         return baseIdent
     }

     /** Confirms if the Text Param is contained within a library (Library denoted by type)
      * @param text string to compare against the library
      * @param type Int: userd to denote which library to compare against
      * @return Boolean
      */
     private fun compareLabel(text: String, type: Int): Boolean { //Returns Boolean if the given string is within the existing blacklist (library)
         var lowerCaseLibary = toRemoveLibrary.map { it.toLowerCase() }//Convert entire array to lowercase
         if (type == 1){ //If to confirmPlant else continue with library
             lowerCaseLibary  = toConfirmPlantLibrary.map { it.toLowerCase() }//Convert entire array to lowercase
         }
         else if (type == 2){
             lowerCaseLibary  = baseIdentLibrary.map { it.toLowerCase() }//Convert entire array to lowercase
         }
        var lowerCaseText = text.decapitalize() //Need to use decapitalise as .toLowerCase uses an array
        if (lowerCaseLibary.contains(lowerCaseText)){ //If the library contains a key word
            return true
        }
        return false
    }
}
