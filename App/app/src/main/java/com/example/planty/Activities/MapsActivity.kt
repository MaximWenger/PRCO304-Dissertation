package com.example.planty.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.planty.Classes.CloudVisionData
import com.example.planty.R
import com.example.planty.Objects.Branch

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var matchKey = ""
    private var attemptCounter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        supportActionBar?.title = "Planty  |  Find Plants"
        verifyLoggedIn()//check the User is logged in

        populateSpecificMarkers()
    }

    private fun populateSpecificMarkers(){
    //Go into database, find the properties
        //get specific
        var plantName = getPlantName()
        if (plantName.isNotEmpty()){
            //Display all businesses which sell this plant (If any)
          //  getSpecPlants(plantName)

            var baseId = getBaseIdent()
            getSpecPlants(plantName, baseId)

           //
        }
        else{
            var baseIdent =  getBaseIdent()
        }
        //if no specific, get base
    }

    private fun getSpecPlants(plantName: String, baseIdent: String){ //Used to match the PlantName to any plant names within the database
        val lowerCasePlantName = plantName.toLowerCase()
        val ref = FirebaseDatabase.getInstance().getReference("/specPlants/${baseIdent}")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot){
                p0.children.forEach{
                    val key = it.key.toString() //Get current key
                    val lowerCaseKey = key.toLowerCase()
                    if (lowerCaseKey.contains(lowerCasePlantName) || lowerCasePlantName.contains(lowerCaseKey)){
                        Log.d("MapsActivity","CORRECT MATCH KEY CONFIRM ")
                        matchKey = key
                        //Now display the specific points
                    }
                }
                if (matchKey == "" && attemptCounter == 0){//If the plantID has not been found in the database, resort to base ID
                    attemptAllBaseIdent(plantName)//Must be here to deal with download latency, if placed outside of loop, will always return as default
                    attemptCounter++
                }
                else{
                    //Display BASEID Pins
                }

            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                //DISPLAY ALL PINS (DEFAULT)
            }

        })
    }

    private fun attemptAllBaseIdent(plantName: String){//Used to iterate through the every baseIdent within database to find specific plant
        val amendedBaseIdentLibrary = removeExistingBaseIdent()//Remove the existing (Already checked baseID)
        for (ident in amendedBaseIdentLibrary){ //Iterates through every baseIdent if needed to try and find a correct match
                getSpecPlants(plantName, ident)
        }
    }

    private fun removeExistingBaseIdent(): MutableList<String> {//Removes the existing baseId from the list, which is then checked for the correct plant type
        var immutableBaseIdentLibrary = CloudVisionData().getBaseIdentLibrary()
        var mutableBaseIdentLibrary = immutableBaseIdentLibrary.toMutableList() //Convert immutableList to mutable so the existing baseID can be removed
        var baseIdent = getBaseIdent()
         mutableBaseIdentLibrary.remove(baseIdent)
        Log.d("MapsActivity","removeExistingBaseIdent before = ${immutableBaseIdentLibrary.size}, After = ${mutableBaseIdentLibrary.size}")
        return mutableBaseIdentLibrary

    }

    private fun getBaseIdent(): String { //Used to display marker points if the specific plant name is not found
        var baseIdent = ""
        try {
            baseIdent = intent.getStringExtra("baseIdent")

        }catch (e: Exception){
            Log.d("MapsActivity", "getBaseIdent Error = ${e.message}")
        }
        return baseIdent
    }

    private fun getPlantName(): String {
        var plantName = ""
        try {
            plantName = intent.getStringExtra("plantName")

        }catch (e: Exception){
            Log.d("MapsActivity", "getPlantName Error = ${e.message}")
        }
        return plantName
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the User will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the User has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(50.375356, -4.140875)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydneyyyy"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        getAllMarkers(googleMap)

    }





    fun getAllMarkers(googleMap: GoogleMap){
        mMap = googleMap
        val ref = FirebaseDatabase.getInstance().getReference("/branches/")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach{
              //  Log.d("MapsActivity",it.toString())
                //  Log.d("MapsActivity","BRANCH == ${currentBranch?.longitude.toString()}")
                    val currentBranch = it.getValue(Branch::class.java)
                    val longitude = currentBranch?.longitude!!.toDouble()
                    val latitude = currentBranch?.latitude!!.toDouble()
                    val branchName = currentBranch?.name.toString()
                    val branch = LatLng(latitude, longitude)
                    mMap.addMarker(MarkerOptions().position(branch).title("${branchName}"))
                     Log.d("MapsActivity","ADDED MARK + X = ${longitude},  Branch = ${branchName}")

                }
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean { //Create the menu
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean { //When an option from the menu is clicked
        when (item?.itemId) { //Switch statement
            R.id.nav_Profile -> {
                navToProfileActivity() //Go to ProfileActivity
            }
            R.id.nav_Identify -> {
                navToIdentifyActivity() //Go to IdentifyActivity
            }
            R.id.nav_Find -> {
                return super.onOptionsItemSelected(item)  //Return as already within MapsActivity
            }
            R.id.nav_Sign_Out -> {
                signOut() //Signs the User out and returns to RegisterActivity
            }
            R.id.nav_Contact -> { //DOES NOTHING RIGHT NOW
                return super.onOptionsItemSelected(item)  //
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, RegisterActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun navToIdentifyActivity() {
        val intent = Intent(this, IdentifyActivity::class.java) //Populate intent with new activity class
        //  intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
        startActivity(intent) //Change to new class
    }

    private fun verifyLoggedIn() { //Check if the User is already logged in, if not, return User to registerActivity
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
            startActivity(intent)
        }
    }

    private fun navToProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java) //Populate intent with new activity class
        //  intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
        startActivity(intent) //Change to new class
    }
}