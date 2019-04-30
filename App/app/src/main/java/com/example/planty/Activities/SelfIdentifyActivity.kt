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
import com.example.planty.Objects.UserImage
import com.example.planty.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_identified.*

import kotlinx.android.synthetic.main.activity_self_identify.*
import java.lang.Exception
import java.util.*
import kotlin.concurrent.schedule

class SelfIdentifyActivity : AppCompatActivity() {
    private val cloudVision = CloudVisionData()
    private val identSave =  IdentSaveToDatabase()
    private var identifiedPlantUUID = ""
    private var plantType = cloudVision.getBaseIdentLibrary().first() //Used to keep chosen plant type (from spinner)



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_self_identify)
        supportActionBar?.title = "Planty  |  Self Identify"

            verifyLoggedIn()
           populateSpinner()
        populateUserImage()
        SelfIdentify_Save_Button.setOnClickListener{
            if (checkPopulatedFields()){
                saveIdentification()
                navToMapsActivityWithIdent()
            }
        }
    }

    /**Change to MapsActivity with baseIdent and IdentifiedPlantUUID (ImageFileName)
     *
     */
    private fun navToMapsActivityWithIdent(){
        val intent = Intent(this, MapsActivity::class.java) //Populate intent with new activity class
        //  intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
        intent.putExtra("baseIdent",plantType)
        intent.putExtra("identifiedPlantUUID", identifiedPlantUUID)
        startActivity(intent) //Change to new class
    }

    /**Saves plantIdentification to Firebaser
     *
     */
    private fun saveIdentification(){//Produces a Identified object and saves to the database
        var plantName = getPlantName()
        var plantDesc = getPlantDesc()
        var imageName = getImageFileName()
        if (imageName.isNotEmpty()) {
            var identifiedPlant = identSave.getIdentObject(plantName, imageName, plantType, plantDesc)
            identifiedPlantUUID = identSave.saveIdentToDatabase(identifiedPlant)
            Log.d("SelfIdentifyActivity", "Identity correctly saved")
        }
    }

    /**Return the imageFile Name (UUID)
     *@return imageFileName (UUID)
     */
    private fun getImageFileName(): String {
            var filename = ""
            try {
                filename = intent.getStringExtra("fileName")

            }catch (e: Exception){
                Log.d("SelfIdentifyActivity", "getImageFileName Error = ${e.message}")
            }
            return filename
        }

    /**Returns the plant Name
     * @return Plant Name
     */
    private fun getPlantName(): String {
        val plantName = SelfIdentify_PlantName_EditText.text
        return plantName.toString()
    }

    /**Returns the plantDescription
     *@return  Plant description
     */
    private fun getPlantDesc(): String {
        val plantDesc = SelfIdentified_PlantDesc_EditText.text
        return plantDesc.toString()
    }

    /**Checks that all the fields are populated
     * @return Boolean
     */
    private fun checkPopulatedFields(): Boolean {
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

    /**Populates the spinner with all baseIDs
     *
     */
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
                Log.d("SelfIdentifyActivity","populateSpinner Error, nothing selected")
            }
        }
    }

    /**Retrieves the users images from the database, then passes them to getImgLoc()
     *
     */
    private fun populateUserImage(){//used to populate the user image at the top of the screen
        try {
            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("/userImages/$uid")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    getImgLoc(p0)
                }
                override fun onCancelled(p0: DatabaseError) {
                    Log.d("SelfIdentifyActivity", "Error Loading main image = ${p0.message}")
                }
            })
        }
        catch(e: Exception){
            Log.d("SelfIdentifyActivity","populateUserImage Error = ${e.message}")
        }
    }

    /**Loads the main image at the top of the activity
     * @param p0 Holds all images associated to the current user from Firebase
     */
    private fun getImgLoc(p0: DataSnapshot){
        var imageName = getImageFileName()
        var retryLoad = true //Used to determine if the image has been loaded yet
        p0.children.forEach {
            if (it.key.toString() == imageName) { //Compares the imageName to the Id name, to confirm the correct image details are loaded
                val currentImage = it.getValue(UserImage::class.java)
                val imgLoc = currentImage?.imageLoc
                Picasso.get().load(imgLoc).rotate(90f).into(SelfIdentify_PlantImage)
                retryLoad = false
            }
        }
        if (retryLoad == true) {
            retryImageLoad()//If image is not loaded, retry in 500ms
        }
    }

    /**Calls populateUserImage() after 500ms
     * Used to re-attempt image download, as the image may not have saved to Firebase yet
     */
    private fun retryImageLoad(){//Attempts to reload the image ever 500ms, if the image is not yet saved to firebase
        //Must be re-attempted untill the file is found, this function can load faster than the file is saved to firebase (THIS IS DONE AS THE IMAGE MAY NOT BE SAVED, THUS AVAILABLE YET)
        Timer("Retry Image Load", false).schedule(500) {
            populateUserImage()
        }
    }

    /**Confirm user is logged in, if not, change to LoginActivity
     *
     */
    private fun verifyLoggedIn(){ //Check if the User is already logged in, if not, return User to registerActivity
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null){//If no user ID, user is not logged in
            val intent =  Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
            startActivity(intent)
        }
    }

    /**Inflate the options menu
     *
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean { //Create the menu
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**Calls methods when a specific menu option is selected
     *
     */
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

    /**Sign the user out and change to LoginActivity
     *
     */
    private fun signOut(){
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    /**Change to MapsActivity
     *
     */
    private fun navToMapsActivity(){
        val intent = Intent(this, MapsActivity::class.java) //Populate intent with new activity class
        //  intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
        startActivity(intent) //Change to new class
    }

    /**Change to ProfileActivity
     *
     */
    private fun navToProfileActivity(){
        val intent = Intent(this, ProfileActivity::class.java) //Populate intent with new activity class
        //  intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
        startActivity(intent) //Change to new class
    }

    /**Change to IdentifyActivity
     *
     */
    private fun navToIdentifyActivity(){
        val intent = Intent(this, IdentifyActivity::class.java) //Populate intent with new activity class
        //  intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
        startActivity(intent) //Change to new class
    }

}
