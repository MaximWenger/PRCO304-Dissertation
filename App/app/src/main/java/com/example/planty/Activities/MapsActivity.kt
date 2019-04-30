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
import com.example.planty.Objects.UserImage

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
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_maps.*
import java.lang.Exception

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager

    private var basePath = "/branches/"
    private var attemptCounter = 0
    var keys: MutableList<String> = mutableListOf<String>()//Used to store the keys for previouslyIdentified plants



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
        getLatestIdentifications()

        MapsActivity_PrevID0_Btn.setOnClickListener{
            Log.d("MapsActivity","BUTTON PRESSED ${keys[0]}")
            mMap.clear()//Clear the markers from the map
            findSpecificIdentified(keys[0])//Find and display the markers for this specific plant
        }
        MapsActivity_PrevID1_Btn.setOnClickListener{
            Log.d("MapsActivity","BUTTON PRESSED ${keys[1]}")
            mMap.clear()
            findSpecificIdentified(keys[1])
        }
        MapsActivity_PrevID2_Btn.setOnClickListener{
            mMap.clear()
            findSpecificIdentified(keys[2])
        }
        MapsActivity_PrevID3_Btn.setOnClickListener{
            mMap.clear()
            findSpecificIdentified(keys[3])
        }

    }

    /** Calls displayIdentifications(identifications) passing MutableList of identification objects
     *  Creates a MutableList called identifications, containing all of the identifications by the user,
     *  populated via Firebase.
     *
     *  */
    private fun getLatestIdentifications() { //Unable to put into other class, due to the override functions, and delay on waiting for download
        val currentIdUUID = getIdentifiedPlantUUID()
        var identifications: MutableList<Identified> = mutableListOf<Identified>()
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/identifiedPlants/${uid}")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    if (it.key.toString() != currentIdUUID) { //Checking that the current identified plant is not saved to the array
                        val userIdentified = (it.getValue(Identified::class.java))
                        Log.d("MapsActivity", "User ${userIdentified!!.identifiedImage}")
                        identifications.add(userIdentified)//Adds the identifications to the Identifications array
                        keys.add(it.key.toString())
                    }
                }
                displayIdentifications(identifications)
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("MapsActivity", "getLatestIdentifications Error = ${p0.message}")
            }
        })
    }

    /**Populates and Hides previous identifications depending if the data exists
     * Calls displayIDX to populate the previous identifications and
     * hides the identifications which are not to be populated
     * @param identifications is the populated MutableList containing Identified objects
     */
    private fun displayIdentifications(identifications: MutableList<Identified>) {//used to populate the previously identified details
        Log.d("MapsActivity"," displayIdentifications"
        )
        var size = identifications.size
        if (size - 1 >= 3) {
            displayId0(identifications[0])//Display relevant fields
            displayId1(identifications[1])
            displayId2(identifications[2])
            displayId3(identifications[3])
        } else if (size - 1 >= 2) {
            displayId0(identifications[0])
            displayId1(identifications[1])
            displayId2(identifications[2])
            hideDisplayID3()
        } else if (size - 1 >= 1) {
            displayId0(identifications[0])
            displayId1(identifications[1])
            hideDisplayID2()
            hideDisplayID3()
        } else if (size > 0) {
            displayId0(identifications[0])
            hideDisplayID1()
            hideDisplayID2()
            hideDisplayID3()
        } else {
            hideDisplayID0()//Hide all fields
            hideDisplayID1()
            hideDisplayID2()
            hideDisplayID3()
        }

    }

    /**Calls displayIdentImage using specific PlantName & baseID from ident
     * @param ident Holds a single Identified object, used to get PlantName & baseID
     */
    private fun displayId0(ident: Identified) {
        MapsActivity_PrevID0_Name.text = ident.plantName
        MapsActivity_PrevID0_Desc.text = ident.baseID
        displayIdentImage(ident.identifiedImage, 0)
    }

    /**Calls displayIdentImage using specific PlantName & baseID from ident
     * @param ident Holds a single Identified object, used to get PlantName & baseID
     */
    private fun displayId1(ident: Identified) {
        Log.d("MapsActivity", "GOT TO displayId1")
        MapsActivity_PrevID1_Name.text = ident.plantName
        MapsActivity_PrevID1_Desc.text = ident.baseID
        displayIdentImage(ident.identifiedImage, 1)

    }

    /**Calls displayIdentImage using specific PlantName & baseID from ident
     * @param ident Holds a single Identified object, used to get PlantName & baseID
     */
    private fun displayId2(ident: Identified) {
        Log.d("MapsActivity", "GOT TO displayId1")
        MapsActivity_PrevID2_Name.text = ident.plantName
        MapsActivity_PrevID2_Desc.text = ident.baseID
        displayIdentImage(ident.identifiedImage, 2)

    }

    /**Calls displayIdentImage using specific PlantName & baseID from ident
     * @param ident Holds a single Identified object, used to get PlantName & baseID
     */
    private fun displayId3(ident: Identified) {
        Log.d("MapsActivity", "GOT TO displayId1")
        MapsActivity_PrevID3_Name.text = ident.plantName
        MapsActivity_PrevID3_Desc.text = ident.baseID
        displayIdentImage(ident.identifiedImage, 3)

    }

    /**Populates previoulsy identified Plant images via Firebase
     * @param imageUUID Unique ID associated to each saved image within Firebase
     * @param imageNumb Denotes the specific previous identified field to populate 0-3
     */
    private fun displayIdentImage(imageUUID: String, imageNumb: Int){
        val uid = FirebaseAuth.getInstance().uid
        var imgLoc: String? =""
        val ref = FirebaseDatabase.getInstance().getReference("/userImages/$uid/$imageUUID")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    val currentImage = p0.getValue(UserImage::class.java)
                     imgLoc = currentImage?.imageLoc
                    populateImgLoc(imgLoc, imageNumb)
                    Log.d("MapsActivity", "PRINT IMAGELOC ${imgLoc}")
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                Log.d("MapsActivity", "displayIdentImage Error = ${p0.message}")
            }
        })

//return imgLoc
    }

    /**Populates the previoulsy identified plant images
     * @param imgLoc Image location, a web address linking directly to the saved image on Firebase
     * @param imageNumb Denotes the specific previous identified field to populate 0-3
     *
     */
    private fun populateImgLoc(imgLoc: String?, imageNumb: Int){
    when(imageNumb){
        0 -> Picasso.get().load(imgLoc).rotate(90f).resize(150,200).into(MapsActivity_PrevID0_Img)
        1 -> Picasso.get().load(imgLoc).rotate(90f).resize(150,200).into(MapsActivity_PrevID1_Img)
        2 -> Picasso.get().load(imgLoc).rotate(90f).resize(150,200).into(MapsActivity_PrevID2_Img)
        3 -> Picasso.get().load(imgLoc).rotate(90f).resize(150,200).into(MapsActivity_PrevID3_Img)
    }
}

    /**Hides the relevant previously identified field, if there is not enough data to populate it
     */
    private fun hideDisplayID0(){//Hides unused previously identified fields
        MapsActivity_PrevID0_Name.visibility = View.INVISIBLE
        MapsActivity_PrevID0_Desc.visibility = View.INVISIBLE
        MapsActivity_PrevID0_Btn.visibility = View.INVISIBLE
        MapsActivity_PrevID0_Img.visibility = View.INVISIBLE
    }

    /**Hides the relevant previously identified field, if there is not enough data to populate it
     */
    private fun hideDisplayID1(){//Hides unused previously identified fields
        MapsActivity_PrevID1_Name.visibility = View.INVISIBLE
        MapsActivity_PrevID1_Desc.visibility = View.INVISIBLE
        MapsActivity_PrevID1_Btn.visibility = View.INVISIBLE
        MapsActivity_PrevID1_Img.visibility = View.INVISIBLE
    }

    /**Hides the relevant previously identified field, if there is not enough data to populate it
     */
    private fun hideDisplayID2(){//Hides unused previously identified fields
        MapsActivity_PrevID2_Name.visibility = View.INVISIBLE
        MapsActivity_PrevID2_Desc.visibility = View.INVISIBLE
        MapsActivity_PrevID2_Btn.visibility = View.INVISIBLE
        MapsActivity_PrevID2_Img.visibility = View.INVISIBLE
    }

    /**Hides the relevant previously identified field, if there is not enough data to populate it
     */
    private fun hideDisplayID3(){//Hides unused previously identified fields
        MapsActivity_PrevID3_Name.visibility = View.INVISIBLE
        MapsActivity_PrevID3_Desc.visibility = View.INVISIBLE
        MapsActivity_PrevID3_Btn.visibility = View.INVISIBLE
        MapsActivity_PrevID3_Img.visibility = View.INVISIBLE
    }

    /**Finds specfic plantName and baseID a single plant Identification, Calls getSpecPlants
     * @param key Unique ID, used to identify a single plant Identification within Firebase
     */
    private fun findSpecificIdentified(key: String) {//Used to retreve the plantName and baseID to then be used in getSpecPlants
        val ref = FirebaseDatabase.getInstance().getReference("/identifiedPlants/${FirebaseAuth.getInstance().uid}/${key}")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot){
                p0.children.forEach{
                val currentIdent = p0.getValue(Identified::class.java)
                    var plantName = currentIdent!!.plantName
                    var baseID = currentIdent!!.baseID
                    getSpecPlants(plantName, baseID) //Used to populate the actual markers
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                Log.d("MapsActivity", "findSpecificIdentified Error ${p0.message}")
            }
        })
    }

    /**Determines which variation of markers can be displayed on the map
     */
    private fun populateSpecificMarkers(){ //Populate the markers for the branches (individual branches or all branches)
        var plantName = getPlantName()
        var baseID = getBaseIdent()
        if (plantName.isNotEmpty()){ //If there is a plantName Display all businesses which sell this plant (If any)
            var baseId = getBaseIdent()//Return baseId
            getSpecPlants(plantName, baseId) //Display the markers for branches which sell the plant
        }
        else if (baseID.isNotEmpty()){//Display all markers for the specific baseID if there is a baseID
            var path = "/basePlants/" + baseID
            getSpecBranchIDs(path)
        }
        else {
            displayAllMarkers()//Display every marker
        }
    }

    /**Finds and updates the GPS location of the device
     *
     */
    @SuppressLint("MissingPermission")
    private fun populateGPS(){//Populate the user GPS locations
  try {
      locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
      var currentGPS = GPSLocation().getLocation(locationManager)
      mMap.isMyLocationEnabled = true
      updateMapLocation(currentGPS)
  }
  catch(e: Exception){//If there's an issue with the currentGPS
      Log.d("MapsActivity", "populateGPS Error ${e.message}")
      startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) //If location settings are not accepted, redirect user to turn on location settings
         }
    }

    /**Moves the Map to device location and correct zoom
     */
    private fun updateMapLocation(gps: LatLng){ //Used to update the user Map location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gps, 8.0f)) //Moves camera to this point
    }

    /**Compares the parameters to see if they contain one or another
     * @param lowerCaseKey Plant Names from Firebase
     * @param lowerCasePlantName latest identified plant name
     * @return Boolean
     */
    private fun processMatchData(lowerCaseKey: String, lowerCasePlantName: String): Boolean {
        if (lowerCaseKey.contains(lowerCasePlantName) || lowerCasePlantName.contains(lowerCaseKey)) {
            Log.d("MapsActivity", "processMatchData CORRECT MATCH KEY CONFIRM ")
            return true
        }
       return false
    }

    /**Searches Firebase specPlants/baseIdent for a specific plantName match
     * If the specific plantName is matched, getSpecBranchIDs() is then called with the correct path to the match
     * else, all markers are displayed via displayAllMarkers() as the baseID does not exist
     * @param plantName Identified plant Name
     * @param baseIdent baseId of the identified plant
     * This method is used, as to avoid any case-sensitivity within Firebase or the Identifications
     */
    private fun getSpecPlants(plantName: String, baseIdent: String){ //Used to match the PlantName or baseID to any plant names or baseID within the database
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
                        getSpecBranchIDs(path)//Calls displaySpecBranchMarkers to display the markers on the map
                    }
                }
                if (matchKey == "" && attemptCounter == 0){//If the plantID has not been found in the database, resort to checking every baseId for the plantName
                    attemptAllBaseIdent(plantName)//Must be here to deal with download latency, if placed outside of loop, will always return as default
                    attemptCounter++//Attempt counter ensures that the loop only continues through every baseID
                }
                attemptCounter++//Attempt counter ensures that the loop only continues through every baseID
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

    /**Find the Firebase path for the base Identification and calls getSpecBranchIDs
     */
    private fun populateSingleBaseIdent(){ //Calls getSpecBranchIDs with a path for the identified Branch
        val basePlants = "basePlants"
        var origBaseIdent = getBaseIdent()
        var path = "/" + basePlants + "/" + origBaseIdent
        getSpecBranchIDs(path)
    }

    /**Produces List containing all branch ids from path, passing the list to displaySpecBranchMarkers()
     * @param path Holds Firebase path to the correct baseID
     * if any issues arise getting the branch objects, displayAllMarkers() is called
     */
    private fun getSpecBranchIDs(path: String){//Gets all branchIDs for the specific plant into an array, then calls displaySpecBranchMarkers to display the markers
      var allIds = mutableListOf<String>()
        val ref = FirebaseDatabase.getInstance().getReference(path)
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach{
                    var branchID = it.value.toString()//Get current branchID
                    allIds.add(branchID)//Add the current branchID to the array
                    Log.d("MapsActivity", "getSpecBranchIDs  branch added ${branchID}")
                }
                displaySpecBranchMarkers(allIds) //Must be transfered as an array, as the overRide methods can become stacked with extra data within a single variable
            }
            override fun onCancelled(p0: DatabaseError) {
                Log.d("MapsActivity", "getSpecBranchIDs Error ${p0.message}")
                displayAllMarkers()//If fails, try to display all markers
            }
        })
    }

    /**Retrieves branch Objects from Firebase and calls populateSingleBranchMarker() to display each branch
     * Iterates through all branchId objects found in Firebase, using the allIds param.
     * @param allIds list of all branchIDs to be displayed on the map
     */
    private fun displaySpecBranchMarkers(allIds: MutableList<String>) { //Display each marker for the specific plant, using an array of String to hold each branchID for Firebase
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
                    Log.d("MapsActivity", "displaySpecBranchMarkers Error ${p0.message}")
                    displayAllMarkers()//If fails, try to display all markers
                }
            })
        }
    }

    /**Populates a single Branch marker on the map, using the given param
     * @param currentBranch single Branch Object
     */
    private fun populateSingleBranchMarker(currentBranch: Branch?){//Populates a single marker, of type Branch
        val longitude = currentBranch?.longitude!!.toDouble()
        val latitude = currentBranch?.latitude!!.toDouble()
        val branchName = currentBranch?.name.toString()
        val branch = LatLng(latitude, longitude)
        mMap.addMarker(MarkerOptions().position(branch).title("${branchName}"))
        Log.d("MapsActivity", "populateSingleBranchMarker Marker added ${branchName}")
    }

    /**Loops through all baseIDs, calling getSpecPlants() for each baseID type
     * Used by getSpecPlants() to loop search every baseID, thus the entire database
     * @param plantName Identified PlantName
     */
    private fun attemptAllBaseIdent(plantName: String){//Used to iterate through the every baseIdent within database to find specific plant
        val amendedBaseIdentLibrary = removeExistingBaseIdent()//Remove the existing (Already checked baseID)
        for (ident in amendedBaseIdentLibrary){ //Iterates through every baseIdent if needed to try and find a correct match
                getSpecPlants(plantName, ident)
        }
    }

    /**Returns a List of baseIds, which does not contain the current baseID
     * If getBaseIdent() returns null, method will return list of all baseIDS
     * @return List of baseIds
     */
    private fun removeExistingBaseIdent(): MutableList<String> {//Removes the existing baseId from the list, which is then checked for the correct plant type
        var immutableBaseIdentLibrary = CloudVisionData().getBaseIdentLibrary()
        var mutableBaseIdentLibrary = immutableBaseIdentLibrary.toMutableList() //Convert immutableList to mutable so the existing baseID can be removed
        var baseIdent = getBaseIdent()
        if(baseIdent != null) {//If the baseIdent is found, remove it
            mutableBaseIdentLibrary.remove(baseIdent)
            Log.d(
                "MapsActivity",
                "removeExistingBaseIdent before = ${immutableBaseIdentLibrary.size}, After = ${mutableBaseIdentLibrary.size}"
            )
            return mutableBaseIdentLibrary //Return amended baseIdentList
        }
        return mutableBaseIdentLibrary//Return original baseIdent List
    }

    /**Returns the baseIdent (If it's been populated)
     * @return the baseIdent
     */
    private fun getBaseIdent(): String { //Returns baseID (If the plant has been identified)
        var baseIdent = ""
        try {
            baseIdent = intent.getStringExtra("baseIdent")

        }catch (e: Exception){
            Log.d("MapsActivity", "getBaseIdent Error = ${e.message}")
        }
        return baseIdent
    }

    /**Returns the plantName (If it's been populated)
     * @return the plantName
     */
    private fun getPlantName(): String {//Returns plantName (If plant has been identified)
        var plantName = ""
        try {
            plantName = intent.getStringExtra("plantName")

        }catch (e: Exception){
            Log.d("MapsActivity", "getPlantName Error = ${e.message}")
        }
        return plantName
    }

    /**Returns the identifiedPlantUUID (If it's been populated)
     * @return the identifiedPlantUUID
     */
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
        populateGPS()
    }

    /**Displays every branch on the map
     * Displays every branch from branches in Firebase
     */
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

    /**Sings the user out and returns to the login screen
     *
     */
    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, RegisterActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    /**Changes activity to IdentifyActivity
     *
     */
    private fun navToIdentifyActivity() {
        val intent = Intent(this, IdentifyActivity::class.java) //Populate intent with new activity class
        //  intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
        startActivity(intent) //Change to new class
    }

    /**Checks the user is logged in, returns to Login if not logged in
     *
     */
    private fun verifyLoggedIn() { //Check if the User is already logged in, if not, return User to registerActivity
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
            startActivity(intent)
        }
    }

    /**Changes activity to ProfileActivity
     *
     */
    private fun navToProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java) //Populate intent with new activity class
        //  intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
        startActivity(intent) //Change to new class
    }
}