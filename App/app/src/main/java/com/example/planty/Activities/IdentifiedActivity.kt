package com.example.planty.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.example.planty.R
import com.example.planty.classes.DateTime
import com.example.planty.classes.Identified
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_identified.*
import java.lang.Exception
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class IdentifiedActivity : AppCompatActivity() {
    private var baseIdent = ""
    private var identifiedString = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identified)

        supportActionBar?.title ="Identified" //change activity Title

        verifyLoggedIn()
        populateBaseIdent()
        populateIdentifiedString()
        populateIDRows()

        identified_button0.setOnClickListener {//If ID 0 clicked

            val plantName = identified_name_textview0.text.toString()
            saveIdentChangeActiv(plantName)
        }
        identified_button1.setOnClickListener {//If ID 1 clicked
            val plantName = identified_name_textview1.text.toString()
            saveIdentChangeActiv(plantName)
        }
        identified_button2.setOnClickListener {//If ID 2 clicked
            val plantName = identified_name_textview2.text.toString()
            saveIdentChangeActiv(plantName)
        }
        identified_selfIdentify.setOnClickListener {
            Log.d("IdentifiedActivity","Self Idenfity Clicked")
        }
    }

    private fun saveIdentChangeActiv(plantName: String){ //Uses the givenPlant name & saves the identifed Image & changes activity
        val correctIdent = getCorrectIdent(plantName)
        saveIdentToDatabase(correctIdent)
        navToMapsActivity()
    }


    private fun getCorrectIdent(plantName: String): Identified {//returns identified object, populated with details of identifed plant
        val uid = FirebaseAuth.getInstance().uid.toString()
        val dateTime = DateTime().getDateTime()
        val identImageName = getFileName()
        val correctIdent = Identified(uid, dateTime, plantName, baseIdent, identImageName) //populate Identified object
        return correctIdent //return identified object
    }

    private fun saveIdentToDatabase(correctIdent: Identified){//Save correct identification to database
        val uuid = UUID.randomUUID().toString() //Produce unique ID for ident file name
        val ref = FirebaseDatabase.getInstance().getReference("/identifiedPlants/${uuid}")
        ref.setValue(correctIdent)
    }

    private fun populateBaseIdent(){
        try {
            baseIdent = intent.getStringExtra("baseIdent")
        }catch (e: Exception){
            Log.d("IdentifiedActivity", "populateBaseIdent Error = ${e.message}")
        }
    }

    private fun populateIdentifiedString(){
        try {
            identifiedString = intent.getStringArrayListExtra("identifications")
        }catch (e: Exception){
            Log.d("IdentifiedActivity", "populatedIdentifedString Error = ${e.message}")
        }
    }

    private fun getFileName(): String { //Return filename (UUID) for saved image
        var filename = ""
        try {
            filename = intent.getStringExtra("fileName")

        }catch (e: Exception){
            Log.d("IdentifiedActivity", "getFileName Error = ${e.message}")
        }
        return filename
    }



    private fun populateIDRows(){//Populate the text fields
        if (identifiedString.size == 3) { //If only one identification
            Log.d("IdentifiedActivity", "Got to 3")
            populateIdent0(identifiedString)
            hideIdent1()
            hideIdent2()
        }
        else if(identifiedString.size == 6){ //If only two identifications
            Log.d("IdentifiedActivity", "Got to 6")
            populateIdent0(identifiedString)
            populateIdent1(identifiedString)
            hideIdent2()
        }
        else{ //Show the top three identifications
            Log.d("IdentifiedActivity", "Got to ELSE")
            populateIdent0(identifiedString)
            populateIdent1(identifiedString)
            populateIdent2(identifiedString)
        }
    }

    private fun hideIdent1(){ //Hide the textviews, image and button
        identified_image1.visibility = View.INVISIBLE
        identified_name1.visibility = View.INVISIBLE
        identified_name_textview1.visibility = View.INVISIBLE
        identified_confidenceTextview1.visibility = View.INVISIBLE
        identified_confidence1.visibility = View.INVISIBLE
        identified_button1.visibility = View.INVISIBLE
        identified_descriptionTextview1.visibility = View.INVISIBLE
    }

    private fun hideIdent2(){//Hide the textviews, image and button
        identified_image2.visibility = View.INVISIBLE
        identified_name2.visibility = View.INVISIBLE
        identified_name_textview2.visibility = View.INVISIBLE
        identified_confidenceTextview2.visibility = View.INVISIBLE
        identified_confidence2.visibility = View.INVISIBLE
        identified_button2.visibility = View.INVISIBLE
        identified_descriptionTextview2.visibility = View.INVISIBLE
    }

    private fun populateIdent0(identifiedString: ArrayList<String>){//Populate the textfields with the identification information
        try {
            var singleIdent = getSingleIdent(identifiedString, 0)//Make a local copy of the first three items
            var confidence = convertTo2dp(singleIdent.get(0))//Convert string to 2dp
            identified_name_textview0.text = singleIdent.get(2)
            identified_confidence0.text = confidence
        }
        catch(e: Exception){
            Log.d("IdentifiedActivity", "PopulateIdent0 Error = ${e.message}")
        }
    }

    private fun populateIdent1(identifiedString: ArrayList<String>){//Populate the textfields with the identification information
        try {
            var singleIdent = getSingleIdent(identifiedString, 3)
            var confidence = convertTo2dp(singleIdent.get(0))//Convert string to 2dp
            identified_name_textview1.text = singleIdent.get(2)
            identified_confidence1.text = confidence
        }catch (e: Exception){
            Log.d("IdentifiedActivity", "PopulateIdent1 Error = ${e.message}")
        }
    }

    private fun populateIdent2(identifiedString: ArrayList<String>){//Populate the textfields with the identification information
        try {
            var singleIdent = getSingleIdent(identifiedString, 6)
            var confidence = convertTo2dp(singleIdent.get(0))//Convert string to 2dp
            identified_name_textview2.text = singleIdent.get(2)
            identified_confidence2.text = confidence
        }
        catch (e: Exception){
            Log.d("IdentifiedActivity", "PopulateIdent2 Error = ${e.message}")
        }
    }

    private fun convertTo2dp(string: String): String{ //Converts a decimal to 2dp, returning a string
        var origString = string.toDouble()
        val newString = BigDecimal(origString).setScale(2, RoundingMode.HALF_EVEN)//Round the confidence to 2dp
        return newString.toString()
    }



    private fun getSingleIdent(identifiedString: ArrayList<String>, indexStart: Int) : ArrayList<String>{ //Returns an ArrayList, 3 long from the provided ArrayList
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

    //Show top three reccomendations
    //Populate the static textfields
    //hide the fields which are not used
    //have an temp image for each plant
    //redownload the identified plant

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


