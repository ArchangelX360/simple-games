import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
}

version = "1.0.0"

kotlin {
    jvm("desktop") {
        // This is the configuration for `desktopRun` commmand, instead, run it with Compose `run` 
        // mainRun {
        //     mainClass = "se.dorne.minesweeper.app.ApplicationKt"
        // }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "minesweeper-app"
        browser {
            commonWebpackConfig {
                outputFileName = "$moduleName.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(project.projectDir.path)
                        add(project.projectDir.path + "/commonMain/")
                        add(project.projectDir.path + "/wasmJsMain/")
                        add(project.projectDir.path + "/composeCommonMain/")
                    }
                }
            }
        }
        binaries.executable()
    }


    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":minesweeper-engine"))
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

compose {
    desktop {
        application {
            mainClass = "se.dorne.minesweeper.app.ApplicationKt"

            nativeDistributions {
                targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
                packageName = "Minesweeper"
                packageVersion = project.version as? String
            }
        }
    }

    experimental {
        web.application {}
    }
}
