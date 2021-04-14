package com.nesib.yourbooknotes.ui.main.fragments.download

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.FragmentDownloadQuoteBinding
import com.nesib.yourbooknotes.utils.showToast
import java.io.File
import java.io.File.separator
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*
import java.util.jar.Manifest


class DownloadQuoteFragment : Fragment(R.layout.fragment_download_quote) {
    private var photoFile: File? = null
    private var photoBitmap: Bitmap? = null
    private lateinit var binding: FragmentDownloadQuoteBinding
    private val args by navArgs<DownloadQuoteFragmentArgs>()
    private val photoStyles: List<View> by lazy {
        listOf(
            binding.photoStyleBlack,
            binding.photoStyleWhite,
            binding.photoStyleBlue,
            binding.photoStyleGreen,
            binding.photoStyleRose,
            binding.photoStyleYellow
        )
    }
    private val photoStyleColors = listOf("#121212","#F2F2F2","#DCEBFE","#C7FFCE","#FFD1EA","#FFF9AB")
    private val incHeightAnim by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.increase_height_anim
        )
    }
    private val decHeightAnim by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.decrease_height_anim
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDownloadQuoteBinding.bind(view)
        setHasOptionsMenu(true)
        setupClickListeners()
        setupUi()
    }

    private fun setupUi() {
        binding.textSizeSlider.value = 18f
        if (args.quoteText!!.length in 181..239) {
            binding.textSizeSlider.valueTo = 18.toFloat()
            binding.textSizeSlider.value = 16f
        }
        if (args.quoteText!!.length in 240..300) {
            binding.textSizeSlider.valueTo = 16.toFloat()
            binding.textSizeSlider.value = 14f
        }
        if (args.quoteText!!.length > 300) {
            binding.textSizeSlider.valueTo = 14.toFloat()
            binding.textSizeSlider.value = 12f
        }
        binding.apply {
            quoteText.text = "\"${args.quoteText}\""
            quoteAuthorText.text = "-${args.quoteAuthor}"
        }
    }

    private fun setupClickListeners() {
        photoStyles.forEachIndexed { i: Int, view: View ->
            view.setOnClickListener {
                it.startAnimation(AnimationUtils.loadAnimation(requireContext(),R.anim.scale_anim))
                binding.photoContainer.setBackgroundColor(Color.parseColor(photoStyleColors[i]))
                if(i==0){
                    binding.quoteText.setTextColor(ContextCompat.getColor(requireContext(),R.color.colorPrimaryOnDark))
                    binding.quoteAuthorText.setTextColor(ContextCompat.getColor(requireContext(),R.color.colorSecondaryOnDark))
                }else{
                    binding.quoteText.setTextColor(ContextCompat.getColor(requireContext(),R.color.colorPrimaryOnLight))
                    binding.quoteAuthorText.setTextColor(ContextCompat.getColor(requireContext(),R.color.colorSecondaryOnLight))
                }
            }
        }
        binding.textSizeSlider.addOnChangeListener { slider, value, fromUser ->
            binding.sliderValueText.text = value.toInt().toString()
            binding.quoteText.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
        }
    }

    private fun takeScreenshot() {
        val view = binding.photoContainer
        photoBitmap = Bitmap.createBitmap(
            view.width,
            view.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(photoBitmap!!)
        view.draw(canvas)

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        } else {
            saveMediaToStorage(photoBitmap!!)
        }
    }

    private fun saveMediaToStorage(bitmap: Bitmap) {
        val filename = "${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requireContext().contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)

        }
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            showToast("Downloaded image successfully !")
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveMediaToStorage(photoBitmap!!)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.download_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.download_done_menu_item) {
            takeScreenshot()
            return true
        }
        return super.onOptionsItemSelected(item)
    }


}