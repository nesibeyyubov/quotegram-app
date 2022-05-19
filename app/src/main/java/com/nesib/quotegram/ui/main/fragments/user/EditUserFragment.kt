package com.nesib.quotegram.ui.main.fragments.user

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nesib.quotegram.R
import com.nesib.quotegram.base.BaseFragment
import com.nesib.quotegram.databinding.FragmentEditProfileBinding
import com.nesib.quotegram.models.User
import com.nesib.quotegram.ui.viewmodels.AuthViewModel
import com.nesib.quotegram.ui.viewmodels.UserViewModel
import com.nesib.quotegram.utils.*
import com.nesib.quotegram.utils.Constants.MIN_USERNAME_LENGTH
import com.nesib.quotegram.utils.Constants.TEXT_UPDATED_USER
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditUserFragment : BaseFragment<FragmentEditProfileBinding>() {
    private val args by navArgs<EditUserFragmentArgs>()
    private val userViewModel: UserViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels({ requireActivity() })

    private var menuItem: MenuItem? = null

    private var updatedUser: User? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        setupUi()
        subscribeObservers()
    }

    private fun setupUi() = with(binding) {
        usernameEditText.setText(args.user?.username)
        bioEditText.setText(args.user?.bio)
        fullnameEditText.setText(args.user?.fullname)
        maxCharactersTextView.text = "${args.user?.bio?.length}/150"
        bioEditText.addTextChangedListener {
            maxCharactersTextView.text = "${it.toString().length}/150"
        }
    }

    private fun subscribeObservers() = with(binding) {
        userViewModel.user.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    updateProfileErrorTextView.gone()
                    showToast(it.data!!.message!!)
                    parentFragmentManager.setFragmentResult(
                        TEXT_UPDATED_USER,
                        bundleOf(TEXT_UPDATED_USER to updatedUser)
                    )
                    findNavController().popBackStack()
                }
                is DataState.Fail -> {
                    updateProfileErrorTextView.visible()
                    updateProfileErrorTextView.text = it.message
                    menuItem?.actionView = null
                    showToast(it.message)
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

    private fun updateUser() = with(binding) {
        val username = usernameEditText.text.toString().trim()
        val fullName = fullnameEditText.text.toString().trim()
        val bio = bioEditText.text.toString().trim()
        args.user?.let { user ->
            updatedUser =
                user.copy(username = username, fullname = fullName, bio = bio)
            if (user.username != username || user.fullname != fullName || user.bio != bio) {
                if (!username.isValidUsername()) {
                    updateProfileErrorTextView.visible()
                    updateProfileErrorTextView.text = getString(
                        R.string.error_username,
                        MIN_USERNAME_LENGTH
                    )
                    menuItem?.actionView = null
                    return@with
                }

                authViewModel.currentUser = authViewModel.currentUser?.copy(username = username)
                authViewModel.updateUser()
                userViewModel.updateUser(username, fullName, bio)
            } else {
                parentFragmentManager.setFragmentResult(
                    TEXT_UPDATED_USER,
                    bundleOf(TEXT_UPDATED_USER to updatedUser)
                )
                findNavController().popBackStack()
            }
        }

    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentEditProfileBinding = FragmentEditProfileBinding.inflate(inflater, container, false)
}