package com.example.planty.Activities

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.planty.R
import com.example.planty.classes.cloudVisionData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.android.synthetic.main.activity_identify.*
import java.lang.Exception

class IdentifyActivity : AppCompatActivity() {

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
           // var sortedList = mutableListOf<FirebaseVisionImageLabel>()
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) { //Check the photo is selected
                Log.d("IdentifyActivity", "Photo was selected")
                val uri = data.data
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)//Convert the resulting image to a bitmap
                val image = FirebaseVisionImage.fromBitmap(bitmap) //Convert the bitmap into an image designed for ML Firebase //NOT CHECKED FOR ROTATION
                val labeler = FirebaseVision.getInstance().getCloudImageLabeler()
                labeler.processImage(image)
                    .addOnSuccessListener { labels ->
                        Log.d("IdentifyActivity", "It worked!")
                        if (cloudVisionData().confirmPlant(labels)) { //check if the image looks to have a plant
                            Log.d("IdentifyActivity","THIS IS A PLANT")
                         var sortedList =  cloudVisionData().imageDataFilter(labels) //Sort the vision data

                            val intent = Intent(this, ProfileActivity::class.java)
                           // intent.putExtra("totalSize", sortedList.size)

                          // var testtest =  ArrayList(sortedList)

                            ///Create arraylist from sortedList
                           // var testList: MutableList<String!> = mutableListOf()
                            var counter = -1
                            var testList = ArrayList<String>()
                            for (label in sortedList) {
                                testList.add(counter + 1, label.text)
                                testList.add(counter + 1, label.entityId.toString())
                                testList.add(counter + 1, label.confidence.toString())
                            }
                            var b = Bundle()
                            b.putStringArrayList("test", testList)
                            intent.putExtras(b)
                            startActivity(intent)
                            //intent.putParcelableArrayListExtra("test", ArrayList(testList))



                            startActivity(intent)

                            Log.d("IdentifyActivity","${sortedList.size}")/////////////////////////////////////////////////////////////////////////////
                        }
                        else {
                            Log.d("IdentifyActivity", "NOT A PLANT")
                            Toast.makeText(this, "NOT A PLANT BOI", Toast.LENGTH_SHORT).show()
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





    private fun verifyLoggedIn(){ //Check if the User is already logged in, if not, return User to registerActivity
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
}
 //CURRENTLY AT 14:59, ADDING A META TAG TO THE MANIFEST TO ADD A BACK BUTTON IN THE RIGHT AREA

