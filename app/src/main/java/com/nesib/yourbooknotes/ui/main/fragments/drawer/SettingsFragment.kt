package com.nesib.yourbooknotes.ui.main.fragments.drawer

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.data.local.SharedPreferencesRepository
import com.nesib.yourbooknotes.databinding.FragmentSettingsBinding
import com.nesib.yourbooknotes.utils.Constants.KEY_THEME
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named


@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private lateinit var binding: FragmentSettingsBinding

    @Inject
    @Named("themeSharedPreferences")
    lateinit var sharedPreferences: SharedPreferences

    private lateinit var sharedPrefEditor: SharedPreferences.Editor

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)
        setupUi()
        setupClickListeners()

    }

    private fun setupClickListeners() {
        binding.genreSpinnerContainer.setOnClickListener {
            binding.themeSpinner.performClick()
        }
        binding.themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                sharedPrefEditor = sharedPreferences.edit()
                when (resources.getStringArray(R.array.themes)[position]) {
                    resources.getString(R.string.theme_dark) -> {
                        sharedPrefEditor.putString(
                            KEY_THEME,
                            resources.getString(R.string.theme_dark)
                        )
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                    resources.getString(R.string.theme_light) -> {
                        sharedPrefEditor.putString(
                            KEY_THEME,
                            resources.getString(R.string.theme_light)
                        )
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                    resources.getString(R.string.theme_default) -> {
                        sharedPrefEditor.putString(
                            KEY_THEME,
                            resources.getString(R.string.theme_default)
                        )
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    }
                }
                sharedPrefEditor.apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    private fun setupUi() {
        when (sharedPreferences.getString(KEY_THEME, resources.getString(R.string.theme_default))) {
            resources.getString(R.string.theme_default) -> {
                binding.themeSpinner.setSelection(0)
            }
            resources.getString(R.string.theme_dark) -> {
                binding.themeSpinner.setSelection(1)
            }
            resources.getString(R.string.theme_light) -> {
                binding.themeSpinner.setSelection(2)
            }
        }
    }
}