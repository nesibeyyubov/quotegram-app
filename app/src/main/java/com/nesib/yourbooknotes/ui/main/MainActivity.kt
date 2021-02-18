package com.nesib.yourbooknotes.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarMainActivity)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView_mainActivity) as NavHostFragment
        navController = navHostFragment.navController
        setupActionBarWithNavController(navController)

        binding.bottomNavView.menu.getItem(2).isEnabled = false
        binding.bottomNavView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener{ navController: NavController, navDestination: NavDestination, bundle: Bundle? ->
//            if(navDestination.id == R.id.searchFragment){
//                binding.toolbarMainActivity.visibility = View.GONE
//            }else{
//                if(binding.toolbarMainActivity.visibility == View.GONE){
//                    binding.toolbarMainActivity.visibility = View.VISIBLE
//                }
//            }

            if(navDestination.id == R.id.editUserFragment){
                binding.bottomNavView.visibility = View.GONE
                binding.fabAddButton.hide()
            }
            else{
                if(binding.bottomNavView.visibility == View.GONE){
                    binding.bottomNavView.visibility = View.VISIBLE
                    binding.fabAddButton.show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


}