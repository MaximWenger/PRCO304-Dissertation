package com.example.planty.Activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import com.example.planty.Classes.CloudVisionData
import com.example.planty.R

import kotlinx.android.synthetic.main.activity_self_identify.*

class SelfIdentifyActivity : AppCompatActivity() {
    private val cloudVision = CloudVisionData()

    private var plantType = cloudVision.getBaseIdentLibrary().first() //Used to keep chosen plant type (from spinner)



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_self_identify)
       // setSupportActionBar(toolbar)

           populateSpinner()

        SelfIdentify_Save_Button.setOnClickListener{

        }
    }

    private fun populateSpinner(){//Populates the spinner (Mutli-choice options)

        val baseIdentLibrary = cloudVision.getBaseIdentLibrary()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, baseIdentLibrary)

        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        SelfIdentify_PlantTypeSpinner.adapter = adapter

        SelfIdentify_PlantTypeSpinner.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                plantType = parent?.getItemAtPosition(position).toString() //Used to update chosen planttype (From spinner)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }


    }

}
