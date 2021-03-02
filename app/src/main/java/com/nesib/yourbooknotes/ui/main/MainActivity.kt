package com.nesib.yourbooknotes.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.google.android.material.navigation.NavigationView
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.ActivityMainBinding
import com.nesib.yourbooknotes.ui.on_boarding.StartActivity

class MainActivity : AppCompatActivity(),View.OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val toBottomAnimation by lazy {
        AnimationUtils.loadAnimation(
            this@MainActivity,
            R.anim.to_bottom
        )
    }
    private val fromBottomAnimation by lazy {
        AnimationUtils.loadAnimation(
            this@MainActivity,
            R.anim.from_bottom
        )
    }
    private val extendedFabAnimation by lazy {
        AnimationUtils.loadAnimation(
            this@MainActivity,
            R.anim.extended_rotate
        )
    }
    private val nonExtendedFabAnimation by lazy {
        AnimationUtils.loadAnimation(
            this@MainActivity,
            R.anim.non_extended_rotate
        )
    }
    private var fabExtended = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarMainActivity)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView_mainActivity) as NavHostFragment
        navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.userProfileFragment),
            binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.bottomNavView.menu.getItem(2).isEnabled = false
        binding.bottomNavView.setupWithNavController(navController)
        binding.drawerNavigationView.setupWithNavController(navController)

        binding.addQuoteBtn.setOnClickListener(this)
        binding.addBookBtn.setOnClickListener(this)
        binding.fabAddButton.setOnClickListener(this)

        binding.drawerNavigationView.setNavigationItemSelectedListener{menuItem->
            if(menuItem.itemId == R.id.drawer_logout){
                startActivity(Intent(this@MainActivity,StartActivity::class.java))
            }
            true
        }

        navController.addOnDestinationChangedListener { navController: NavController, navDestination: NavDestination, bundle: Bundle? ->
//            if(navDestination.id == R.id.searchFragment){
//                binding.toolbarMainActivity.visibility = View.INVISIBLE
//            }else{
//                if(binding.toolbarMainActivity.visibility == View.INVISIBLE){
//                    binding.toolbarMainActivity.visibility = View.VISIBLE
//                }
//            }

            if (navDestination.id == R.id.editUserFragment) {
                binding.bottomNavView.visibility = View.GONE
                binding.fabAddButton.hide()
                if(binding.addQuoteBtn.visibility == View.VISIBLE){
                    binding.addQuoteBtn.visibility = View.GONE
                    binding.addBookBtn.visibility = View.GONE
                }
            } else {
                if (binding.bottomNavView.visibility == View.GONE) {
                    binding.bottomNavView.visibility = View.VISIBLE
                    binding.fabAddButton.show()
                    if(binding.addQuoteBtn.visibility == View.GONE && fabExtended){
                        binding.addQuoteBtn.visibility = View.VISIBLE
                        binding.addBookBtn.visibility = View.VISIBLE
                    }
                }
            }
        }


    }



    private fun startExtendedFabAnimation() {
        binding.fabAddButton.startAnimation(extendedFabAnimation)
        binding.addBookBtn.visibility = View.VISIBLE
        binding.addQuoteBtn.visibility = View.VISIBLE
        binding.addQuoteBtn.startAnimation(fromBottomAnimation)
        binding.addBookBtn.startAnimation(fromBottomAnimation)
    }

    private fun startNonExtendedFabAnimation() {
        binding.fabAddButton.startAnimation(nonExtendedFabAnimation)
        binding.addQuoteBtn.startAnimation(toBottomAnimation)
        binding.addBookBtn.startAnimation(toBottomAnimation)
        binding.addBookBtn.visibility = View.INVISIBLE
        binding.addQuoteBtn.visibility = View.INVISIBLE
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.add_quote_btn->{
                navController.navigate(R.id.action_global_addQuoteFragment)
            }
            R.id.add_book_btn->{
                navController.navigate(R.id.action_global_addBookFragment)
            }
            R.id.fab_add_button->{
                if (!fabExtended) {
                    startExtendedFabAnimation()
                    fabExtended = true
                } else {
                    startNonExtendedFabAnimation()
                    fabExtended = false
                }
            }
        }
    }


}