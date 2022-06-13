package com.nesib.quotegram.ui.main.fragments.download

import android.app.*
import android.app.Activity.RESULT_OK
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
import android.util.Log
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
import coil.load
import com.nesib.quotegram.R
import com.nesib.quotegram.adapters.ColorBoxAdapter
import com.nesib.quotegram.base.BaseFragment
import com.nesib.quotegram.databinding.FragmentDownloadQuoteBinding
import com.nesib.quotegram.databinding.RationaleDialogLayoutBinding
import com.nesib.quotegram.ui.custom_views.SingleSelectBottomView
import com.nesib.quotegram.utils.*
import com.unsplash.pickerandroid.photopicker.UnsplashPhotoPicker
import com.unsplash.pickerandroid.photopicker.data.UnsplashPhoto
import com.unsplash.pickerandroid.photopicker.presentation.UnsplashPickerActivity
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import javax.inject.Inject

@AndroidEntryPoint
class DownloadQuoteFragment :
    BaseFragment<FragmentDownloadQuoteBinding>() {
    private var quoteBackgroundUrl: String? = null
    private var photoBitmap: Bitmap? = null
    private var imageUri: Uri? = null
    private var imageFileName: String? = ""
    private val args by navArgs<DownloadQuoteFragmentArgs>()
    private val photoStyleColors =
        listOf("#1B1B1B", "#F2F2F2", "#DCEBFE", "#C7FFCE", "#FFD1EA", "#FFF9AB")
    private val colorAdapter by lazy { ColorBoxAdapter(photoStyleColors) }

    companion object {
        const val REQUEST_PICK_IMAGE = 123
    }

    @Inject
    lateinit var imagePicker: UnsplashPhotoPicker

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
        this.quoteBackgroundUrl = args.quote?.backgroundUrl
        setHasOptionsMenu(true)
        setupClickListeners()
        setupUi()
        setupRecyclerView()
    }

    private fun setupRecyclerView() = with(binding) {
        colorAdapter.onColorBoxClickedListener = { view, color ->
            ivQuoteBg.setImageDrawable(null)
            quoteOverlay.safeGone()
            view.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.scale_anim))
            photoContainer.setBackgroundColor(Color.parseColor(color))
            if (photoStyleColors.indexOf(color) == 0) {
                quoteText.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorPrimaryOnDark
                    )
                )
            } else {
                quoteText.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
            }
        }
        binding.rvColors.apply {
            adapter = colorAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }

    }

    private fun requestStoragePermission() =
        requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)

    private fun setupUi() = with(binding) {
        val quote = args.quote ?: return@with
        if (quote.quote == null) return@with
        textSizeSlider.value = 18f
        when (quote.quote?.length) {
            in 181..239 -> {
                textSizeSlider.valueTo = 18.toFloat()
                textSizeSlider.value = 16f
            }
            in 240..300 -> {
                textSizeSlider.valueTo = 16.toFloat()
                textSizeSlider.value = 14f
            }
            in 300..Constants.MAX_QUOTE_LENGTH -> {
                textSizeSlider.valueTo = 14.toFloat()
                textSizeSlider.value = 12f
            }
        }
        if ((quote.backgroundUrl != null && quote.backgroundUrl != "") || quoteBackgroundUrl != null) {
            initQuoteBackground()
            quoteOverlay.visible()
            quoteText.setTextColor(getColor(R.color.gray_300))
            ivQuoteSelectedBg.setOnClickListener { view ->
                view.startAnimation(
                    AnimationUtils.loadAnimation(
                        requireContext(),
                        R.anim.scale_anim
                    )
                )
                ivQuoteBg.load(quoteBackgroundUrl)
                quoteText.setTextColor(getColor(R.color.gray_300))
                quoteOverlay.safeVisible()
            }
        } else {
            ivQuoteSelectedBg.gone()
            quoteOverlay.safeGone()
        }
        quoteText.text = quote.quote
    }

    private fun setupClickListeners() = with(binding) {
        singleSelectBottomView.onTextClick = {
            onSelectBottomNavItemClicked(SingleSelectBottomView.Item.Text)
        }
        singleSelectBottomView.onImageClick = {
            onSelectBottomNavItemClicked(SingleSelectBottomView.Item.Image)
        }
        singleSelectBottomView.onColorClick = {
            onSelectBottomNavItemClicked(SingleSelectBottomView.Item.Color)
        }
        ibCloseStyle.setOnClickListener {
            singleSelectBottomView.setState(SingleSelectBottomView.Item.None)
            resetStyleOptionsVisibility()
        }

        singleSelectBottomView.onSameItemClick = {
            resetStyleOptionsVisibility()
        }
        textSizeSlider.addOnChangeListener { slider, value, fromUser ->
            quoteText.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
        }

        changeBg.setOnClickListener {
            startActivityForResult(
                UnsplashPickerActivity.getStartingIntent(
                    requireContext(),
                    false
                ),
                REQUEST_PICK_IMAGE
            )
        }
    }

    private fun onSelectBottomNavItemClicked(item: SingleSelectBottomView.Item) = with(binding) {
        resetStyleOptionsVisibility()
        when (item) {
            SingleSelectBottomView.Item.Color -> {
                styleContainer.visible()
                rvColors.visible()
                tvHeader.text = "Change background color"
            }
            SingleSelectBottomView.Item.Image -> {
                styleContainer.visible()
                tvHeader.text = "Change background image"
                llImageChooser.visible()
            }
            SingleSelectBottomView.Item.Text -> {
                styleContainer.visible()
                tvHeader.text = "Change text size"
                textSizeSlider.visible()
            }
            SingleSelectBottomView.Item.None -> {}
        }
    }

    private fun resetStyleOptionsVisibility() = with(binding) {
        llImageChooser.safeGone()
        rvColors.safeGone()
        textSizeSlider.safeGone()
        styleContainer.safeGone()
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
            showToast(getString(R.string.image_downloaded_successfully))
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
                .setContentTitle(getString(R.string.image_downloaded))
                .setContentText(imageFileName)
                .setSmallIcon(R.drawable.ic_download_complete)
                .setContentIntent(pendingIntent)
                .build()
        } else {
            NotificationCompat.Builder(requireContext())
                .setContentTitle(getString(R.string.image_downloaded))
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
            rationaleDialog.dismiss()
            saveMediaToStorage(photoBitmap!!)
        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                rationaleDialog.show()
            } else {
                showToast(getString(R.string.image_access_denied))
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK) {
            val images =
                data?.getParcelableArrayListExtra<UnsplashPhoto>(UnsplashPickerActivity.EXTRA_PHOTOS)
            if (images != null && images.size > 0) {
                quoteBackgroundUrl = images[0].urls.small
                binding.ivQuoteSelectedBg.visible()
                initQuoteBackground()
            }
        }
    }

    private fun initQuoteBackground() = with(binding) {
        ivQuoteBg.load(quoteBackgroundUrl)
        ivQuoteSelectedBg.load(quoteBackgroundUrl)
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDownloadQuoteBinding =
        FragmentDownloadQuoteBinding.inflate(inflater, container, false)


}