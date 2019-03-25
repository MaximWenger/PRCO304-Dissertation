package com.example.planty.classes

import android.util.Log
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import java.lang.Exception

 class cloudVisionData {//Used to confirm and sort the data, returned by Google Cloud Vision
     val toRemoveLibrary = arrayOf("Petal","Plant", "Yellow", "Flower", "Flowering Plant", "Spring", "Wildflower") //Used to hold strings of identification labels to be removed
     val toConfirmPlantLibrary = arrayOf("Petal","Plant", "Flower", "Flowering Plant", "Wildflower") //Used to confirm the identification is a plant


     fun confirmPlant(list: MutableList<FirebaseVisionImageLabel>):Boolean {//Check that atleast one of the unsorted labels matches toConfirmPlantLibrary (Confirming the picture is a plant)
         for (label in list) {
             val text = label.text //Get the label
             if (compareLabel(text, 1)){
                 return true //return true, the image DOES look to be a plant
             }
         }
         return false //Return false, the image does NOT look to be a plant
     }

     fun imageDataFilter(list: MutableList<FirebaseVisionImageLabel>): MutableList<FirebaseVisionImageLabel> { //Returns sorted list, removing specific words
                      val sortedList = mutableListOf<FirebaseVisionImageLabel>() //Used to store all sorted identifications
             for (label in list) {
                 val text = label.text
                 val entityId = label.entityId
                 val confidence = label.confidence
                 Log.d("IdentifyActivity", "Text = ${text}")
                 Log.d("IdentifyActivity", "entityID = ${entityId}")
                 Log.d("IdentifyActivity", "confidence = ${confidence}")
                 Log.d("IdentifyActivity", "Total amount = ${list.size}")

                 if (!compareLabel(text, 0)) { //If the label has been found within the reserved words. remove it
                     try {
                         sortedList.add(label) //Add the identification to the sortedList
                         Log.d("IdentifyActivity", "Test SIZE = ${sortedList.size}")
                     } catch (e: Exception) {
                         Log.d("IdentifyActivity", "ERROR = ${e.message}")
                     }
                 }
             }
             return sortedList
              }

     private fun compareLabel(text: String, type: Int): Boolean { //Returns Boolean if the given string is within the existing blacklist (library)
         var lowerCaseLibary = toRemoveLibrary.map { it.toLowerCase() }//Convert entire array to lowercase
         if (type == 1){ //If to confirmPlant else continue with library
             lowerCaseLibary  = toConfirmPlantLibrary.map { it.toLowerCase() }//Convert entire array to lowercase
         }
        var lowerCaseText = text.decapitalize() //Need to use decapitalise as .toLowerCase uses an array
        if (lowerCaseLibary.contains(lowerCaseText)){ //If the library contains a key word
            return true
        }
        return false
    }
}
