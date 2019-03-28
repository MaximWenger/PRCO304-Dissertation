package com.example.planty.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.example.planty.R
import com.google.firebase.auth.FirebaseAuth
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_identified.*

class IdentifiedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identified)

        supportActionBar?.title ="Identified"

        val adapter = GroupAdapter<ViewHolder>()

        adapter.add(IdentifiedPlant())
        adapter.add(IdentifiedPlant())
        adapter.add(IdentifiedPlant())

        recyclerView_Identified.adapter


        verifyLoggedIn()

        val test = intent.getStringArrayListExtra("identifications")
        Log.d("IdentifiedActivity", "test = ${test.size}")
        for (item in test){
            Log.d("IdentifiedActivity", "test = ${item}")
        }
        Log.d("IdentifiedActivity", "COMPLETED")
    }

    private fun verifyLoggedIn(){ //Check if the User is already logged in, if not, return User to registerActivity
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null){//If no user ID, user is not logged in
            val intent =  Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
            startActivity(intent)
        }
    }
}

class IdentifiedPlant: Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
     //Will be called in the list for each object
    }

    override fun getLayout(): Int { //Renders each row
        return R.layout.identifiedrow
    }
}
