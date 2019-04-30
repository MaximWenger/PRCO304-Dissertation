package com.example.planty.Classes

import com.example.planty.Objects.Identified
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class IdentSaveToDatabase {

    /**Saves given Object to identifiedPlants in Firebase
     * @param correctIdent populated Identified object
     * @return string: SavedIdent UUID
     */
     fun saveIdentToDatabase(correctIdent: Identified): String {//Save correct identification to database
        val uuid = UUID.randomUUID().toString() //Produce unique ID for ident file name
         val userUUID = FirebaseAuth.getInstance().uid.toString()
        val ref = FirebaseDatabase.getInstance().getReference("/identifiedPlants/${userUUID}/${uuid}")
        ref.setValue(correctIdent)
         return uuid
    }

    /**Returns an Identified Object, populated with given parameters
     * @param plantName PlantName
     * @param identImageName to associate the identification to the image
     * @param defaultDesc to give a description of the plant
     * @param baseIdentity used to give an base identity of the type of plant
     * @return Identified object
     */
     fun getIdentObject(plantName: String, identImageName: String, defaultDesc: String, baseIdentity: String): Identified {//returns identified object, populated with details of identifed plant
        val dateTime = DateTime().getDateTime()
        val correctIdent = Identified(
            dateTime,
            plantName,
            baseIdentity,
            identImageName,
            defaultDesc
        ) //populate Identified object
        return correctIdent //return identified object
    }
}