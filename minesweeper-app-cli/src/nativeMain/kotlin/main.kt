import se.dorne.minesweeper.gameengine.*
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val (numberOfColumns, numbersOfRows, numberOfMines) = when {
        args.size != 3 -> {
            // println(
            //     """
            //     usage: minesweeper <rows> <columns> <mines>
            // """.trimIndent()
            // )
            // exitProcess(1)
            arrayOf("9", "9", "10")
        }

        else -> args
    }
    var board = Board.initialBoard(
        columns = numberOfColumns.toInt(),
        rows = numbersOfRows.toInt(),
        numberOfMines = numberOfMines.toInt(),
    )
    while (true) {
        board.display(showEverything = false)
        val row = askForRow(board)
        val column = askForColumn(board)
        val action = askForAction()
        board = board.play(action, row, column)
        when (val state = board.gameSate()) {
            is GameState.Finished -> {
                when (state.outcome) {
                    Outcome.WON -> println("You won!\n\n")
                    Outcome.LOST -> println("You lost!\n\n")
                }
                board.display(showEverything = true)
                exitProcess(0)
            }

            GameState.Ongoing -> {}
        }
    }
}

private fun askForRow(board: Board): Int {
    while (true) {
        print("Row number [0-${board.rows}]: ")
        val row = readln().toIntOrNull()?.minus(1).takeIf { it in 0..<board.rows }
        if (row != null) {
            return row
        } else {
            println("\n⚠\uFE0F row must be an int between 1 and ${board.rows + 1}")
            // continue
        }
    }
}

private fun askForColumn(board: Board): Int {
    while (true) {
        print("Column number [0-${board.columns}]: ")
        val col = readln().toIntOrNull()?.minus(1).takeIf { it in 0..<board.columns }
        if (col != null) {
            return col
        } else {
            println("\n⚠\uFE0F column must be an int between 1 and ${board.columns + 1}")
            // continue
        }
    }
}

private fun askForAction(): Action {
    while (true) {
        val actions = Action.entries.joinToString("\n") { " - ${it.toChar()}: ${it.toDescription()}" }
        val possibleChars = Action.entries.map { it.toChar() }
        println("Possible actions:\n$actions")
        print("Action [${possibleChars.joinToString("/")}]: ")
        val actionInputChar = readln().singleOrNull()
        val action = Action.entries.find { it.toChar() == actionInputChar }
        if (action != null) {
            return action
        } else {
            println("\n⚠\uFE0F action must be one of the following allowed char: $possibleChars")
            // continue
        }
    }
}

fun Action.toChar() = when (this) {
    Action.REVEAL -> 'R'
    Action.MARK_AS_MINE -> 'M'
}

fun Action.toDescription() = when (this) {
    Action.REVEAL -> "reveal cell"
    Action.MARK_AS_MINE -> "mark or unmark cell as mine"
}

internal fun Board.display(showEverything: Boolean) {
    val padRows = calculatePadding(rows)
    val padColumns = calculatePadding(columns)

    val prefix = " ".repeat(padRows + 1) // +1 because of space before row start
    val columnNumbers = (1..columns).joinToString(" ") { "$it".padStart(padColumns, ' ') }
    val columnHeader = "$prefix$columnNumbers"

    val game = cells.mapIndexed { index, cell ->
        val state = if (showEverything) cell.reveal(this, safely = true).state else cell.state
        val c = state.text(showEverything).padStart(padColumns, ' ')
        if (index % columns == 0) {
            val rowCount = ((index / columns) + 1).toString().padStart(padRows, ' ')
            "\n$rowCount $c"
        } else {
            c
        }
    }.joinToString(" ")
    println(columnHeader + game)
}

private fun calculatePadding(x: Int): Int {
    var xx = x
    var pad = 0
    do {
        xx /= 10
        pad++
    } while (xx != 0)
    return pad
}

private fun CellState.text(showEverything: Boolean) = when (this) {
    is CellState.MarkedAsMine -> if (showEverything && rightfullyMarkedAsMine()) "Y" else "X"
    CellState.Untouched -> "."
    CellState.Empty -> " "
    is CellState.Numbered -> "$surroundingMineCount"
    CellState.RevealedMine -> "⁂"
    CellState.UnrevealedMine -> if (showEverything) "¤" else "."
}
