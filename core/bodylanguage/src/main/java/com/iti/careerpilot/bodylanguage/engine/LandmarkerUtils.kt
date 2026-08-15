package com.iti.careerpilot.bodylanguage.engine

import android.util.Log
import com.google.mediapipe.tasks.core.Delegate

internal inline fun <T> safeCreateLandmarker(
    tag: String = "LandmarkerEngine",
    createBlock: (Delegate) -> T,
): T? {
    return try {
        createBlock(Delegate.GPU)
    } catch (e: Exception) {
        Log.w(tag, "GPU delegate failed, falling back to CPU", e)
        try {
            createBlock(Delegate.CPU)
        } catch (e2: Exception) {
            Log.e(tag, "CPU initialization also failed", e2)
            null
        }
    }
}
