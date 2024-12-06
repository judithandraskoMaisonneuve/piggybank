package com.example.piggybank_projet3

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.piggybank_projet3.ui.dashboard.DashboardFragment

class NavigationBar(private val context: Context, private val fragmentManager: FragmentManager) {

    fun setupNavigation(view: View) {
        view.findViewById<View>(R.id.btn_home).setOnClickListener {
            navigateToFragment(DashboardFragment())
        }

        view.findViewById<View>(R.id.btn_analysis).setOnClickListener {
            navigateToFragment(AnalysisFragment())
        }


        view.findViewById<View>(R.id.btn_budget).setOnClickListener {
            navigateToFragment(BudgetFragment())
        }

        view.findViewById<View>(R.id.btn_settings).setOnClickListener {
            navigateToFragment(SettingsFragment())
        }
    }

    fun navigateToFragment(fragment: Fragment) {
        fragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

}
