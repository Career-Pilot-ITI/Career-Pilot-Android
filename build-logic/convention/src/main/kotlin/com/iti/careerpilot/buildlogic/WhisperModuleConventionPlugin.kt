package com.iti.careerpilot.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class WhisperModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("careerpilot.android.library")
        pluginManager.apply("careerpilot.android.hilt")
        pluginManager.apply("careerpilot.testing")

        extensions.configure<LibraryExtension> {
            namespace = "com.iti.careerpilot.whisper"

            packaging {
                jniLibs.pickFirsts.add("**/libonnxruntime.so")
                jniLibs.pickFirsts.add("**/libsherpa-onnx-jni.so")
            }
        }

        addLibrary("implementation", "sherpa-onnx")
    }
}
