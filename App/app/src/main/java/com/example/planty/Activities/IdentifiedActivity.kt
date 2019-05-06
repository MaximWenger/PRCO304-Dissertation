package com.example.planty.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.example.planty.Classes.ActivityNavigation
import com.example.planty.Classes.DataSort
import com.example.planty.R
import com.example.planty.Classes.IdentSaveToDatabase
import com.example.planty.Entities.Plant
import com.example.planty.Entities.UserImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_identified.*
import java.lang.Exception
import java.util.*
import kotlin.concurrent.schedule

class IdentifiedActivity : AppCompatActivity() {
    private var baseIdent = ""
    private var identifiedPlantUUID = ""
    private var identifiedString = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identified)

        supportActionBar?.title = "Planty  |  Identified!"//change activity Title

        ActivityNavigation.verifyLoggedIn(this)
        populateBaseIdent()
        populateIdentifiedString()
        populateIDRows()
        populateUserImage()




        identified_button0.setOnClickListener {
            //If ID 0 clicked
            val plantName = identified_name_textview0.text.toString()
            saveIdentChangeActiv(plantName)
            navToMapsActivityWithIdent(plantName)
        }
        identified_button1.setOnClickListener {
            //If ID 1 clicked
            val plantName = identified_name_textview1.text.toString()
            saveIdentChangeActiv(plantName)
            navToMapsActivityWithIdent(plantName)
        }
        identified_button2.setOnClickListener {
            //If ID 2 clicked
            val plantName = identified_name_textview2.text.toString()
            saveIdentChangeActiv(plantName)
            navToMapsActivityWithIdent(plantName)
        }
        identified_selfIdentify.setOnClickListener {
            navToSelfIdentifyActivity()
        }
    }

    /**Changes to MapsActivity, saving plantName, baseIdent and IdentifiedPlantUUID in intents
     * @param plantName Plant Name
     */
    private fun navToMapsActivityWithIdent(plantName: String) {//Navigates to the Maps Activity, with the PlantName and baseIdent
        val intent = Intent(this, MapsActivity::class.java) //Populate intent with new activity class
        //  intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
        intent.putExtra("plantName", plantName)
        intent.putExtra("baseIdent", baseIdent)
        intent.putExtra("identifiedPlantUUID", identifiedPlantUUID)
        startActivity(intent) //Change to new class
    }

    /**Retrieves all user Images and passes them within DataSnapShot object to getImgLoc()
     *
     */
    private fun populateUserImage() {//used to populate the user image at the top of the screen
        try {
            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("/userImages/$uid")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    getImgLoc(p0)
                }

                override fun onCancelled(p0: DatabaseError) {
                    Log.d("IdentifiedActivity", "Error Loading main image = ${p0.message}")
                }
            })
        } catch (e: Exception) {
            Log.d("IdentifiedActivity", "populateUserImage Error = ${e.message}")
        }
    }

    /**Populates the plant being Identified Image at the top of the activity
     * @param p0 Object holding all UserImage objects
     */
    private fun getImgLoc(p0: DataSnapshot) {
        var imageName = getImageFileName()
        var retryLoad = true //Used to determine if the image has been loaded yet
        p0.children.forEach {
            if (it.key.toString() == imageName) { //Compares the imageName to the Id name, to confirm the correct image details are loaded
                val currentImage = it.getValue(UserImage::class.java)
                val imgLoc = currentImage?.imageLoc
                Picasso.get().load(imgLoc).rotate(90f).into(identified_userImage)
                retryLoad = false
            }
        }
        if (retryLoad == true) {
            retryImageLoad()//If image is not loaded, retry in 500ms
        }
    }

    /**Calls populateUserImage() after 500ms to try and load the image
     * This is used, as the image may not have been saved to Firebase yet, so a re-attempt to download the image is
     * required
     */
    private fun retryImageLoad() {//Attempts to reload the image ever 500ms, if the image is not yet saved to firebase
        //Must be re-attempted untill the file is found, this function can load faster than the file is saved to firebase
        Timer("Retry Image Load", false).schedule(500) {
            populateUserImage()
        }
    }

    /**Saves the identification to the database
     * @param Plant Name
     */
    private fun saveIdentChangeActiv(plantName: String) { //Uses the givenPlant name & saves the identifed Image
        val identImageName = getImageFileName()
        val defaultDesc = ""
        val correctIdent = IdentSaveToDatabase().getIdentObject(plantName, identImageName, defaultDesc, baseIdent)
        identifiedPlantUUID = IdentSaveToDatabase().saveIdentToDatabase(correctIdent)
    }

    /**Populates baseIdent Global Var with the baseIdent
     *
     */
    private fun populateBaseIdent() {
        try {
            baseIdent = intent.getStringExtra("baseIdent")
        } catch (e: Exception) {
            Log.d("IdentifiedActivity", "populateBaseIdent Error = ${e.message}")
        }
    }

    /**Populates identifiedString Global Var with List of identifications (e.g.Text, confidence)
     *
     */
    private fun populateIdentifiedString() {
        try {
            identifiedString = intent.getStringArrayListExtra("identifications")
        } catch (e: Exception) {
            Log.d("IdentifiedActivity", "populatedIdentifedString Error = ${e.message}")
        }
    }

    /**Returns the imageFileName (UUID)
     *@return imageID (UUID)
     */
    private fun getImageFileName(): String { //Return filename (UUID) for saved image
        var filename = ""
        try {
            filename = intent.getStringExtra("fileName")

        } catch (e: Exception) {
            Log.d("IdentifiedActivity", "getImageFileName Error = ${e.message}")
        }
        return filename
    }

    /**Calls populateIdentx() to Populate and hideIdentx() to Hide the relevant Identification fields
     *
     */
    private fun populateIDRows() {//Populate the text fields
        if (identifiedString.size == 3) { //If only one identification
            Log.d("IdentifiedActivity", "Got to 3")
            populateIdent0(identifiedString)
            hideIdent1()
            hideIdent2()
        } else if (identifiedString.size == 6) { //If only two identifications
            Log.d("IdentifiedActivity", "Got to 6")
            populateIdent0(identifiedString)
            populateIdent1(identifiedString)
            hideIdent2()
        } else { //Show the top three identifications
            Log.d("IdentifiedActivity", "Got to ELSE")
            populateIdent0(identifiedString)
            populateIdent1(identifiedString)
            populateIdent2(identifiedString)
        }
    }

    /**Hides the fields for Identification1
     *
     */
    private fun hideIdent1() { //Hide the textviews, image and button
        identified_image1.visibility = View.INVISIBLE
        identified_name1.visibility = View.INVISIBLE
        identified_name_textview1.visibility = View.INVISIBLE
        identified_confidenceTextview1.visibility = View.INVISIBLE
        identified_confidence1.visibility = View.INVISIBLE
        identified_button1.visibility = View.INVISIBLE
        identified_descriptionTextview1.visibility = View.INVISIBLE
    }

    /**Hides the fields for Identification2
     *
     */
    private fun hideIdent2() {//Hide the textviews, image and button
        identified_image2.visibility = View.INVISIBLE
        identified_name2.visibility = View.INVISIBLE
        identified_name_textview2.visibility = View.INVISIBLE
        identified_confidenceTextview2.visibility = View.INVISIBLE
        identified_confidence2.visibility = View.INVISIBLE
        identified_button2.visibility = View.INVISIBLE
        identified_descriptionTextview2.visibility = View.INVISIBLE
    }

    /**Populates Ident0 with identification data
     * @param identifiedString List containing identification data
     */
    private fun populateIdent0(identifiedString: ArrayList<String>) {//Populate the textfields with the identification information
        try {
            var singleIdent = DataSort().getSingleIdent(identifiedString, 0)//Make a local copy of the first three items
            var confidence = DataSort().convertTo2dp(singleIdent.get(0))//Convert string to 2dp
            identified_name_textview0.text = singleIdent.get(2)
            identified_confidence0.text = confidence

            var plantNameLowerCase = singleIdent.get(2).toLowerCase()
            val ref = FirebaseDatabase.getInstance()
                .getReference("/specPlants/$baseIdent/${plantNameLowerCase.capitalize()}/Details")//Exact, works off of the EXACT plant name
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    Log.d(
                        "IdentifiedActivity",
                        "TESTING populateIdent0, baseIdent = $baseIdent, PlantNameWithCap ${plantNameLowerCase.capitalize()}"
                    )
                    var identifiedPlant = p0.getValue(Plant::class.java)
                    Log.d("IdentifiedActivity", " Desc: ${identifiedPlant?.Description}")
                    if (identifiedPlant?.Description != null) {
                        identified_descriptionTextview0.text = identifiedPlant?.Description
                        Picasso.get().load(identifiedPlant?.Image).into(identified_image0)
                    } else {
                        identified_descriptionTextview0.visibility = View.INVISIBLE
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    Log.d("IdentifiedActivity", "Error populateIdent0 = ${p0.message}")
                }
            })
        } catch (e: Exception) {
            Log.d("IdentifiedActivity", "PopulateIdent0 Error = ${e.message}")
        }
    }

    /**Populates Ident1 with identification data
     * @param identifiedString List containing identification data
     */
    private fun populateIdent1(identifiedString: ArrayList<String>) {//Populate the textfields with the identification information
        try {
            var singleIdent = DataSort().getSingleIdent(identifiedString, 3)
            var confidence = DataSort().convertTo2dp(singleIdent.get(0))//Convert string to 2dp
            identified_name_textview1.text = singleIdent.get(2)
            identified_confidence1.text = confidence

            var plantNameLowerCase = singleIdent.get(2).toLowerCase()
            val ref = FirebaseDatabase.getInstance()
                .getReference("/specPlants/$baseIdent/${plantNameLowerCase.capitalize()}/Details")//Exact, works off of the EXACT plant name
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    Log.d(
                        "IdentifiedActivity",
                        "TESTING populateIdent1, baseIdent = $baseIdent, PlantNameWithCap ${plantNameLowerCase.capitalize()}"
                    )
                    var identifiedPlant = p0.getValue(Plant::class.java)
                    Log.d("IdentifiedActivity", " populateIdent1 ${identifiedPlant?.Description}")
                    if (identifiedPlant?.Description != null) {
                        identified_descriptionTextview1.text = identifiedPlant?.Description
                        Picasso.get().load(identifiedPlant?.Image).into(identified_image1)
                    } else {
                        identified_descriptionTextview1.visibility = View.INVISIBLE
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    Log.d("IdentifiedActivity", "Error populateIdent1 = ${p0.message}")
                }
            })


        } catch (e: Exception) {
            Log.d("IdentifiedActivity", "PopulateIdent1 Error = ${e.message}")
        }
    }

/*    private fun populateSpecific(foundCorrectID: String){
        Log.d("IdentifiedActivity", "Passed Correct ID =$foundCorrectID, baseIdent = $baseIdent")
        Log.d("IdentifiedActivity", "Path is /specPlants/$baseIdent/$foundCorrectID}/Details/")
        try {
            val ref = FirebaseDatabase.getInstance().getReference("/specPlants/$baseIdent/$foundCorrectID}/Details")//Exact, works off of the EXACT plant name
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    //Log.d("IdentifiedActivity", "TESTING P0 = $p0")
                    var identifiedPlant = p0.getValue(Plant::class.java)
                    Log.d("IdentifiedActivity", "Key = ${p0.key.toString()}, p0 = $p0")
                    Log.d("IdentifiedActivity", "KEY KEY KEY KEY KEY KEY Desc: ${identifiedPlant?.Description}")
                    //identified_descriptionTextview1.text = identifiedPlant?.Description
                    //Picasso.get().load(identifiedPlant?.Image).into(identified_image1)
                }
                override fun onCancelled(p0: DatabaseError) {
                    Log.d("IdentifiedActivity", "Error populateIdent0 = ${p0.message}")
                }
            })
        }
        catch(e: Exception){
            Log.d("IdentifiedActivity","populateUserImage Error = ${e.message}")
        }

    }*/

    /**Populates Ident2 with identification data
     * @param identifiedString List containing identification data
     */
    private fun populateIdent2(identifiedString: ArrayList<String>) {//Populate the textfields with the identification information
        try {
            var singleIdent = DataSort().getSingleIdent(identifiedString, 6)
            var confidence = DataSort().convertTo2dp(singleIdent.get(0))//Convert string to 2dp
            identified_name_textview2.text = singleIdent.get(2)
            identified_confidence2.text = confidence


            var plantNameLowerCase = singleIdent.get(2).toLowerCase()
            val ref = FirebaseDatabase.getInstance()
                .getReference("/specPlants/$baseIdent/${plantNameLowerCase.capitalize()}/Details")//Exact, works off of the EXACT plant name
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    Log.d(
                        "IdentifiedActivity",
                        "TESTING populateIdent2, baseIdent = $baseIdent, PlantNameWithCap ${plantNameLowerCase.capitalize()}"
                    )
                    var identifiedPlant = p0.getValue(Plant::class.java)
                    Log.d("IdentifiedActivity", "Desc: ${identifiedPlant?.Description}")
                    if (identifiedPlant?.Description != null) {
                        identified_descriptionTextview2.text = identifiedPlant?.Description
                        Picasso.get().load(identifiedPlant?.Image).into(identified_image2)
                    } else {
                        identified_descriptionTextview2.visibility = View.INVISIBLE
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    Log.d("IdentifiedActivity", "Error populateIdent2 = ${p0.message}")
                }
            })
        } catch (e: Exception) {
            Log.d("IdentifiedActivity", "PopulateIdent2 Error = ${e.message}")
        }
    }


    /**Inflates the Options menu in the top right of the activity
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
        when (item?.itemId) { //Switch statement
            R.id.nav_Profile -> { //DOES NOTHING RIGHT NOW
                ActivityNavigation.navToProfileActivity(this) //Go to ProfileActivity
            }
            R.id.nav_Identify -> {
                ActivityNavigation.navToIdentifyActivity(this)
            }
            R.id.nav_Find -> {
                ActivityNavigation.navToMapsActivity(this) //Go to MapsActivity
            }
            R.id.nav_Sign_Out -> {
                ActivityNavigation.signOut(this) //Signs the User out and returns to RegisterActivity
            }
            R.id.nav_Contact -> { //DOES NOTHING RIGHT NOW
                ActivityNavigation.navToContactUsActivity(this)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navToSelfIdentifyActivity() {
        val intent = Intent(this, SelfIdentifyActivity::class.java) //Populate intent with new activity class
        //  intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
        var imageFileName = getImageFileName()
        intent.putExtra("fileName", imageFileName)
        startActivity(intent) //Change to new class
    }
}


/*
                val ref = FirebaseDatabase.getInstance().getReference("/specPlants/$baseIdent")
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        p0.children.forEach{
                            val plantName = it.key.toString().toLowerCase()
                            if (DataSort().findIfDataContains(plantNameLowerCase, plantName)) {//Compare every name in database, if there's one which is the same name or contained within it, save it to the database
                                foundCorrectID = it.key.toString()
                            }
                        }
                        populateSpecific(foundCorrectID)
                    }

                    override fun onCancelled(p0: DatabaseError) {
                        Log.d("IdentifiedActivity", "Error populateIdent0 = ${p0.message}")
                    }
                })
 */
