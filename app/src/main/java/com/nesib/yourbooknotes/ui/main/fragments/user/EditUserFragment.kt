package com.nesib.yourbooknotes.ui.main.fragments.user

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.FragmentEditProfileBinding
import com.nesib.yourbooknotes.models.User
import com.nesib.yourbooknotes.ui.viewmodels.AuthViewModel
import com.nesib.yourbooknotes.ui.viewmodels.UserViewModel
import com.nesib.yourbooknotes.utils.DataState
import com.nesib.yourbooknotes.utils.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditUserFragment : Fragment(R.layout.fragment_edit_profile) {
    private val args by navArgs<EditUserFragmentArgs>()
    private val userViewModel: UserViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels({requireActivity()})
    private lateinit var binding: FragmentEditProfileBinding

    private var menuItem:MenuItem? = null

    private var updatedUser: User? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEditProfileBinding.bind(view)
        setHasOptionsMenu(true)
        setupUi()
        subscribeObservers()
    }

    private fun setupUi() {
        binding.apply {
            usernameEditText.setText(args.user?.username)
            bioEditText.setText(args.user?.bio)
            emailEditText.setText(args.user?.email)
            fullnameEditText.setText(args.user?.fullname)
        }
    }

    private fun subscribeObservers() {
        userViewModel.user.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    binding.updateProfileErrorTextView.visibility = View.GONE
                    showToast(it.data!!.message!!)
                    parentFragmentManager.setFragmentResult(
                        "updatedUser",
                        bundleOf("updatedUser" to updatedUser)
                    )
                    findNavController().popBackStack()
                }
                is DataState.Fail -> {
                    binding.updateProfileErrorTextView.visibility = View.VISIBLE
                    binding.updateProfileErrorTextView.text = it.message
                    menuItem?.actionView = null
                    showToast(it.message)
                }
                is DataState.Loading -> {
                    Log.d("mytag", "update user loading...")
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_profile_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.edit_profile_save_menu_item) {
            menuItem = item
            item.setActionView(R.layout.progress_bar_layout)
            updateUser()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateUser() {
        val username = binding.usernameEditText.text.toString().trim()
        val fullName = binding.fullnameEditText.text.toString().trim()
        val bio = binding.bioEditText.text.toString().trim()
        updatedUser =
            args.user!!.copy(username = username, fullname = fullName, bio = bio)
        if(args.user!!.username != username || args.user!!.fullname != fullName || args.user!!.bio != bio){
            if(username.length < 5){
                binding.updateProfileErrorTextView.visibility = View.VISIBLE
                binding.updateProfileErrorTextView.text = "Username length must be at least 5 characters"
                menuItem?.actionView =null
                return
            }
            Log.d(
                "mytag",
                "updateUser: currentUser in sharedPrefernces: ${authViewModel.currentUser}"
            )
            authViewModel.currentUser = authViewModel.currentUser?.copy(username = username)
            authViewModel.updateUser()
            userViewModel.updateUser(username, fullName, bio)
        }else{
            parentFragmentManager.setFragmentResult(
                "updatedUser",
                bundleOf("updatedUser" to updatedUser)
            )
            findNavController().popBackStack()
        }
    }
}