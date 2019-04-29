package com.example.planty.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.example.planty.Classes.CloudVisionData
import com.example.planty.Classes.GPSLocation
import com.example.planty.R
import com.example.planty.Objects.Branch
import com.example.planty.Objects.Identified

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
import kotlinx.android.synthetic.main.activity_maps.*
import java.lang.Exception

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager

    private var basePath = "/branches/"
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
        populatePrevIdentified()
    }

    private fun populatePrevIdentified(){
        getLatestIdentifications()
        //find latest identifciations
        //Display those details
    }

    private fun getLatestIdentifications(){ //Unable to put into other class, due to the override functions, and delay on waiting for download
        Log.d("MapsActivity", "GOT TO getLatestIdentifications")
        val currentIdUUID = getIdentifiedPlantUUID()
        var identifications: MutableList<Identified> = mutableListOf<Identified>()
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/identifiedPlants/${uid}")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    if (it.key.toString() != currentIdUUID) { //Checking that the current identified plant is not saved to the array
                        val userIdentified = (it.getValue(Identified::class.java))
                        Log.d("MapsActivity","User ${userIdentified!!.identifiedImage}")
                        identifications.add(userIdentified)//Adds the identifications to the Identifications array
                    }
                }
                displayIdentifications(identifications)
                Log.d("MapsActivity","111Running through SIZE= ${identifications?.size}")
                //sortList()//Going to display first, then sort
            }
            //Once all the identifications have been added to the list, sort them in date order
            override fun onCancelled(p0: DatabaseError) {
                Log.d("MapsActivity", "getLatestIdentifications Error = ${p0.message}")
            }
        })
    }

    private fun displayIdentifications(identifications: MutableList<Identified>){//used to populate the previously identified details
        Log.d("MapsActivity", "GOT TO displayIdentifications Size = ${identifications.size}, amended size = ${identifications.size-1}")
        var size = identifications.size
        if (size-1 >= 3) {
            displayId0(identifications[0])//Display relivent fields
            displayId1(identifications[1])
            displayId2(identifications[2])
            displayId3(identifications[3])
        }else if (size-1 >= 2){
            displayId0(identifications[0])
            displayId1(identifications[1])
            displayId2(identifications[2])
            hideDisplayID3()
        }else if (size-1 >= 1){
            displayId0(identifications[0])
            displayId1(identifications[1])
            hideDisplayID2()
            hideDisplayID3()
        }else if (size > 0 ){
            displayId0(identifications[0])
            hideDisplayID1()
            hideDisplayID2()
            hideDisplayID3()
        }
        else{
            hideDisplayID0()//Hide all fields
            hideDisplayID1()
            hideDisplayID2()
            hideDisplayID3()
        }

    }
    private fun displayId0(ident: Identified){
        Log.d("MapsActivity", "GOT TO displayId1")
        MapsActivity_PrevID0_Name.text = ident.plantName
        //Display image
    }
    private fun displayId1(ident: Identified){
        Log.d("MapsActivity", "GOT TO displayId1")
        MapsActivity_PrevID1_Name.text = ident.plantName
        //Display image
    }
    private fun displayId2(ident: Identified){
        Log.d("MapsActivity", "GOT TO displayId1")
        MapsActivity_PrevID2_Name.text = ident.plantName
        //Display image
    }
    private fun displayId3(ident: Identified){
        Log.d("MapsActivity", "GOT TO displayId1")
        MapsActivity_PrevID3_Name.text = ident.plantName
        //Display image
    }

    private fun hideDisplayID0(){
        MapsActivity_PrevID0_Name.visibility = View.INVISIBLE
    }
    private fun hideDisplayID1(){
        MapsActivity_PrevID1_Name.visibility = View.INVISIBLE
    }
    private fun hideDisplayID2(){
        MapsActivity_PrevID2_Name.visibility = View.INVISIBLE
    }
    private fun hideDisplayID3(){
        MapsActivity_PrevID3_Name.visibility = View.INVISIBLE
    }

    private fun populateSpecificMarkers(){ //Popualte the markers for the branches (individual branches or all branches)
        var plantName = getPlantName()
        var baseID = getBaseIdent()
        if (plantName.isNotEmpty()){ //If there is a plantName Display all businesses which sell this plant (If any)
            var baseId = getBaseIdent()//Return baseId
            getSpecPlants(plantName, baseId) //Display the markers for branches which sell the plant
        }
        else if (baseID.isNotEmpty()){
            var path = "/basePlants/" + baseID
            getSpecBranchIDs(path)
        }
        else {
            displayAllMarkers()
        }
    }

    @SuppressLint("MissingPermission")
    private fun populateGPS(){//Populate the user GPS locations
  try {
      locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
      var currentGPS = GPSLocation().getLocation(locationManager)
      mMap.isMyLocationEnabled = true
      updateLocation(currentGPS)
  }
  catch(e: Exception){//If there's an issue with the currentGPS
      Log.d("MapsActivity", "populateGPS Error ${e.message}")
      startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) //If location settings are not accepted, redirect user to turn on location settings
         }
    }

    private fun updateLocation(gps: LatLng){ //Used to update the user Map location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gps, 8.0f)) //Moves camera to this point
    }

    private fun processMatchData(lowerCaseKey: String, lowerCasePlantName: String): Boolean {
        if (lowerCaseKey.contains(lowerCasePlantName) || lowerCasePlantName.contains(lowerCaseKey)) {
            Log.d("MapsActivity", "processMatchData CORRECT MATCH KEY CONFIRM ")
            return true
        }
       return false
    }

    private fun getSpecPlants(plantName: String, baseIdent: String){ //Used to match the PlantName to any plant names within the database
        var matchKey = ""
        val specPlants = "specPlants"
        var baseIdentLibrarySize = CloudVisionData().getBaseIdentLibrary().size
        var path = ""
        val lowerCasePlantName = plantName.toLowerCase()
        val ref = FirebaseDatabase.getInstance().getReference("/specPlants/${baseIdent}")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot){
                p0.children.forEach{
                    val key = it.key.toString() //Get current key
                    if (processMatchData(key.toLowerCase(), lowerCasePlantName)){
                        matchKey = key//Get the PlantName from the database (Gets formatting)
                        path = "/" + specPlants + "/" + baseIdent + "/" + matchKey
                        getSpecBranchIDs(path)//Calls displaySpecMarkers to display the markers on the map
                    }
                }
                if (matchKey == "" && attemptCounter == 0){//If the plantID has not been found in the database, resort to checking every baseId for the plantName
                    attemptAllBaseIdent(plantName)//Must be here to deal with download latency, if placed outside of loop, will always return as default
                    attemptCounter++
                }
                attemptCounter++
                if (matchKey == "" && attemptCounter > baseIdentLibrarySize ){//If the key hasnt been found & every baseID has been checked, display all branches within the baseID
                    populateSingleBaseIdent() //Populate map with a marker for every branch within baseIdent
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                Log.d("MapsActivity", "getSpecPlants Error ${p0.message}")
                displayAllMarkers()//If fails, try to display all markers
            }
        })
    }

    private fun populateSingleBaseIdent(){ //Populates map with a marker for every branch within origBaseIdent
        val basePlants = "basePlants"
        var origBaseIdent = getBaseIdent()
        var path = "/" + basePlants + "/" + origBaseIdent
        Log.d("MapsActivity","ELSE. BaseIdent = ${origBaseIdent}  ")
        getSpecBranchIDs(path)
    }

    private fun getSpecBranchIDs(path: String){//Gets all branchIDs for the specific plant into an array, then calls displaySpecMarkers to display the markers
      var allIds = mutableListOf<String>()
        val ref = FirebaseDatabase.getInstance().getReference(path)
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach{
                    var branchID = it.value.toString()//Get current branchID
                    allIds.add(branchID)//Add the current branchID to the array
                    Log.d("MapsActivity", "getSpecBranchIDs  branch added ${branchID}")
                }
                displaySpecMarkers(allIds) //Must be transfered as an array, as the overRide methods can become stacked with extra data within a single variable
            }
            override fun onCancelled(p0: DatabaseError) {
                Log.d("MapsActivity", "getSpecBranchIDs Error ${p0.message}")
                displayAllMarkers()//If fails, try to display all markers
            }
        })
    }

    private fun displaySpecMarkers(allIds: MutableList<String>) { //Display each marker for the specific plant, using an array of String to hold each branchID for Firebase
        for (branchID in allIds){
            var path = basePath + branchID
            val ref = FirebaseDatabase.getInstance().getReference(path)
            ref.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(p0: DataSnapshot) {
                    p0.children.forEach{
                        val currentBranch = p0.getValue(Branch::class.java)
                        populateSingleBranchMarker(currentBranch)//Populate a single marker
                    }
                }
                override fun onCancelled(p0: DatabaseError) {
                    Log.d("MapsActivity", "displaySpecMarkers Error ${p0.message}")
                    displayAllMarkers()//If fails, try to display all markers
                }
            })
        }
    }

    private fun populateSingleBranchMarker(currentBranch: Branch?){//Populates a single marker, of type Branch
        val longitude = currentBranch?.longitude!!.toDouble()
        val latitude = currentBranch?.latitude!!.toDouble()
        val branchName = currentBranch?.name.toString()
        val branch = LatLng(latitude, longitude)
        mMap.addMarker(MarkerOptions().position(branch).title("${branchName}"))
        Log.d("MapsActivity", "displaySpecMarkers Marker added ${branchName}")
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

    private fun getBaseIdent(): String { //Returns baseID (If the plant has been identified)
        var baseIdent = ""
        try {
            baseIdent = intent.getStringExtra("baseIdent")

        }catch (e: Exception){
            Log.d("MapsActivity", "getBaseIdent Error = ${e.message}")
        }
        return baseIdent
    }

    private fun getPlantName(): String {//Returns plantName (If plant has been identified)
        var plantName = ""
        try {
            plantName = intent.getStringExtra("plantName")

        }catch (e: Exception){
            Log.d("MapsActivity", "getPlantName Error = ${e.message}")
        }
        return plantName
    }

    private fun getIdentifiedPlantUUID(): String {//Returns IdentifiedUUID (If plant has been identified) To be used to populate the previous identifications
        var identifiedID = ""
        try {
            identifiedID = intent.getStringExtra("identifiedPlantUUID")

        }catch (e: Exception){
            Log.d("MapsActivity", "getIdentifiedPlantUUID Error = ${e.message}")
        }
        return identifiedID
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

       // GPSLocation().getLocation() //Gets current location
        populateGPS()

        // Add a marker in Sydney and move the camera
     //   val sydney = LatLng(50.375356, -4.140875)
       // mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydneyyyy"))

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))///////////////////////////////////////////////




        //displayAllMarkers()//for testing, stopped this

    }





    private fun displayAllMarkers(){//Display every marker from the database
        val ref = FirebaseDatabase.getInstance().getReference(basePath)
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach{
                    val currentBranch = it.getValue(Branch::class.java)
                    val longitude = currentBranch?.longitude!!.toDouble()
                    val latitude = currentBranch?.latitude!!.toDouble()
                    val branchName = currentBranch?.name.toString()
                    val branch = LatLng(latitude, longitude)
                    mMap.addMarker(MarkerOptions().position(branch).title("${branchName}"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(branch))
                     Log.d("MapsActivity","ADDED MARK + X = ${longitude},  Branch = ${branchName}")
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                Log.d("MapsActivity","displayAllMarkers Error = ${p0.message}")
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
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
            startActivity(intent)
        }
    }

    private fun navToProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java) //Populate intent with new activity class
        //  intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
        startActivity(intent) //Change to new class
    }
}