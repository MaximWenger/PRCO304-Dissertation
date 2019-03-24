package com.example.planty

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.system.Os.remove
import android.text.TextUtils.indexOf
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import kotlinx.android.synthetic.main.activity_identify.*
import java.lang.Exception

class IdentifyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identify)

        supportActionBar?.title = "Planty  |  Identify Plants"

        verifyLoggedIn()//check the user is logged in

        selectgallery_button_Identify.setOnClickListener{ //Called when gallery icon is selected

            getGalleryImage()// Get image from device gallery
        }
        selectcamera_button_Identify.setOnClickListener{
            getCameraImage()//Open device camera and use the image taken
        }
    }

    private fun getCameraImage(){//Open device camera and use the image taken
        try {
            Log.d("IdentifyActivity", "Clicked the camera")
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 0)
        }
        catch(e: Exception){
        Log.d("IdentifyActivity", "getCameraImage = ${e.message}")
        }
    }

    private fun getGalleryImage(){ // Get image from device gallery
        Log.d("IdentifyActivity", "Clicked the gallery")
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*" //View all photo directories on the phone
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { //Gets called after image is chosen from gallery
        try {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) { //Check the photo is selected
                Log.d("IdentifyActivity", "Photo was selected")
                val uri = data.data
                val bitmap =
                    MediaStore.Images.Media.getBitmap(contentResolver, uri)//Convert the resulting image to a bitmap
                val image =
                    FirebaseVisionImage.fromBitmap(bitmap) //Convert the bitmap into an image designed for ML Firebase //NOT CHECKED FOR ROTATION
                val labeler = FirebaseVision.getInstance().getCloudImageLabeler()
                labeler.processImage(image)
                    .addOnSuccessListener { labels ->
                        Log.d("IdentifyActivity", "It worked!")
/*                        for (label in labels) {
                            val text = label.text
                            val entityId = label.entityId
                            val confidence = label.confidence
                            Log.d("IdentifyActivity", "Text = ${text}")
                            Log.d("IdentifyActivity", "entityID = ${entityId}")
                            Log.d("IdentifyActivity", "confidence = ${confidence}")
                            Log.d("IdentifyActivity", "Total amount = ${labels.size}")
                        }*/
                        imageDataFilter(labels)
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

    private fun imageDataFilter(list: MutableList<FirebaseVisionImageLabel>){ //Change the immutable list to mutable
        var index = 0
    for (label in list){
        index++
        val text = label.text
        val entityId = label.entityId
        val confidence = label.confidence
        Log.d("IdentifyActivity", "Text = ${text}")
        Log.d("IdentifyActivity", "entityID = ${entityId}")
        Log.d("IdentifyActivity", "confidence = ${confidence}")
        Log.d("IdentifyActivity", "Total amount = ${list.size}")

       if (compareLabel(text)) { //If the label has been found within the reserved words. remove it
            Log.d("IdentifyActivity", "Removing = ${text}")

           try {
               // list.remove(label)
               //list.removeAt(1)
               var test: MutableList<FirebaseVisionImageLabel> =
               //////////////////////////////////////////////////////////////////////////////////

               Log.d("IdentifyActivity", "WOULD REMOVE")
           }catch (e: Exception){
               Log.d("IdentifyActivity", "ERROR = ${e.message}")
           }
       }
    }


    }

    private fun compareLabel(text: String): Boolean { //Returns Boolean if the given string is within the existing blacklist (library)

        val toRemoveLibary = arrayOf("Petal","Plant", "Yellow", "Flower", "Flowering Plant", "Spring", "Wildflower")
        val lowerCaseLibary = toRemoveLibary.map { it.toLowerCase() }//Convert entire array to lowercase
        var lowerCaseText = text.decapitalize() //Need to use decapitize as .toLowerCase uses an array

        if (lowerCaseLibary.contains(lowerCaseText)){ //If the libary contains a key word
            return true
        }
        return false
    }



    private fun verifyLoggedIn(){ //Check if the user is already logged in, if not, return user to registerActivity
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null){
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
                return super.onOptionsItemSelected(item)  //Return as already within Identify Activity
            }
            R.id.nav_Find -> {
                navToMapsActivity() //Go to MapsActivity
            }
            R.id.nav_Sign_Out -> {
                signOut() //Signs the user out and returns to RegisterActivity
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
}
 //CURRENTLY AT 14:59, ADDING A META TAG TO THE MANIFEST TO ADD A BACK BUTTON IN THE RIGHT AREA

