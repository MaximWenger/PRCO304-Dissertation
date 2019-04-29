package com.example.planty.Classes

import com.example.planty.Objects.Identified
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class IdentSaveToDatabase {

     fun saveIdentToDatabase(correctIdent: Identified): String {//Save correct identification to database
        val uuid = UUID.randomUUID().toString() //Produce unique ID for ident file name
         val userUUID = FirebaseAuth.getInstance().uid.toString()
        val ref = FirebaseDatabase.getInstance().getReference("/identifiedPlants/${userUUID}/${uuid}")
        ref.setValue(correctIdent)
         return uuid
    }

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