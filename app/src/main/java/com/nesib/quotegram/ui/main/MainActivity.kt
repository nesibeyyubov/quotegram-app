package com.nesib.quotegram.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import coil.load
import com.nesib.quotegram.BuildConfig
import com.nesib.quotegram.R
import com.nesib.quotegram.databinding.ActivityMainBinding
import com.nesib.quotegram.databinding.DrawerHeaderLayoutBinding
import com.nesib.quotegram.databinding.NotAuthenticatedLayoutBinding
import com.nesib.quotegram.ui.on_boarding.StartActivity
import com.nesib.quotegram.ui.viewmodels.AuthViewModel
import com.nesib.quotegram.ui.viewmodels.SharedViewModel
import com.nesib.quotegram.utils.*
import com.nesib.quotegram.utils.Constants.TEXT_DIRECT_TO_LOGIN
import com.nesib.quotegram.utils.Constants.TEXT_DIRECT_TO_SIGNUP
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
            authViewModel.logout()
            val intent = Intent(
                this,
                StartActivity::class.java
            )
            intent.putExtra(Constants.TEXT_DIRECT_TO_LOGIN, true)
            startActivity(intent)
        }
        dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarMainActivity)
        this.title = ""

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
        val currentUser = authViewModel.getUser()
        if (authViewModel.isAuthenticated) {
            binding.apply {
                val headerView = drawerNavigationView.getHeaderView(0)
                val headerBinding = DrawerHeaderLayoutBinding.bind(headerView)
                headerBinding.headerUsername.text = currentUser.username
                headerBinding.headerEmail.text = currentUser.email
                headerBinding.headerProfileImage.load(currentUser.profileImage) {
                    error(R.drawable.user)
                }
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
            when (menuItem.itemId) {
                R.id.drawer_logout -> {
                    authViewModel.logout()
                    startActivity(Intent(this@MainActivity, StartActivity::class.java))
                }
                R.id.drawer_signup -> {
                    authViewModel.logout()
                    val intent = Intent(this, StartActivity::class.java)
                    intent.putExtra(TEXT_DIRECT_TO_SIGNUP, true)
                    startActivity(intent)
                    finish()
                }
                R.id.drawer_login -> {
                    authViewModel.logout()
                    val intent = Intent(this, StartActivity::class.java)
                    intent.putExtra(TEXT_DIRECT_TO_LOGIN, true)
                    startActivity(intent)
                    finish()
                }
                R.id.drawer_privacy -> {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(Constants.PRIVACY_POLICY_URL)
                        startActivity(intent)
                    } catch (e: Exception) {
                        showToast(e.message ?: "Something went wrong")
                    }
                }
                R.id.drawer_review -> {
                    binding.drawerLayout.close()
                    reviewApp()
                }
                R.id.drawer_share -> {
                    binding.drawerLayout.close()
                    shareApp()
                }
                R.id.drawer_home -> {
                    binding.drawerLayout.close()
                }
                R.id.drawer_settings -> {
                    binding.drawerLayout.close()
                    navController.navigate(R.id.action_global_settingsFragment)
                }
            }
            true
        }
    }

    private fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Quotegram")
            var shareMessage =
                "\nLet me recommend you this cool application,which is for book and quote lovers\n\n"
            shareMessage =
                shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "Choose one"))
        } catch (e: Exception) {
            showToast("Some error happened,please try again")
        }
    }

    private fun reviewApp() {
        val uri: Uri = Uri.parse("market://details?id=$packageName")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
        try {
            startActivity(intent)
        } catch (e: Exception) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }

    private fun setupNavigation() = with(binding) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView_mainActivity) as NavHostFragment
        navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.myProfileFragment, R.id.notificationsFragment),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        bottomNavView.menu.getItem(2).isEnabled = false
        bottomNavView.setupWithNavController(navController)
        drawerNavigationView.setupWithNavController(navController)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    fun showAuthenticationDialog() {
        dialog.show()
    }

    private fun setupBottomNavChangeListeners() {
        navController.addOnDestinationChangedListener { navController: NavController, navDestination: NavDestination, bundle: Bundle? ->
            when (navDestination.id) {
                R.id.homeFragment -> {
                    binding.toolbarText.text = "Quotes"
                    if (!binding.fabAddButton.isShown) {
                        binding.fabAddButton.show()
                    }
                    if (binding.bottomNavView.visibility == View.GONE) {
                        binding.bottomNavView.visible()
                    }
                }
                R.id.searchFragment -> with(binding) {
                    toolbarMainActivity.navigationIcon = null
                    toolbarText.text = "Discover"
                    searchInputContainer.visibility = View.VISIBLE
                    toolbarText.visibility = View.GONE
                }
                R.id.editUserFragment -> {
                    binding.toolbarText.text = "Edit User"
                }
                R.id.quoteFragment -> {
                    binding.toolbarText.text = "Quote"
                }
                R.id.notificationsFragment -> {
                    binding.toolbarText.text = "Notifications"
                }
                R.id.downloadQuoteFragment -> {
                    binding.toolbarText.text = "Download"
                }
                R.id.myProfileFragment -> {
                    binding.toolbarText.text = "Your Profile"
                }
                R.id.searchQuotesFragment -> {
                    binding.toolbarText.text = sharedViewModel.toolbarText
                }
                R.id.settingsFragment -> {
                    binding.toolbarText.text = "Settings"
                }
                R.id.userProfileFragment -> {
                    binding.toolbarText.text = "User Profile"
                }
            }

            if (navDestination.id != R.id.searchFragment) {
                binding.searchInputContainer.visibility = View.GONE
                binding.toolbarText.visibility = View.VISIBLE
            }

            if (navDestination.isRootScreen()) {
                binding.drawerNavigationView.menu.findItem(R.id.drawer_home).isChecked = true
            }
            if (navDestination.shouldShowBottomNav()) {
                showBottomNavFab()
            } else {
                hideBottomNavFab()
            }
        }
    }

    private fun showBottomNavFab() {
        binding.bottomNavView.visible()
        binding.fabAddButton.show()
    }

    private fun hideBottomNavFab() {
        binding.bottomNavView.gone()
        binding.fabAddButton.hide()
    }

    private fun addSearchInputTextChangeListener() {
        binding.searchInput.doAfterTextChanged { sharedViewModel.setChangedText(it.toString()) }
    }

    private fun setupClickListeners() {
        binding.fabAddButton.setOnClickListener(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.fab_add_button -> {
                if (authViewModel.isAuthenticated) {
                    navController.navigate(R.id.action_global_addQuoteFragment)
                } else {
                    dialog.show()
                }
            }
        }
    }


}