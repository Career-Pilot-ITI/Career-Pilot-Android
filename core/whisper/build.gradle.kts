plugins {
    alias(libs.plugins.careerpilot.module.whisper)
}


dependencies {
    implementation(files("libs/sherpa-onnx-1.13.4.aar"))
}
