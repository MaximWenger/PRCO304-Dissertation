package com.example.planty.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.example.planty.Classes.DataSort
import com.example.planty.R
import com.example.planty.Classes.DateTime
import com.example.planty.Classes.IdentSaveToDatabase
import com.example.planty.Objects.Identified
import com.example.planty.Objects.UserImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_identified.*
import java.lang.Exception
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.concurrent.schedule

class IdentifiedActivity : AppCompatActivity() {
    private var baseIdent = ""
    private var identifiedString = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identified)

        supportActionBar?.title ="Identified!" //change activity Title

        verifyLoggedIn()
        populateBaseIdent()
        populateIdentifiedString()
        populateIDRows()
        populateUserImage()




        identified_button0.setOnClickListener {//If ID 0 clicked

            val plantName = identified_name_textview0.text.toString()
            saveIdentChangeActiv(plantName)
            navToMapsActivityWithIdent(plantName)
        }
        identified_button1.setOnClickListener {//If ID 1 clicked
            val plantName = identified_name_textview1.text.toString()
            saveIdentChangeActiv(plantName)
            navToMapsActivityWithIdent(plantName)
        }
        identified_button2.setOnClickListener {//If ID 2 clicked
            val plantName = identified_name_textview2.text.toString()
            saveIdentChangeActiv(plantName)
            navToMapsActivityWithIdent(plantName)
        }
        identified_selfIdentify.setOnClickListener {
            navToSelfIdentifyActivity()
        }
    }

    private fun navToMapsActivityWithIdent(plantName: String){//Navigates to the Maps Activity, with the PlantName and baseIdent
        val intent = Intent(this, MapsActivity::class.java) //Populate intent with new activity class
        //  intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
        intent.putExtra("plantName",plantName)
        intent.putExtra("baseIdent",baseIdent)
        startActivity(intent) //Change to new class
    }




    private fun populateUserImage(){//used to populate the user image at the top of the screen
        try {
            val ref = FirebaseDatabase.getInstance().getReference("/userImages")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    getImgLoc(p0)
                }
                override fun onCancelled(p0: DatabaseError) {
                    Log.d("IdentifiedActivity", "Error Loading main image = ${p0.message}")
                }
            })
        }
        catch(e: Exception){
            Log.d("IdentifiedActivity","populateUserImage Error = ${e.message}")
        }
    }

    private fun getImgLoc(p0: DataSnapshot){
        var imageName = getImageFileName()
        var retryLoad = true //Used to determine if the image has been loaded yet
        p0.children.forEach {
            if (it.key.toString() == imageName) { //Compares the imageName to the Id name, to confirm the correct image details are loaded
                val currentImage = it.getValue(UserImage::class.java)
                val imgLoc = currentImage?.imageLoc
                Picasso.get().load(imgLoc).into(identified_userImage)
                retryLoad = false
            }
        }
        if (retryLoad == true) {
            retryImageLoad()//If image is not loaded, retry in 500ms
        }
    }

    private fun retryImageLoad(){//Attempts to reload the image ever 500ms, if the image is not yet saved to firebase
       //Must be re-attempted untill the file is found, this function can load faster than the file is saved to firebase
            Timer("Retry Image Load", false).schedule(500) {
                populateUserImage()
            }
    }

    private fun saveIdentChangeActiv(plantName: String){ //Uses the givenPlant name & saves the identifed Image & changes activity
        val identImageName = getImageFileName()
        val defaultDesc = ""
        val correctIdent = IdentSaveToDatabase().getIdentObject(plantName, identImageName, defaultDesc, baseIdent)
        IdentSaveToDatabase().saveIdentToDatabase(correctIdent)

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

    private fun getImageFileName(): String { //Return filename (UUID) for saved image
        var filename = ""
        try {
            filename = intent.getStringExtra("fileName")

        }catch (e: Exception){
            Log.d("IdentifiedActivity", "getImageFileName Error = ${e.message}")
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
            var singleIdent = DataSort().getSingleIdent(identifiedString, 0)//Make a local copy of the first three items
            var confidence = DataSort().convertTo2dp(singleIdent.get(0))//Convert string to 2dp
            identified_name_textview0.text = singleIdent.get(2)
            identified_confidence0.text = confidence
        }
        catch(e: Exception){
            Log.d("IdentifiedActivity", "PopulateIdent0 Error = ${e.message}")
        }
    }

    private fun populateIdent1(identifiedString: ArrayList<String>){//Populate the textfields with the identification information
        try {
            var singleIdent = DataSort().getSingleIdent(identifiedString, 3)
            var confidence = DataSort().convertTo2dp(singleIdent.get(0))//Convert string to 2dp
            identified_name_textview1.text = singleIdent.get(2)
            identified_confidence1.text = confidence
        }catch (e: Exception){
            Log.d("IdentifiedActivity", "PopulateIdent1 Error = ${e.message}")
        }
    }

    private fun populateIdent2(identifiedString: ArrayList<String>){//Populate the textfields with the identification information
        try {
            var singleIdent = DataSort().getSingleIdent(identifiedString, 6)
            var confidence = DataSort().convertTo2dp(singleIdent.get(0))//Convert string to 2dp
            identified_name_textview2.text = singleIdent.get(2)
            identified_confidence2.text = confidence
        }
        catch (e: Exception){
            Log.d("IdentifiedActivity", "PopulateIdent2 Error = ${e.message}")
        }
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

    private fun navToIdentifyActivity() {
        val intent = Intent(this, IdentifyActivity::class.java) //Populate intent with new activity class
        //  intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
        startActivity(intent) //Change to new class
    }

    private fun navToSelfIdentifyActivity(){
        val intent = Intent(this, SelfIdentifyActivity::class.java) //Populate intent with new activity class
        //  intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
        var imageFileName = getImageFileName()
        intent.putExtra("fileName",imageFileName)
        startActivity(intent) //Change to new class
    }
}


/*
---------
*/
/*                val currentImage = p0.getValue(UserImage::class.java)
            Log.d("IdentifiedActivity", "current Image to String = ${currentImage.toString()}")
            val imgLoc = currentImage?.imageLoc
            //val imgLoc = "https://firebasestorage.googleapis.com/v0/b/kotlinplanty.appspot.com/o/images%2F1d9e3e14-f3b6-4c97-8737-9b95eed88ddc?alt=media&token=d67b6883-e41d-4001-a86c-ef1fef77f92f"
            Log.d("IdentifiedActivity", "Image loc = ${imgLoc.toString()}")
            Log.d("IdentifiedActivity", "ImageName = ${imageName}")
            Picasso.get().load(imgLoc).into(identified_userImage)*/
