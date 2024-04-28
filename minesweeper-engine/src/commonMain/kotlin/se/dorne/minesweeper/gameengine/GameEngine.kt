package se.dorne.minesweeper.gameengine

import kotlin.math.abs
import kotlin.random.Random

enum class Action {
    REVEAL, MARK_AS_MINE,
}

class Board(
    val columns: Int,
    val rows: Int,
    val cells: List<Cell>,
) {
    fun play(action: Action, line: Int, column: Int) = play(action, cellIndex(line, column))

    fun play(action: Action, cellIndex: Int) = when (action) {
        Action.REVEAL -> reveal(cellIndex)
        Action.MARK_AS_MINE -> markOrUnmarkAsMine(cellIndex)
    }

    private fun cellIndex(line: Int, column: Int) = (line * columns) + column

    private fun markOrUnmarkAsMine(cellIndex: Int): Board {
        val oldCell = cells[cellIndex]
        val cell = when {
            oldCell.state is CellState.MarkedAsMine -> oldCell.unmarkedAsMine()
            else -> oldCell.markAsMine()
        }
        return fromBoard(this, listOf(cell))
    }

    private fun reveal(cellIndex: Int): Board {
        val toReveal = ArrayDeque(listOf(cells[cellIndex]))
        val revealed = mutableSetOf<Cell>()
        var safely = false
        while (toReveal.isNotEmpty()) {
            val current = toReveal.removeFirst()
            if (!revealed.any { current.index == it.index }) {
                val rev = current.reveal(this, safely)
                safely = true
                revealed.add(rev)
                if (rev.state is CellState.Empty) {
                    toReveal.addAll(rev.neighbours(this))
                }
            }
        }
        return fromBoard(this, revealed)
    }

    companion object {
        fun fromBoard(board: Board, newCells: Iterable<Cell>): Board {
            val cellByIndex = newCells.associateBy { it.index }
            return Board(
                columns = board.columns,
                rows = board.rows,
                cells = board.cells.mapIndexed { index, cell ->
                    cellByIndex[index] ?: cell
                },
            )
        }

        fun initialBoard(columns: Int, rows: Int, numberOfMines: Int): Board {
            val mineIndices = buildSet(numberOfMines) {
                while (size < numberOfMines) {
                    add(Random.nextInt(0, columns * rows))
                }
            }
            return Board(
                columns,
                rows,
                MutableList(columns * rows) { index ->
                    when {
                        mineIndices.contains(index) -> Cell(index, CellState.UnrevealedMine)
                        else -> Cell(index, CellState.Untouched)
                    }
                },
            )
        }
    }

    val mines by lazy { cells.filter { it.state.isMine() } }

    fun gameSate(): GameState = when {
        mines.any { it.state is CellState.RevealedMine } -> GameState.Finished(Outcome.LOST)
        cells.all { it.state.alreadyRevealed() || it.state.rightfullyMarkedAsMine() || it.state.isMine() } -> GameState.Finished(
            Outcome.WON
        )

        else -> GameState.Ongoing
    }
}

sealed class GameState {
    data object Ongoing : GameState()
    class Finished(val outcome: Outcome) : GameState()
}

enum class Outcome {
    WON, LOST
}

sealed class CellState {
    data object Untouched : CellState()
    data object Empty : CellState()
    data object UnrevealedMine : CellState()
    data object RevealedMine : CellState()
    class Numbered(val surroundingMineCount: Int) : CellState()
    class MarkedAsMine(val was: CellState) : CellState()

    fun alreadyRevealed() = this is RevealedMine || this is Numbered || this is Empty
    fun isMine() =
        this is UnrevealedMine || this is RevealedMine || (this is MarkedAsMine && (this.was is UnrevealedMine || this.was is RevealedMine))

    fun rightfullyMarkedAsMine() = this is MarkedAsMine && this.was is UnrevealedMine
}

data class Cell(
    val index: Int,
    val state: CellState = CellState.Untouched,
) {
    fun reveal(board: Board, safely: Boolean) = when {
        state.alreadyRevealed() -> this
        state.isMine() && safely -> this
        state.isMine() && !safely -> asState(CellState.RevealedMine)
        neighbourMinesCount(board) > 0 -> asState(CellState.Numbered(neighbourMinesCount(board)))
        else -> asState(CellState.Empty)
    }

    private fun asState(state: CellState) = Cell(index, state)
    internal fun markAsMine(): Cell {
        if (state.alreadyRevealed()) error("cannot mark already revealed cell")
        return asState(CellState.MarkedAsMine(was = this.state))
    }

    internal fun unmarkedAsMine(): Cell {
        if (state !is CellState.MarkedAsMine) error("cell was not marked as mine")
        return asState(state.was)
    }

    private fun rowNumberIn(board: Board) = index / board.columns
    private fun columnNumberIn(board: Board) = index % board.columns

    private enum class Distances(private val lineDist: Int, private val colDist: Int) {
        TOP_LEFT(-1, -1),
        TOP(-1, 0),
        TOP_RIGHT(-1, 1),
        RIGHT(0, 1),
        BOTTOM_RIGHT(1, 1),
        BOTTOM(1, 0),
        BOTTOM_LEFT(1, -1),
        LEFT(0, -1);

        fun toIndexDistance(numberOfColumns: Int) = lineDist * numberOfColumns + colDist
    }

    internal fun neighbours(board: Board) = listOf(
        Distances.TOP_LEFT,
        Distances.TOP,
        Distances.TOP_RIGHT,
        Distances.RIGHT,
        Distances.BOTTOM_RIGHT,
        Distances.BOTTOM,
        Distances.BOTTOM_LEFT,
        Distances.LEFT,
    ).mapNotNull { distance ->
        board.cells.getOrNull(index + distance.toIndexDistance(board.columns))
    }.filter { supposedNeighbour ->
        this.isNeighboursOf(supposedNeighbour, inBoard = board)
    }.toSet()

    // simplified euclidean distance <= 1
    private fun Cell.isNeighboursOf(c2: Cell, inBoard: Board): Boolean =
        abs(rowNumberIn(inBoard) - c2.rowNumberIn(inBoard)) <= 1 && abs(
            columnNumberIn(inBoard) - c2.columnNumberIn(inBoard)
        ) <= 1

    private fun neighbourMinesCount(board: Board) =
        neighbours(board).map { it.index }.intersect(board.mines.map { it.index }.toSet()).size
}
