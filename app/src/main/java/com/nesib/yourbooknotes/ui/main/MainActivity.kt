package com.nesib.yourbooknotes.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.ActivityMainBinding
import com.nesib.yourbooknotes.databinding.DrawerHeaderLayoutBinding
import com.nesib.yourbooknotes.databinding.NotAuthenticatedLayoutBinding
import com.nesib.yourbooknotes.ui.on_boarding.StartActivity
import com.nesib.yourbooknotes.ui.viewmodels.AuthViewModel
import com.nesib.yourbooknotes.ui.viewmodels.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var authViewModel: AuthViewModel
    private val sharedViewModel: SharedViewModel by viewModels()

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
        setSupportActionBar(binding.toolbarMainActivity)

        initViewModels()
        setupDrawerUi()
        setupNavigation()
        setupClickListeners()
        setupBottomNavChangeListeners()
        setupDrawerNavChangeListener()
        addSearchInputTextChangeListener()
    }

    private fun initViewModels() {
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
    }

    private fun setupDrawerUi() {
        val extraDetail = authViewModel.getUser()
        if (authViewModel.isAuthenticated) {
            binding.apply {
                val headerView = drawerNavigationView.getHeaderView(0)
                val headerBinding = DrawerHeaderLayoutBinding.bind(headerView)
                headerBinding.headerUsername.text = extraDetail!!.username
                headerBinding.headerEmail.text = extraDetail.email
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
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    fun showAuthenticationDialog() {
        dialog.show()
    }

    private fun setupBottomNavChangeListeners() {
        navController.addOnDestinationChangedListener { navController: NavController, navDestination: NavDestination, bundle: Bundle? ->
            supportActionBar?.title = ""

            when (navDestination.id) {
                R.id.homeFragment -> {
                    binding.toolbarText.text = "Quotes"
                }
                R.id.searchFragment -> {
                    binding.toolbarMainActivity.navigationIcon = null
                    binding.toolbarText.text = "Discover"
                    binding.searchInputContainer.visibility = View.VISIBLE
                    binding.toolbarText.visibility = View.GONE
                }
                R.id.editUserFragment -> {
                    binding.toolbarText.text = "Edit User"
                }
                R.id.selectBookFragment -> {
                    binding.toolbarText.text = "Select Book"
                }
                R.id.addQuoteFragment -> {
                }
                R.id.addBookFragment -> {
                }
                R.id.myProfileFragment -> {
                    binding.toolbarText.text = ""
                }
                R.id.searchQuotesFragment -> {
                    binding.toolbarText.text = sharedViewModel.toolbarText
                }
            }
            if (navDestination.id != R.id.searchFragment) {
                binding.searchInputContainer.visibility = View.GONE
                binding.toolbarText.visibility = View.VISIBLE
                setSupportActionBar(binding.toolbarMainActivity)
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

    private fun addSearchInputTextChangeListener() {
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setChangedText(s.toString())
            }

        })
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
                if (authViewModel.isAuthenticated) {
                    navController.navigate(R.id.action_global_selectBookFragment)
                } else {
                    dialog.show()
                }
            }
            R.id.add_book_btn -> {
                if (authViewModel.isAuthenticated) {
                    navController.navigate(R.id.action_global_addBookFragment)
                } else {
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