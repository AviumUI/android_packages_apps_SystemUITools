package org.avium.systemuitools.compose

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import org.avium.systemuitools.ImageSaver
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import org.avium.systemuitools.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class FloatingWindowManager(
    private val context: Context,
    private val onSaveCompleted: () -> Unit
) {
    private var windowManager: WindowManager? = null
    private var floatingPickerView: View? = null

    private val _images = mutableStateOf<List<Bitmap>>(emptyList())
    private val _selectedImages = mutableStateOf<List<Bitmap>>(emptyList())
    private val _isLoading = mutableStateOf(true)

    private inner class FloatingWindowLifeCycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
        private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
        private val viewModelStoreInstance: ViewModelStore = ViewModelStore()
        private val savedStateRegistryController: SavedStateRegistryController = SavedStateRegistryController.create(this)

        init {
            savedStateRegistryController.performRestore(null)
        }

        override val lifecycle: Lifecycle
            get() = lifecycleRegistry

        override val viewModelStore: ViewModelStore
            get() = viewModelStoreInstance

        override val savedStateRegistry: SavedStateRegistry
            get() = savedStateRegistryController.savedStateRegistry

        fun handleLifecycleEvent(event: Lifecycle.Event) {
            lifecycleRegistry.handleLifecycleEvent(event)
        }
    }

    private var floatingWindowLifeCycleOwner: FloatingWindowLifeCycleOwner? = null

    init {
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
    }

    fun show() {
        val wm = windowManager ?: return

        if (floatingPickerView != null) {
            Log.w("FloatingWindowManager", "Floating window is already shown.")
            return
        }

        val lifeCycleOwner = FloatingWindowLifeCycleOwner()
        floatingWindowLifeCycleOwner = lifeCycleOwner

        val container = FrameLayout(context)

        val composeView = ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        }

        container.addView(composeView)

        container.setViewTreeLifecycleOwner(lifeCycleOwner)
        container.setViewTreeViewModelStoreOwner(lifeCycleOwner)
        container.setViewTreeSavedStateRegistryOwner(lifeCycleOwner)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = 50
        }

        try {
            wm.addView(container, params)
            floatingPickerView = container

            lifeCycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            lifeCycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)

            composeView.setContent {
                FloatingPickerUI(
                    images = _images,
                    selectedImages = _selectedImages,
                    isLoading = _isLoading,
                    onImageClick = { bitmap ->
                        _selectedImages.value = if (_selectedImages.value.contains(bitmap)) {
                            _selectedImages.value.filterNot { it === bitmap }
                        } else {
                            _selectedImages.value + bitmap
                        }
                    },
                    onSaveClick = {
                        ImageSaver.saveBitmaps(context, _selectedImages.value)
                        onSaveCompleted()
                        remove()
                    },
                    onClose = {
                        onSaveCompleted()
                        remove()
                    },
                    onShareClick = {
                        shareImages(_selectedImages.value)
                    }
                )
            }
        } catch (e: Exception) {
            Log.e("FloatingWindowManager", "Failed to add floating window", e)
            floatingWindowLifeCycleOwner = null
        }
    }

    fun remove() {
        val wm = windowManager
        val view = floatingPickerView
        val lifeCycleOwner = floatingWindowLifeCycleOwner

        if (wm != null && view != null && view.isAttachedToWindow) {
            try {
                lifeCycleOwner?.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                lifeCycleOwner?.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                wm.removeView(view)
                floatingPickerView = null
                floatingWindowLifeCycleOwner = null
            } catch (e: Exception) {
                Log.e("FloatingWindowManager", "Error removing floating window", e)
            }
        } else if (view == null || !view.isAttachedToWindow) {
            Log.d("FloatingWindowManager", "Floating window view is null or not attached.")
        } else {
            Log.e("FloatingWindowManager", "WindowManager is null, cannot remove floating window.")
        }
    }

    fun updateImages(bitmaps: List<Bitmap>) {
        _images.value = bitmaps
        _isLoading.value = false
    }

    private fun shareImages(images: List<Bitmap>) {
        if (images.isEmpty()) {
            Log.d("FloatingWindowManager", "No images selected for sharing")
            return
        }
        
        try {
            val imageUris = ArrayList<Uri>()

            for (bitmap in images) {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val imageFileName = "JPEG_$timeStamp.jpg"
                val cacheDir = context.externalCacheDir ?: context.cacheDir
                val imageFile = File(cacheDir, imageFileName)

                FileOutputStream(imageFile).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }

                val contentUri = FileProvider.getUriForFile(
                    context,
                    "org.avium.systemuitools.fileprovider",
                    imageFile
                )
                imageUris.add(contentUri)
            }

            val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
            shareIntent.type = "image/*"
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            val chooserIntent = Intent.createChooser(shareIntent, context.getString(R.string.share))
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            Log.e("FloatingWindowManager", "Error sharing images", e)
        }
    }
}
