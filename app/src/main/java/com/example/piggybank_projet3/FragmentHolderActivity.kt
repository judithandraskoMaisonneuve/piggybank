package com.example.piggybank_projet3

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.piggybank_projet3.ui.dashboard.DashboardFragment

class FragmentHolderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragmentholder_layout)

        // Initialize Navigation
        val navigation = NavigationBar(this, supportFragmentManager)

        // Pass the navigation bar view to setup navigation
        val navigationBar = findViewById<View>(R.id.navigation_bar)
        navigation.setupNavigation(navigationBar)

        // Set the default fragment if none exists
        if (savedInstanceState == null) {
            navigation.navigateToFragment(DashboardFragment())
        }
    }
}
