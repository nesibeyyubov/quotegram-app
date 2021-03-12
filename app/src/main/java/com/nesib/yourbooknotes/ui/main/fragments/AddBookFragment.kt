package com.nesib.yourbooknotes.ui.main.fragments

import android.R.attr
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.FragmentAddBookBinding
import com.theartofdev.edmodo.cropper.CropImage
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class AddBookFragment : BottomSheetDialogFragment(), View.OnClickListener {
    private lateinit var binding: FragmentAddBookBinding
    private lateinit var imagePickLauncher: ActivityResultLauncher<Intent>

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
    }

    private fun setOnClickListeners() {
        binding.addBookImageButton.setOnClickListener(this)
        binding.addBookImageContainer.setOnClickListener(this)
        binding.addBookButton.setOnClickListener(this)
    }

    private fun pickImage() {
        val intent = Intent(
            Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        imagePickLauncher.launch(intent)
//        ImagePicker.with(requireActivity())
//            .compress(100)
//            .crop(5f, 8f)
//            .galleryOnly()
//            .start { resultCode, data ->
//                Log.d("mytag", "pickImage: resultCode:${resultCode} ")
//                if(resultCode == RESULT_OK){
//                    val uri = data!!.data!!
//                    val file = File(uri.path!!)
//                    val fileSize = file.length().toFloat() / 1024
//                    Log.d("mytag", "fileSize: $fileSize")
//
//                    binding.bookImageView.visibility = View.VISIBLE
//                    binding.bookImageView.setImageURI(uri)
//                }
//            }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.addBookImageButton, R.id.addBookImageContainer -> {
                pickImage()
            }
            binding.addBookButton.id -> {
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
                binding.bookImageView.setImageURI(resultUri)
                val file = File(resultUri.path!!)
                lifecycleScope.launch(Dispatchers.IO) {
                    val compressedFile = Compressor.compress(requireContext(),file)
                    Log.d("mytag", "file after compression: size = ${(compressedFile.length()/1024).toInt()} kb ")
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }


}