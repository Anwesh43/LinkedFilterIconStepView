package com.anwesh.uiprojects.linkedfiltericonstepview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.filtericonstepview.FilterIconStepView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FilterIconStepView.create(this)
    }
}
