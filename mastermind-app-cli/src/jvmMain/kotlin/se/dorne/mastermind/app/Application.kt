package se.dorne.mastermind.app
       
import kotlin.random.Random

private const val MAX_ATTEMPS = 12

fun main() {
    var board = Board.randomlyGenerated()
    var attemp = 1
    while (true) {
        println(Marker.legend())
        println(ColorBulb.legend())
        val combinaison = askForCombinaison()
        board = play(board, combinaison)
        print("\u001Bc")
        println(board.display())

        if (board.victorious) {
            println("Won in $attemp attemps!")
            return
        }
        attemp++
        if (attemp > MAX_ATTEMPS) {
            println("Lost! Solution was: ${board.solution.display()}")
            return
        }
    }
}

private fun askForCombinaison(): List<ColorBulb> {
    while (true) {
        print("Enter a 4 digits combinaison: ")
        val digits = readln().split("", limit = 5).drop(1).map { it.toIntOrNull() }
        when {
            digits.any { it !in 1..ColorBulb.entries.size } -> println(
                "ERROR: input must be a 4 digits number, each digit between 1 and ${ColorBulb.entries.size})"
            )

            else -> return digits.map { (ColorBulb.entries[it!! - 1]) }.also { selected ->
                println("Selected combinaison: ${selected.display()}")
            }
        }
    }
}

private fun play(board: Board, combinaison: List<ColorBulb>): Board = Board(
    solution = board.solution,
    played = board.played + evaluate(combinaison, board.solution)
)

internal fun evaluate(combinaison: List<ColorBulb>, solution: List<ColorBulb>): EvaluatedCombinaison {
    val seenIndices = mutableSetOf<Int>()
    val correctlyPlaced = combinaison.withIndex().count { (userIndex, userBulb) ->
        when (userBulb) {
            solution[userIndex] -> {
                seenIndices.add(userIndex)
                true
            }

            else -> false
        }
    }
    val correctColorOnly = combinaison.withIndex().filter { (userIndex, _) ->
        userIndex !in seenIndices
    }.count { (_, userBulb) ->
        val colorExistsElsewhereInSolution = solution.withIndex().filter { (index, _) ->
            index !in seenIndices
        }.firstOrNull { (_, bulb) ->
            bulb == userBulb
        }
        when {
            colorExistsElsewhereInSolution != null -> {
                seenIndices.add(colorExistsElsewhereInSolution.index)
                true
            }

            else -> false
        }
    }
    return EvaluatedCombinaison(
        combinaison = combinaison,
        correctlyPlaced = correctlyPlaced,
        correctColorOnly = correctColorOnly,
    )
}

private const val TOP_SEPARATOR = "┌───────┬───────┐"
private const val BOTTOM_SEPARATOR = "└───────┴───────┘"

private class Board(
    val solution: List<ColorBulb>,
    val played: List<EvaluatedCombinaison> = emptyList(),
) {
    val victorious
        get() = solution == played.last().combinaison

    fun display(): String = """
        |$TOP_SEPARATOR
        |${played.joinToString("\n") { line -> line.display() }}
        |$BOTTOM_SEPARATOR
    """.trimMargin()

    companion object {
        fun randomlyGenerated(): Board = Board(
            solution = (1..4).map { ColorBulb.random() },
        )
    }
}

private enum class Marker(val displayChar: String, val explanation: String) {
    CORRECTLY_PLACED(displayChar = "◈", explanation = "correct position + correct color"),
    CORRECT_COLOR_ONLY(displayChar = "◇", explanation = "correct color only");

    companion object {
        fun legend(): String = "Markers:\n${
            Marker.entries.joinToString("\n") { marker ->
                "  ${marker.displayChar} ▸ ${marker.explanation}"
            }
        }"
    }
}

internal enum class ColorBulb(
    private val value: String,
) {
    RED("[31m"),
    GREEN("[32m"),
    YELLOW("[33m"),
    BLUE("[34m"),
    PURPLE("[35m"),
    CYAN("[36m");

    fun display() = "\u001B${value}◉\u001B[0m"

    companion object {
        fun legend(): String = "Possible colors:\n${
            ColorBulb.entries.withIndex().joinToString("\n") { (index, color) ->
                "  ${index + 1} ▸ ${color.display()}"
            }
        }"

        fun random(): ColorBulb {
            val rand = Random.nextInt(0, entries.size)
            return entries[rand]
        }
    }
}

private fun List<ColorBulb>.display(): String = joinToString(" ") { it.display() }

internal data class EvaluatedCombinaison(
    val combinaison: List<ColorBulb>,
    val correctlyPlaced: Int,
    val correctColorOnly: Int,
) {
    fun display(): String {
        val correctlyPlace = (1..correctlyPlaced).map { Marker.CORRECTLY_PLACED.displayChar }
        val correctColorOnly = (1..correctColorOnly).map { Marker.CORRECT_COLOR_ONLY.displayChar }
        val evaluation = (correctlyPlace + correctColorOnly).joinToString(" ").padEnd(7)
        return "│${combinaison.display()}│$evaluation│"
    }
}
