package com.example.planty.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.planty.R
import com.google.firebase.auth.FirebaseAuth
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_identified.*
import kotlinx.android.synthetic.main.identifiedrow.*
import java.lang.Exception
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.ArrayList

class IdentifiedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identified)

        supportActionBar?.title ="Identified" //change activity Title

        verifyLoggedIn()
try {
    val identifiedString = intent.getStringArrayListExtra("identifications")
    populateIDRows(identifiedString)
}
catch (e: Exception){
    Log.d("IdentifiedActivity", "Create Error = ${e.message}")
}
    }

    private fun populateIDRows(identifiedString: ArrayList<String>){//Populate the text fields
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

    private fun hideIdent1(){ //Hide the textviews and button
      identified_button1.visibility = View.INVISIBLE
      identified_name_textview1.visibility = View.INVISIBLE
        identified_confidence1.visibility = View.INVISIBLE
    }

    private fun hideIdent2(){//Hide the textviews and button
        identified_button2.visibility = View.INVISIBLE
        identified_name_textview2.visibility = View.INVISIBLE
        identified_confidence2.visibility = View.INVISIBLE
    }

    private fun populateIdent0(identifiedString: ArrayList<String>){//Populate the textfields with the identification information
        try {
            var singleIdent = getSingleIdent(identifiedString, 0)//Make a local copy of the first three items
            var confidence = convertTo2dp(singleIdent.get(0))//Convert string to 2dp
            identified_name_textview0.text = singleIdent.get(2)
            identified_confidence0.text = confidence
        }
        catch(e: Exception){
            Log.d("IdentifiedActivity", "PopulateIdent0 Error = ${e.message}")
        }
    }

    private fun populateIdent1(identifiedString: ArrayList<String>){//Populate the textfields with the identification information
        try {
            var singleIdent = getSingleIdent(identifiedString, 3)
            var confidence = convertTo2dp(singleIdent.get(0))//Convert string to 2dp
            identified_name_textview1.text = singleIdent.get(2)
            identified_confidence1.text = confidence
        }catch (e: Exception){
            Log.d("IdentifiedActivity", "PopulateIdent1 Error = ${e.message}")
        }
    }

    private fun populateIdent2(identifiedString: ArrayList<String>){//Populate the textfields with the identification information
        try {
            var singleIdent = getSingleIdent(identifiedString, 6)
            var confidence = convertTo2dp(singleIdent.get(0))//Convert string to 2dp
            identified_name_textview2.text = singleIdent.get(2)
            identified_confidence2.text = confidence
        }
        catch (e: Exception){
            Log.d("IdentifiedActivity", "PopulateIdent2 Error = ${e.message}")
        }
    }

    private fun convertTo2dp(string: String): String{ //Converts a decimal to 2dp, returning a string
        var origString = string.toDouble()
        val newString = BigDecimal(origString).setScale(2, RoundingMode.HALF_EVEN)//Round the confidence to 2dp
        return newString.toString()
    }



    private fun getSingleIdent(identifiedString: ArrayList<String>, indexStart: Int) : ArrayList<String>{ //Returns an ArrayList, 3 long from the provided ArrayList
        var orignalString = identifiedString
        var newString = ArrayList<String>()
        var counter = 0
        var index = indexStart
        for (item in orignalString){
            if (counter <= 2) {
                newString.add(counter, orignalString.get(index))
                index++
                counter++
            }
        }
        return newString
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
}


