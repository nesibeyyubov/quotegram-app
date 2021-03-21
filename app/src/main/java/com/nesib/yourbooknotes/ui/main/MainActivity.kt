package com.nesib.yourbooknotes.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.ActivityMainBinding
import com.nesib.yourbooknotes.databinding.NotAuthenticatedLayoutBinding
import com.nesib.yourbooknotes.ui.on_boarding.StartActivity
import com.nesib.yourbooknotes.ui.viewmodels.AuthViewModel
import com.nesib.yourbooknotes.ui.viewmodels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var authViewModel: AuthViewModel

    val dialog by lazy {
        val notAuthenticatedBinding = NotAuthenticatedLayoutBinding.bind(
            layoutInflater.inflate(
                R.layout.not_authenticated_layout,
                binding.root,
                false
            )
        )
        val dialog = AlertDialog.Builder(this)
            .setView(notAuthenticatedBinding.root)
            .create()
        notAuthenticatedBinding.notNowBtn.setOnClickListener { dialog.dismiss() }
        notAuthenticatedBinding.signInBtn.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    StartActivity::class.java
                )
            )
        }
        dialog
    }

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

        initViewModels()
        setSupportActionBar(binding.toolbarMainActivity)
        setupDrawerUi()
        setupNavigation()
        setupClickListeners()
        setupBottomNavChangeListeners()
        setupDrawerNavChangeListener()
    }

    private fun initViewModels() {
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
    }

    private fun setupDrawerUi() {
        val extraDetail = authViewModel.getExtraUserDetail().split(",")
        val username = extraDetail[0]
        val email = extraDetail[1]
        val profileImage = extraDetail[2]
        if (authViewModel.isAuthenticated) {
            binding.apply {
                val headerView = drawerNavigationView.getHeaderView(0)
                headerView.findViewById<TextView>(R.id.headerUsername).text = username
                headerView.findViewById<TextView>(R.id.headerEmail).text = email
                val drawerMenu = drawerNavigationView.menu
                drawerMenu.findItem(R.id.drawer_login).isVisible = false
                drawerMenu.findItem(R.id.drawer_signup).isVisible = false
            }
        } else {
            binding.apply {
                val bottomNavMenu = drawerNavigationView.menu
                bottomNavMenu.findItem(R.id.drawer_logout).isVisible = false
            }
        }
    }

    private fun setupDrawerNavChangeListener() {
        binding.drawerNavigationView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.drawer_logout) {
                authViewModel.logout()
                startActivity(Intent(this@MainActivity, StartActivity::class.java))
            }
            true
        }
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView_mainActivity) as NavHostFragment
        navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.myProfileFragment),
            binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavView.menu.getItem(2).isEnabled = false
        binding.bottomNavView.setupWithNavController(navController)
        binding.drawerNavigationView.setupWithNavController(navController)
    }

    fun showAuthenticationDialog(){
        dialog.show()
    }

    private fun setupBottomNavChangeListeners() {
        navController.addOnDestinationChangedListener { navController: NavController, navDestination: NavDestination, bundle: Bundle? ->
            if (navDestination.id != R.id.searchFragment) {
                if (binding.toolbarMainActivity.visibility == View.GONE) {
                    binding.toolbarMainActivity.visibility = View.VISIBLE
                }
            }
            if (navDestination.id == R.id.myProfileFragment) {
                supportActionBar?.title = ""
            }

            if (navDestination.id == R.id.editUserFragment
                || navDestination.id == R.id.selectBookFragment
                || navDestination.id == R.id.addQuoteFragment
                || navDestination.id == R.id.addBookFragment
            ) {
                if (binding.addQuoteBtn.visibility == View.VISIBLE) {
                    shrinkFabWithAnimation()
                }
                binding.bottomNavView.visibility = View.GONE
                binding.fabAddButton.hide()

            } else {
                if (binding.bottomNavView.visibility == View.GONE) {
                    if (binding.addQuoteBtn.visibility == View.GONE && fabExtended) {
                        extendFabWithAnimation()
                    }
                    binding.bottomNavView.visibility = View.VISIBLE
                    binding.fabAddButton.show()

                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.addQuoteBtn.setOnClickListener(this)
        binding.addBookBtn.setOnClickListener(this)
        binding.fabAddButton.setOnClickListener(this)
    }

    private fun extendFabWithAnimation() {
        binding.fabAddButton.startAnimation(extendedFabAnimation)
        binding.addBookBtn.visibility = View.VISIBLE
        binding.addQuoteBtn.visibility = View.VISIBLE
        binding.addQuoteBtn.startAnimation(fromBottomAnimation)
        binding.addBookBtn.startAnimation(fromBottomAnimation)
        fabExtended = true
    }

    private fun shrinkFabWithAnimation() {
        binding.fabAddButton.startAnimation(nonExtendedFabAnimation)
        binding.addQuoteBtn.startAnimation(toBottomAnimation)
        binding.addBookBtn.startAnimation(toBottomAnimation)
        binding.addBookBtn.visibility = View.INVISIBLE
        binding.addQuoteBtn.visibility = View.INVISIBLE
        fabExtended = false
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.add_quote_btn -> {
                if(authViewModel.isAuthenticated){
                    navController.navigate(R.id.action_global_selectBookFragment)
                }else{
                    dialog.show()
                }
            }
            R.id.add_book_btn -> {
                if(authViewModel.isAuthenticated){
                    navController.navigate(R.id.action_global_addBookFragment)
                }else{
                    dialog.show()
                }
            }
            R.id.fab_add_button -> {
                if (!fabExtended) {
                    extendFabWithAnimation()
                } else {
                    shrinkFabWithAnimation()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }


}