package se.dorne.minesweeper.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import se.dorne.minesweeper.ui.minesweeperApp

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        minesweeperApp()
    }
}
