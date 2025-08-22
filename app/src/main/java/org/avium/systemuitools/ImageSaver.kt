package org.avium.systemuitools

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import java.util.UUID

object ImageSaver {
    fun saveBitmaps(context: Context, bitmaps: List<Bitmap>) {
        if (bitmaps.isEmpty()) return

        val resolver = context.contentResolver
        bitmaps.forEach { bitmap ->
            val fileName = "AviumPicker_${UUID.randomUUID()}"
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/AviumPicker")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                try {
                    resolver.openOutputStream(it)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(it, values, null, null)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}