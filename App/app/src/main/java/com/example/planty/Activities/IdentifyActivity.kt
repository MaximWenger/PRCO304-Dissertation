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
import android.widget.Toast
import com.example.planty.R
import com.example.planty.Objects.UserImage
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
    var selectedPhotoUri: Uri? = null //Stores photo
    private var filename = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identify)
        supportActionBar?.title = "Planty  |  Identify Plants"

        verifyLoggedIn()//check the User is logged in
        selectgallery_button_Identify.setOnClickListener{ //Called when gallery icon is selected
            getGalleryImage()// Get image from device gallery
        }
        selectcamera_button_Identify.setOnClickListener{
            getCameraImage()//Open device camera and use the image taken
        }
    }

    /**Changes to the Camera Activity
     *
     */
    private fun getCameraImage(){//Open device camera and use the image taken
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 0)
        }
        catch(e: Exception){
        Log.d("IdentifyActivity", "getCameraImage = ${e.message}")
        }
    }

    /**Changes to the Gallery Activity
     *
     */
    private fun getGalleryImage(){ // Get image from device gallery
        Log.d("IdentifyActivity", "Clicked the gallery")
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*" //View all photo directories on the phone
        startActivityForResult(intent, 0)
    }

    /**Determines if the image contains a plant
     * if the image contains a plant, the image is saved, labels are sorted and the activity is changed.
     *If not, the user is prompted to try another photo
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { //Gets called after image is chosen from gallery
        try {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) { //Check the photo is selected
                Log.d("IdentifyActivity", "Photo was selected")
                selectedPhotoUri = data.data
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)//Convert the resulting image to a bitmap
                val image = FirebaseVisionImage.fromBitmap(bitmap) //Convert the bitmap into an image designed for ML Firebase //NOT CHECKED FOR ROTATION
                val labeler = FirebaseVision.getInstance().getCloudImageLabeler()
                labeler.processImage(image)
                    .addOnSuccessListener { labels ->
                        if (CloudVisionData().confirmPlant(labels)) { //check if the image looks to have a plant
                                  saveImageToFirebase()              //save the image to firebase
                                    var baseIdent = CloudVisionData().baseImageIdentFilter(labels)//Return base identification
                                       var sortedList = CloudVisionData().imageDataFilter(labels) //Sort the vision data
                                  passStringNewActivity(sortedList, baseIdent)//Pass the data to new activity & change activity
                            } else { //If it is NOT a plant
                                Toast.makeText(this, "This photo is not a plant, please try another photo", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.d("IdentifyActivity", "Something went wrong : ${e.message}")
                    }
            }
        }
        catch (e: Exception){
            Log.d("IdentifyActivity", "Image processing broke = ${e.message}")
        }
    }

    /**Saves the photo to the database and calls saveImageIDToUserFirebase() to save Identification
     *
     */
    private fun saveImageToFirebase(){ //Used to save the user image to firebase storage
        if (selectedPhotoUri == null)return
        filename = UUID.randomUUID().toString() //Populate filename with new UUID
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            Log.d("IdentifyActivity", "Photo Saved!")
            ref.downloadUrl.addOnSuccessListener {
                saveImageIDToUserFirebase(it.toString()) //Converts the img URL to string
            }
        }
            .addOnFailureListener{
                Log.d("IdentifyActivity", "Error saveImageToFirebase. Something went wrong with photo save")
            }
    }

    /**Saves image Identification to the database
     * @param path Photo URI
     */
    private fun saveImageIDToUserFirebase(photoURI: String?){//Saves the saved image details, to Firebase database (to associate the image to the user)
        val dateTime = DateTime().getDateTime() //Returns dateTime
        val uid = FirebaseAuth.getInstance().uid ?: "" //Elvis operator //Using the unique ID (Used to link authenticated User to the database within Firebase
        val ref = FirebaseDatabase.getInstance().getReference("/userImages/$uid/$filename")//Using the unique ID (Used to link authenticated User to the database within Firebase) as a unique name within Firebase
        val imageUID = UserImage(photoURI, dateTime)
         ref.setValue(imageUID).addOnSuccessListener {
            Log.d("IdentifyActivity", "UID saved")
        }
    }

    /**Change to IdentifiedActivity with populated intents, Identifications, baseIdent & fileName
     *@param sortedList CloudVision label data
     *@param baseIdent baseId
     */
    private fun passStringNewActivity(sortedList: MutableList<FirebaseVisionImageLabel>, baseIdent: String){ //Passes List into ArrayList, then sends that array to the new activity. Changing the activity
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
        intent.putExtra("fileName",filename)
        startActivity(intent)//Change to the new activity
    }

    /**Checks the user is logged in, returns to Login if not logged in
     *
     */
    private fun verifyLoggedIn(){ //Check if the User is already logged in, if not, return User to registerActivity
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null){
            val intent =  Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
            startActivity(intent)
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
        when (item?.itemId){ //Switch statement
            R.id.nav_Profile -> { //DOES NOTHING RIGHT NOW
                navToProfileActivity() //Go to ProfileActivity
            }
            R.id.nav_Identify -> {
                return super.onOptionsItemSelected(item)  //Return as already within Identify Activity
            }
            R.id.nav_Find -> {
                navToMapsActivity() //Go to MapsActivity
            }
            R.id.nav_Sign_Out -> {
                signOut() //Signs the User out and returns to RegisterActivity
            }
            R.id.nav_Contact -> { //DOES NOTHING RIGHT NOW
                navToContactUsActivity()
            }
            else ->  return super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    /**Logs user out and changed to RegisterActivity
     *
     */
    private fun signOut(){
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, RegisterActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    /**Changes to MapsActivity
     *
     */
    private fun navToMapsActivity(){
        val intent = Intent(this, MapsActivity::class.java) //Populate intent with new activity class
      //  intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
        startActivity(intent) //Change to new class
    }

    /**Changes to ProfileActivity
     *
     */
    private fun navToProfileActivity(){
        val intent = Intent(this, ProfileActivity::class.java) //Populate intent with new activity class
        //  intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
        startActivity(intent) //Change to new class
    }

    private fun navToContactUsActivity(){
        val intent = Intent(this, ContactUsActivity::class.java) //Populate intent with new activity class
        //  intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
        startActivity(intent) //Change to new class
    }
}

