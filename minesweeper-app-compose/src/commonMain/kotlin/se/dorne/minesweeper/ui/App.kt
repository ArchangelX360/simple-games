package se.dorne.minesweeper.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import se.dorne.minesweeper.gameengine.*

private val CELL_SIZE = 48.dp

@Composable
fun minesweeperApp() {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme(),
    ) {
        var board by remember { mutableStateOf<Board?>(null) }
        Surface {
            Column(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "Minesweeper",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 5.dp)
                )
                when (val b = board) {
                    null -> {
                        minesweeperBoardInitializer { columns, rows, mines ->
                            board = Board.initialBoard(columns, rows, mines)
                        }
                    }

                    else -> {
                        when (val gameState = b.gameSate()) {
                            is GameState.Finished -> {
                                endOfGame(gameState.outcome, b, onRestart = { board = null })
                            }

                            GameState.Ongoing -> {
                                minesweeperBoard(b, false) { cell, action ->
                                    board = b.play(action, cell.index)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun endOfGame(outcome: Outcome, board: Board, onRestart: () -> Unit) {
    var showDialog by remember { mutableStateOf(true) }
    minesweeperBoard(board, true)
    if (showDialog) {
        endOfGameDialog(
            outcome,
            onDismiss = { showDialog = false },
            onRestart = onRestart,
        )
    } else {
        restartGameButton(modifier = Modifier.padding(top = 5.dp), onRestart)
    }
}

@Composable
fun endOfGameDialog(outcome: Outcome, onDismiss: () -> Unit, onRestart: () -> Unit) {
    val dialogText = when (outcome) {
        Outcome.WON -> "You won!"
        Outcome.LOST -> "You lost!"
    }
    AlertDialog(
        title = { Text(text = "End of game") },
        text = { Text(text = dialogText) },
        onDismissRequest = onDismiss,
        confirmButton = { restartGameButton(onRestart = onRestart) },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("See board")
            }
        },
    )
}

@Composable
fun restartGameButton(modifier: Modifier = Modifier, onRestart: () -> Unit) {
    Button(
        modifier = modifier,
        onClick = onRestart,
    ) {
        Text("Restart game")
    }
}

@Composable
fun minesweeperBoard(
    board: Board,
    showEverything: Boolean,
    onCellClick: (cell: Cell, action: Action) -> Unit = { _, _ -> },
) {
    LazyVerticalGrid(
        modifier = Modifier.width(board.columns * CELL_SIZE).height(board.rows * CELL_SIZE),
        columns = GridCells.Fixed(board.columns),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.Center,
    ) {
        items(board.cells) { cell ->
            minesweeperCell(cell, board, showEverything, onCellClick)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun minesweeperCell(
    cell: Cell,
    board: Board,
    showEverything: Boolean,
    onCellClick: (cell: Cell, action: Action) -> Unit,
) {
    val state = if (showEverything) cell.reveal(board, safely = true).state else cell.state
    Surface(
        shape = RectangleShape,
        border = ButtonDefaults.outlinedButtonBorder,
        color = state.color(showEverything),
        modifier = Modifier.size(CELL_SIZE).combinedClickable(
            enabled = !showEverything && !cell.state.alreadyRevealed(),
            onLongClick = { onCellClick(cell, Action.MARK_AS_MINE) },
            onClick = { onCellClick(cell, Action.REVEAL) },
        ).onClick(
            // if devices can use a mouse right click
            matcher = PointerMatcher.mouse(PointerButton.Secondary),
            onClick = { onCellClick(cell, Action.MARK_AS_MINE) }),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(state.text(showEverything))
        }
    }
}

@Composable
private fun CellState.color(showEverything: Boolean) = when (this) {
    is CellState.MarkedAsMine -> when {
        showEverything && rightfullyMarkedAsMine() -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }

    CellState.Untouched -> MaterialTheme.colorScheme.primaryContainer
    CellState.Empty -> MaterialTheme.colorScheme.secondaryContainer
    is CellState.Numbered -> MaterialTheme.colorScheme.inversePrimary
    CellState.RevealedMine -> MaterialTheme.colorScheme.errorContainer
    CellState.UnrevealedMine -> when {
        showEverything -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }
}

private fun CellState.text(showEverything: Boolean) = when (this) {
    is CellState.MarkedAsMine -> if (showEverything && rightfullyMarkedAsMine()) "❗" else "❌"
    CellState.Untouched -> " "
    CellState.Empty -> " "
    is CellState.Numbered -> "$surroundingMineCount"
    CellState.RevealedMine -> "\uD83D\uDCA5"
    CellState.UnrevealedMine -> if (showEverything) "\uD83D\uDCA3" else " "
}

private enum class GameType(val description: String, val rows: Int, val columns: Int, val mines: Int) {
    BEGINNER("Beginner", 9, 9, 10),
    INTERMEDIATE("Intermediate", 16, 16, 40),
    EXPERT("Expert", 16, 30, 99),
}

@Composable
fun minesweeperBoardInitializer(onValidation: (columns: Int, rows: Int, mines: Int) -> Unit) {
    var type by remember { mutableStateOf(GameType.BEGINNER) }
    Column(Modifier.fillMaxWidth(0.5f), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Select the type of game you want to play:", style = MaterialTheme.typography.bodyLarge)
        Column(horizontalAlignment = Alignment.Start) {
            GameType.entries.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = type == it,
                        onClick = { type = it },
                    )
                    Text("${it.rows}x${it.columns} ${it.mines} mines (${it.description})")
                }
            }
        }
        Button(
            content = { Text("Start game") },
            onClick = { onValidation(type.columns, type.rows, type.mines) },
        )
    }
}
