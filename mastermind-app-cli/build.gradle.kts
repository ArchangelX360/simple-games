import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
    jvm {
        mainRun {
            mainClass = "se.dorne.mastermind.app.ApplicationKt"
            tasks.named<JavaExec>("jvmRun") {
                standardInput = System.`in`
            }
        }
    }

    sourceSets {
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
