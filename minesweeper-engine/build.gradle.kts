import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()
    macosArm64()
    macosX64()
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
}
