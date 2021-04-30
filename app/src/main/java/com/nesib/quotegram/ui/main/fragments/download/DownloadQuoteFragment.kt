package com.nesib.quotegram.ui.main.fragments.download

import android.app.*
import android.content.ContentValues
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.TypedValue
import android.view.*
import android.view.animation.AnimationUtils
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.nesib.quotegram.R
import com.nesib.quotegram.adapters.ColorBoxAdapter
import com.nesib.quotegram.databinding.FragmentDownloadQuoteBinding
import com.nesib.quotegram.databinding.RationaleDialogLayoutBinding
import com.nesib.quotegram.utils.showToast
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class DownloadQuoteFragment : Fragment(R.layout.fragment_download_quote) {
    private var photoBitmap: Bitmap? = null
    private var imageUri: Uri? = null
    private var imageFileName: String? = ""
    private lateinit var binding: FragmentDownloadQuoteBinding
    private val args by navArgs<DownloadQuoteFragmentArgs>()
    private val photoStyleColors =
        listOf("#1B1B1B", "#F2F2F2", "#DCEBFE", "#C7FFCE", "#FFD1EA", "#FFF9AB")
    private val colorAdapter by lazy { ColorBoxAdapter(photoStyleColors) }
//    private val photoStyles: List<View> by lazy {
//        listOf(
//            binding.photoStyleBlack,
//            binding.photoStyleWhite,
//            binding.photoStyleBlue,
//            binding.photoStyleGreen,
//            binding.photoStyleRose,
//            binding.photoStyleYellow
//        )
//    }


    private val rationaleDialog: AlertDialog by lazy {
        val dBinding = RationaleDialogLayoutBinding.inflate(layoutInflater, null, false)
        val view = dBinding.root
        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
        dBinding.allowButton.setOnClickListener { requestStoragePermission() }
        dBinding.denyButton.setOnClickListener { dialog.dismiss() }

        dialog
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDownloadQuoteBinding.bind(view)
        setHasOptionsMenu(true)
        setupClickListeners()
        setupUi()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        colorAdapter.onColorBoxClickedListener = {view,color->
            view.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.scale_anim))
            binding.photoContainer.setBackgroundColor(Color.parseColor(color))
            if (photoStyleColors.indexOf(color) == 0) {
                binding.quoteText.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorPrimaryOnDark
                    )
                )
                binding.quoteAuthorText.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorSecondaryOnDark
                    )
                )
            } else {
                binding.quoteText.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorPrimaryOnLight
                    )
                )
                binding.quoteAuthorText.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorSecondaryOnLight
                    )
                )
            }
        }
        binding.colorRecyclerView.apply {
            adapter = colorAdapter
            layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        }

    }

    private fun requestStoragePermission() =
        requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)

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
            quoteAuthorText.text = "-From \"${args.quoteAuthor}\" book "
        }
    }

    private fun setupClickListeners() {
//        photoStyles.forEachIndexed { i: Int, view: View ->
//            view.setOnClickListener {
//                it.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.scale_anim))
//                binding.photoContainer.setBackgroundColor(Color.parseColor(photoStyleColors[i]))
//                if (i == 0) {
//                    binding.quoteText.setTextColor(
//                        ContextCompat.getColor(
//                            requireContext(),
//                            R.color.colorPrimaryOnDark
//                        )
//                    )
//                    binding.quoteAuthorText.setTextColor(
//                        ContextCompat.getColor(
//                            requireContext(),
//                            R.color.colorSecondaryOnDark
//                        )
//                    )
//                } else {
//                    binding.quoteText.setTextColor(
//                        ContextCompat.getColor(
//                            requireContext(),
//                            R.color.colorPrimaryOnLight
//                        )
//                    )
//                    binding.quoteAuthorText.setTextColor(
//                        ContextCompat.getColor(
//                            requireContext(),
//                            R.color.colorSecondaryOnLight
//                        )
//                    )
//                }
//            }
//        }
        binding.textSizeSlider.addOnChangeListener { slider, value, fromUser ->
            binding.sliderValueText.text = value.toInt().toString()
            binding.quoteText.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
        }
    }

    private fun takeScreenshot() {
        val view = binding.photoContainer
        photoBitmap = Bitmap.createBitmap(
            view.width,
            view.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(photoBitmap!!)
        view.draw(canvas)

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestStoragePermission()
        } else {
            saveMediaToStorage(photoBitmap!!)
        }
    }

    private fun saveMediaToStorage(bitmap: Bitmap) {
        imageFileName = "quote-image-${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requireContext().contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, imageFileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                imageUri =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, imageFileName)
            fos = FileOutputStream(image)
            imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().applicationContext.packageName + ".provider",
                    image
                )
            } else {
                image.toUri()
            }

        }
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            showToast("Downloaded image successfully !")
            showImageDownloadedNotification()
        }
    }

    private fun showImageDownloadedNotification() {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.setDataAndType(imageUri, "image/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        val pendingIntent = PendingIntent.getActivity(requireContext(), 0, intent, 0)

        val notification = if (Build.VERSION.SDK_INT > 26) {
            NotificationCompat.Builder(requireContext(), "download_image")
                .setContentTitle("Downloaded Image")
                .setContentText(imageFileName)
                .setSmallIcon(R.drawable.ic_download_complete)
                .setContentIntent(pendingIntent)
                .build()
        } else {
            NotificationCompat.Builder(requireContext())
                .setContentTitle("Downloaded Image")
                .setContentText(imageFileName)
                .setSmallIcon(R.drawable.ic_download_complete)
                .setContentIntent(pendingIntent)
                .build()
        }

        val notificationManager =
            requireActivity().getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "download_image",
                "download_image_channel",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(5, notification)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveMediaToStorage(photoBitmap!!)
        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                rationaleDialog.show()
            } else {
                showToast("Image not saved,because of denial of permission")
            }
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