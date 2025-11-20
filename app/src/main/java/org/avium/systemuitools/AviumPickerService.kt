/*
 * Copyright (C) 2025 The AviumUI Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.avium.systemuitools

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LifecycleService
import org.avium.systemuitools.compose.FloatingWindowManager
import kotlin.random.Random

class AviumPickerService : LifecycleService() {

    private val TAG = "AviumPickerDebug"
    private lateinit var floatingManager: FloatingWindowManager

    private val handler = Handler(Looper.getMainLooper())
    private var pollRunnable: Runnable? = null
    private var pollAttempts = 0

    override fun onCreate() {
        super.onCreate()
        floatingManager = FloatingWindowManager(this) { stopSelf() }
        floatingManager.show()
        handler.postDelayed(::startCaptureProcess, 300)
    }

    @SuppressLint("PrivateApi")
    private fun startCaptureProcess() {
        try {
            Log.d(TAG, "[LOG 1] Starting capture process...")

            val serviceManagerClass = Class.forName("android.os.ServiceManager")
            val getServiceMethod = serviceManagerClass.getMethod("getService", String::class.java)
            val windowManagerBinder = getServiceMethod.invoke(null, "window") as IBinder
            val iWindowManagerStubClass = Class.forName("android.view.IWindowManager\$Stub")
            val asInterfaceMethod = iWindowManagerStubClass.getMethod("asInterface", IBinder::class.java)
            val windowManagerService = asInterfaceMethod.invoke(null, windowManagerBinder)

            val requestId = Random.nextInt(1, Int.MAX_VALUE)
            Log.d(TAG, "[LOG 2] Generated Request ID: $requestId")

            val iWindowManagerClass = Class.forName("android.view.IWindowManager")
            val captureMethod = iWindowManagerClass.getMethod("captureFocusedWindowDrawables", Int::class.javaPrimitiveType)
            Log.d(TAG, "[LOG 3] Invoking captureFocusedWindowDrawables...")
            captureMethod.invoke(windowManagerService, requestId)
            startPollingForResult(windowManagerService, requestId)

        } catch (e: Exception) {
            Log.e(TAG, "[LOG E] CRITICAL FAILURE during capture process", e)
            floatingManager.updateImages(emptyList())
        }
    }

    private fun startPollingForResult(wms: Any, requestId: Int) {
        pollAttempts = 0
        pollRunnable = Runnable {
            try {
                pollAttempts++
                Log.d(TAG, "[LOG 4] Polling... Attempt #$pollAttempts")

                val iWindowManagerClass = Class.forName("android.view.IWindowManager")
                val checkMethod = iWindowManagerClass.getMethod("checkCaptureResult", Int::class.javaPrimitiveType)

                @Suppress("UNCHECKED_CAST")
                val result = checkMethod.invoke(wms, requestId) as? List<Bitmap>

                if (result != null) {
                    Log.d(TAG, "[LOG 5] >>> SUCCESS! Got result with ${result.size} images. <<<")
                    floatingManager.updateImages(result)
                    stopPolling()
                } else if (pollAttempts > 20) {
                    Log.w(TAG, "[LOG W] Polling timed out after 5 seconds.")
                    floatingManager.updateImages(emptyList())
                    stopPolling()
                } else {
                    handler.postDelayed(pollRunnable!!, 250)
                }
            } catch (e: Exception) {
                Log.e(TAG, "[LOG E] CRITICAL FAILURE during polling", e)
                stopPolling()
            }
        }
        handler.post(pollRunnable!!)
    }

    private fun stopPolling() {
        pollRunnable?.let { handler.removeCallbacks(it) }
        pollRunnable = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPolling()
        floatingManager.remove()
    }
}