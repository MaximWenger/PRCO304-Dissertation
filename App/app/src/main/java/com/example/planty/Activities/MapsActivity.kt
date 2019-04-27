package com.example.planty.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.ActivityCompat
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
    private lateinit var locationManager: LocationManager
    private var hasGps = false
    private var hasNetwork = false
    private  var locationGps : Location? = null
    private  var locationNetwork : Location? = null

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

    @SuppressLint("MissingPermission")
    private fun getLocation(){
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (hasGps || hasNetwork){
            if (hasGps) {
                Log.d("MapsActivity", "HasGPS")
              //  locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0F, object: LocationListener()) //Duration of gps update & Minimum distance
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,0F, object : LocationListener {
                    override fun onLocationChanged(location: Location?) {
                        if (location != null){
                            locationGps = location
                        }
                    }

                    override fun onProviderDisabled(provider: String?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onProviderEnabled(provider: String?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }
                })
            val localGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (localGpsLocation != null){
                    locationGps = localGpsLocation
                }
            }
            if (hasNetwork) {
                Log.d("MapsActivity", "HasNetwork")
                //  locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0F, object: LocationListener()) //Duration of gps update & Minimum distance
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,5000,0F, object : LocationListener {
                    override fun onLocationChanged(location: Location?) {
                        if (location != null){
                            locationNetwork = location
                          }
                    }

                    override fun onProviderDisabled(provider: String?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onProviderEnabled(provider: String?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }
                })
                val localNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (localNetworkLocation != null){
                    locationNetwork = localNetworkLocation
                }
        }
        if (locationGps != null && locationNetwork != null){
            if (locationGps!!.accuracy > locationNetwork!!.accuracy){
                Log.d("MapsActivity","Latitude NETWORK= ${locationNetwork!!.latitude}, Longitude = ${locationNetwork!!.longitude}")
            }
            else{
                Log.d("MapsActivity","Latitude GPS= ${locationGps!!.latitude}, Longitude = ${locationGps!!.longitude}")
            }
        }
            if (locationNetwork != null){
                Log.d("MapsActivity","Latitude NETWORK= ${locationNetwork!!.latitude}, Longitude = ${locationNetwork!!.longitude}")
            }
            if (locationGps != null){
                val currentLatLng = LatLng(locationGps!!.latitude, locationGps!!.longitude)
                mMap.isMyLocationEnabled = true
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16.0f)) //Moves camera to this point

               // mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng))
                //now to zoom
                Log.d("MapsActivity","Latitude GPS= ${locationGps!!.latitude}, Longitude = ${locationGps!!.longitude}")
            }
        }
        else{
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) //If location settings are not accetped, redirect user to turn on location settings
        }
    }

    private fun getSpecPlants(plantName: String, baseIdent: String){ //Used to match the PlantName to any plant names within the database
        var matchKey = ""
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
                        getSpecBranchID(baseIdent, matchKey)
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

    private fun getSpecBranchID(baseIdent: String, matchKey: String){//Gets all branchIDs for the specific plant
      var allIds = mutableListOf<String>()
        val ref = FirebaseDatabase.getInstance().getReference("/specPlants/${baseIdent}/${matchKey}")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach{
                    var branchID = it.value.toString()
                    Log.d("MapsActivity", "GOT BRANCH VALUE ${branchID}")
                    allIds.add(branchID)
                }
                getSpecMarkers(allIds) //Must be transfered in an array, as the overRide methods can become stacked with extra data within a singlke variable
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun getSpecMarkers(allIds: MutableList<String>) { //Display each marker for the specific plant
        Log.d("MapsActivity","getSpecMarkers BranchID ${allIds.size}}")

        for (branchID in allIds){
            var path = basePath + branchID
            val ref = FirebaseDatabase.getInstance().getReference(path)
            ref.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(p0: DataSnapshot) {
                    p0.children.forEach{
                        Log.d("MapsActivity"," Got to getSpecMarkers Value = ${p0}")
                        val currentBranch = p0.getValue(Branch::class.java)
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

        getLocation()

        // Add a marker in Sydney and move the camera
     //   val sydney = LatLng(50.375356, -4.140875)
       // mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydneyyyy"))

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))///////////////////////////////////////////////




        //getAllMarkers()//for testing, stopped this

    }





    fun getAllMarkers(){
        val ref = FirebaseDatabase.getInstance().getReference(basePath)
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
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(branch))
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