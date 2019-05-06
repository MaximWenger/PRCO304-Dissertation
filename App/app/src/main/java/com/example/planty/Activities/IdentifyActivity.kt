package com.example.planty.Activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.example.planty.Classes.ActivityNavigation
import com.example.planty.R
import com.example.planty.Entities.UserImage
import com.example.planty.Classes.CloudVisionData
import com.example.planty.Classes.DateTime
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_identify.*
import java.lang.Exception
import java.util.*

class IdentifyActivity : AppCompatActivity() {
    var lastSelectedPhotoUri: Uri? = null //Stores photo
    private var filename = ""

    companion object {
        val SELECT_PHOTO_REQUEST_CODE = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identify)
        supportActionBar?.title = "Planty  |  Identify Plants"

        hideSelfIdentify()

        IdentifyActivity_Button_SelfIdentify.setOnClickListener {
            saveImageToFirebase(lastSelectedPhotoUri)
            passStringSelfIdentifyActivity()
        }

        ActivityNavigation.verifyLoggedIn(this)//check the User is logged in
        selectgallery_button_Identify.setOnClickListener {
            //Called when gallery icon is selected
            getGalleryImage()// Get image from device gallery
        }
        selectcamera_button_Identify.setOnClickListener {
            getCameraImage()//Open device camera and use the image taken
        }
    }

    /**Hides Button and Textview for Self-identifying
     *
     */
    private fun hideSelfIdentify() {
        IdentifyActivity_Button_SelfIdentify.visibility = View.INVISIBLE
        IdentifyActivity_TextView_ActuallyPlant.visibility = View.INVISIBLE
    }

    /**Shows Button and Textview for Self-identifying
     *
     */
    private fun showSelfIdentify() {
        IdentifyActivity_Button_SelfIdentify.visibility = View.VISIBLE
        IdentifyActivity_TextView_ActuallyPlant.visibility = View.VISIBLE
    }

    /**
     * Changes to the Camera Activity
     */
    private fun getCameraImage() {//Open device camera and use the image taken
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, SELECT_PHOTO_REQUEST_CODE)
        } catch (e: Exception) {
            Log.d("IdentifyActivity", "getCameraImage = ${e.message}")
        }
    }

    /**Changes to the Gallery Activity
     *
     */
    private fun getGalleryImage() { // Get image from device gallery
        Log.d("IdentifyActivity", "Clicked the gallery")
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*" //View all photo directories on the phone
        startActivityForResult(intent, SELECT_PHOTO_REQUEST_CODE)
    }

    /**Determines if the image contains a plant
     * if the image contains a plant, the image is saved, labels are sorted and the activity is changed.
     * If not, the user is prompted to try another photo
     */
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) { //Gets called after image is chosen from gallery
        try {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == SELECT_PHOTO_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) { //Check the photo is selected
                Log.d("IdentifyActivity", "Photo was selected")
                lastSelectedPhotoUri = data.data
                val bitmap = MediaStore.Images.Media.getBitmap(
                    contentResolver,
                    lastSelectedPhotoUri
                )//Convert the resulting image to a bitmap
                val image =
                    FirebaseVisionImage.fromBitmap(bitmap) //Convert the bitmap into an image designed for ML Firebase //NOT CHECKED FOR ROTATION
                val labeler = FirebaseVision.getInstance().cloudImageLabeler
                labeler.processImage(image)
                    .addOnSuccessListener { labels ->
                        if (CloudVisionData().confirmPlant(labels)) { //check if the image looks to have a plant
                            saveImageToFirebase(lastSelectedPhotoUri)              //save the image to firebase
                            val baseIdent = CloudVisionData().baseImageIdentFilter(labels)//Return base identification
                            val sortedList = CloudVisionData().imageDataFilter(labels) //Sort the vision data
                            passStringIdentifiedActivity(
                                sortedList,
                                baseIdent
                            )//Pass the data to new activity & change activity
                        } else { //If it is NOT a plant
                            showSelfIdentify()
                            Toast.makeText(
                                this,
                                "This photo is not a plant, please try another photo",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.d("IdentifyActivity", "Something went wrong : ${e.message}")
                    }
            }
            if (data == null){
                Toast.makeText(
                    this,
                    "Something went wrong, could not fetch image.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Log.d("IdentifyActivity", "Image processing broke = ${e.message}")
        }
    }

    /**Saves the photo to the database and calls saveImageIDToUserFirebase() to save Identification
     *
     */
    private fun saveImageToFirebase(lastSelectedPhotoUri: Uri?) { //Used to save the user image to firebase storage
        if (lastSelectedPhotoUri == null) return
        filename = UUID.randomUUID().toString() //Populate filename with new UUID
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(lastSelectedPhotoUri).addOnSuccessListener {
            Log.d("IdentifyActivity", "Photo Saved!")
            ref.downloadUrl.addOnSuccessListener {
                saveImageIDToUserFirebase(it.toString()) //Converts the img URL to string
            }
        }
            .addOnFailureListener {
                Log.d("IdentifyActivity", "Error saveImageToFirebase. Something went wrong with photo save")
            }
    }

    /**Saves image Identification to the database
     * @param path Photo URI
     */
    private fun saveImageIDToUserFirebase(photoURI: String?) {//Saves the saved image details, to Firebase database (to associate the image to the user)
        val dateTime = DateTime.getDateTime() //Returns dateTime
        val uid = FirebaseAuth.getInstance().uid
            ?: "" //Elvis operator //Using the unique ID (Used to link authenticated User to the database within Firebase
        val ref = FirebaseDatabase.getInstance()
            .getReference("/userImages/$uid/$filename")//Using the unique ID (Used to link authenticated User to the database within Firebase) as a unique name within Firebase
        val imageUID = UserImage(photoURI, dateTime)
        ref.setValue(imageUID).addOnSuccessListener {
            Log.d("IdentifyActivity", "UID saved")
        }
    }

    /**Change to IdentifiedActivity with populated intents, Identifications, baseIdent & fileName
     *@param sortedList CloudVision label data
     *@param baseIdent baseId
     */
    private fun passStringIdentifiedActivity(
        sortedList: MutableList<FirebaseVisionImageLabel>,
        baseIdent: String
    ) { //Passes List into ArrayList, then sends that array to the new activity. Changing the activity
        val intent = Intent(this, IdentifiedActivity::class.java)
        var counter = -1
        var stringList = ArrayList<String>()
        for (label in sortedList) {
            stringList.add(counter + 1, label.text)
            stringList.add(counter + 1, label.entityId.toString())
            stringList.add(counter + 1, label.confidence.toString())
        }
        var b = Bundle()
        b.putStringArrayList("identifications", stringList)
        intent.putExtras(b)
        intent.putExtra("baseIdent", baseIdent)
        intent.putExtra("fileName", filename)
        startActivity(intent)//Change to the new activity
    }

    private fun passStringSelfIdentifyActivity() {
        val intent = Intent(this, SelfIdentifyActivity::class.java)
        intent.putExtra("fileName", filename)
        startActivity(intent)//Change to the new activity
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
                return super.onOptionsItemSelected(item)  //Return as already within Identify Activity
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

}

