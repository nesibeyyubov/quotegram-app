package com.nesib.yourbooknotes.ui.on_boarding

import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.ActivityStartBinding

class StartActivity : AppCompatActivity() {
    private lateinit var binding:ActivityStartBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setAppTheme()

        navController = (supportFragmentManager.findFragmentById(R.id.fragmentContainerView_startActivity) as NavHostFragment).navController

        setSupportActionBar(binding.toolbarStartActivity)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        navController.addOnDestinationChangedListener{ navController: NavController, navDestination: NavDestination, bundle: Bundle? ->
            if(navDestination.id == R.id.splashFragment){
                binding.toolbarStartActivity.visibility = View.GONE
            }
            else{
                binding.toolbarStartActivity.visibility = View.VISIBLE
            }
        }
    }
    private fun setAppTheme(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            window.statusBarColor = Color.BLACK
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}