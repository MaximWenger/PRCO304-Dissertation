package com.example.planty.Activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import com.example.planty.Classes.CloudVisionData
import com.example.planty.Classes.IdentSaveToDatabase
import com.example.planty.R
import com.google.firebase.auth.FirebaseAuth

import kotlinx.android.synthetic.main.activity_self_identify.*
import java.lang.Exception

class SelfIdentifyActivity : AppCompatActivity() {
    private val cloudVision = CloudVisionData()
    private val identSave =  IdentSaveToDatabase()

    private var plantType = cloudVision.getBaseIdentLibrary().first() //Used to keep chosen plant type (from spinner)



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_self_identify)
       // setSupportActionBar(toolbar)

            verifyLoggedIn()
           populateSpinner()

        SelfIdentify_Save_Button.setOnClickListener{
            if (checkPopulatedFields()){
                saveIdentification()

            }

        }
    }

    private fun saveIdentification(){//Produces a Identified object and saves to the database
        var plantName = getPlantName()
        var plantDesc = getPlantDesc()
        var imageName = getImageFileName()
        if (imageName.isNotEmpty()) {

            var identifiedPlant = identSave.getIdentObject(plantName, imageName, plantType, plantDesc)
            identSave.saveIdentToDatabase(identifiedPlant)
            Log.d("SelfIdentifyActivity", "Identity correctly saved")
        }
    }

    private fun getImageFileName(): String {
            var filename = ""
            try {
                filename = intent.getStringExtra("fileName")

            }catch (e: Exception){
                Log.d("SelfIdentifyActivity", "getImageFileName Error = ${e.message}")
            }
            return filename
        }




    private fun getPlantName(): String {
        val plantName = SelfIdentify_PlantName_EditText.text
        return plantName.toString()
    }

    private fun getPlantDesc(): String {
        val plantDesc = SelfIdentified_PlantDesc_EditText.text
        return plantDesc.toString()
    }

    private fun  checkPopulatedFields(): Boolean {
        var correctlyPopulated = false
        val plantName = getPlantName()
        val plantDesc = getPlantDesc()
        if (plantName.isEmpty() && plantDesc.isEmpty()){
            Toast.makeText(this, "Please populate Plant Name & Plant Description", Toast.LENGTH_SHORT).show()
        }
        else if (plantName.isEmpty()){
            Toast.makeText(this, "Please populate Plant Name", Toast.LENGTH_SHORT).show()
        }
        else if (plantDesc.isEmpty()){
            Toast.makeText(this, "Please populate Plant Description", Toast.LENGTH_SHORT).show()
        }
        else {
            correctlyPopulated = true
        }
        return correctlyPopulated
    }

    private fun populateSpinner(){//Populates the spinner (Mutli-choice options)

        val baseIdentLibrary = cloudVision.getBaseIdentLibrary()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, baseIdentLibrary)

        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        SelfIdentify_PlantTypeSpinner.adapter = adapter

        SelfIdentify_PlantTypeSpinner.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                plantType = parent?.getItemAtPosition(position).toString() //Used to update chosen planttype (From spinner)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }


    }

    private fun verifyLoggedIn(){ //Check if the User is already logged in, if not, return User to registerActivity
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null){//If no user ID, user is not logged in
            val intent =  Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean { //Create the menu
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean { //When an option from the menu is clicked
        when (item?.itemId){ //Switch statement
            R.id.nav_Profile -> { //DOES NOTHING RIGHT NOW
                navToProfileActivity() //Go to ProfileActivity
            }
            R.id.nav_Identify -> {
                navToIdentifyActivity()
            }
            R.id.nav_Find -> {
                navToMapsActivity() //Go to MapsActivity
            }
            R.id.nav_Sign_Out -> {
                signOut() //Signs the User out and returns to RegisterActivity
            }
            R.id.nav_Contact -> { //DOES NOTHING RIGHT NOW
                return super.onOptionsItemSelected(item)  //
            }
            else ->  return super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun signOut(){
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, RegisterActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun navToMapsActivity(){
        val intent = Intent(this, MapsActivity::class.java) //Populate intent with new activity class
        //  intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
        startActivity(intent) //Change to new class
    }

    private fun navToProfileActivity(){
        val intent = Intent(this, ProfileActivity::class.java) //Populate intent with new activity class
        //  intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
        startActivity(intent) //Change to new class
    }


    private fun navToIdentifyActivity(){
        val intent = Intent(this, IdentifyActivity::class.java) //Populate intent with new activity class
        //  intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
        startActivity(intent) //Change to new class
    }

}
