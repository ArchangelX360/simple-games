plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    macosArm64 {
        binaries {
            executable {
                entryPoint = "main"
                runTask?.standardInput = System.`in`
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":minesweeper-engine"))
            }
        }
    }
}
