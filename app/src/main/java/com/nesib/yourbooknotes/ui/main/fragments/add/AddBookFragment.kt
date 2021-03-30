package com.nesib.yourbooknotes.ui.main.fragments.add

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.adapters.SpinnerAdapter
import com.nesib.yourbooknotes.databinding.FragmentAddBookBinding
import com.nesib.yourbooknotes.ui.viewmodels.BookViewModel
import com.nesib.yourbooknotes.utils.DataState
import com.theartofdev.edmodo.cropper.CropImage
import dagger.hilt.android.AndroidEntryPoint
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

@AndroidEntryPoint
class AddBookFragment : BottomSheetDialogFragment(), View.OnClickListener {
    private lateinit var binding: FragmentAddBookBinding
    private lateinit var imagePickLauncher: ActivityResultLauncher<Intent>
    private val bookViewModel: BookViewModel by viewModels()

    private var selectedBookImage: File? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddBookBinding.inflate(inflater)
        imagePickLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                if (activityResult.resultCode == RESULT_OK) {
                    CropImage.activity(activityResult.data!!.data)
                        .setAspectRatio(5, 8)
                        .start(requireContext(), this)
                }
            }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
        subscribeObservers()
    }

    private fun subscribeObservers() {
        bookViewModel.book.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    toggleProgressBar(false)
                    findNavController().popBackStack()
                    Toast.makeText(requireContext(), "Book is added successfully", Toast.LENGTH_SHORT)
                        .show()
                }
                is DataState.Fail -> {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    toggleProgressBar(false)
                }
                is DataState.Loading -> {
                    toggleProgressBar(true)
                }
            }
        }
    }

    private fun toggleProgressBar(loading: Boolean) {
        binding.addBtnTextView.visibility = if (loading) View.GONE else View.VISIBLE
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun setOnClickListeners() {
        val genres = resources.getStringArray(R.array.book_genres).toList()
        binding.bookGenreSpinner.adapter = SpinnerAdapter(requireContext(),genres)
        binding.addBookImageButton.setOnClickListener(this)
        binding.addBookImageContainer.setOnClickListener(this)
        binding.addBookButton.setOnClickListener(this)
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setDataAndType(
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            "image/*"
        )
        imagePickLauncher.launch(intent)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.addBookImageButton, R.id.addBookImageContainer -> {
                pickImage()
            }
            binding.addBookButton.id -> {
                val name = binding.bookTitle.text.toString()
                val author = binding.bookAuthor.text.toString()
                val genre = binding.bookGenreSpinner.selectedItem.toString().toLowerCase(Locale.ROOT)
                bookViewModel.postBook(name, author, genre, selectedBookImage!!)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri: Uri = result.uri
                binding.bookImageView.visibility = View.VISIBLE
                binding.bookPhotoHereTextView.visibility = View.INVISIBLE
                val file = File(resultUri.path!!)

                toggleLoadingImageProgressBar(true)
                lifecycleScope.launch(Dispatchers.IO) {
                    selectedBookImage = Compressor.compress(requireContext(), file) {
                        quality(20)
                    }
                    withContext(Dispatchers.Main) {
                        toggleLoadingImageProgressBar(false)
                        binding.bookImageView.setImageURI(selectedBookImage?.toUri())
                    }
                    Log.d(
                        "mytag",
                        "file after compression: size = ${(selectedBookImage?.length()!! / 1024).toInt()} kb "
                    )
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }

    private fun toggleLoadingImageProgressBar(loading: Boolean){
        binding.addImageIcon.visibility = if(loading) View.INVISIBLE else View.VISIBLE
        binding.loadingImageProgressBar.visibility = if(loading) View.VISIBLE else View.INVISIBLE
    }


}