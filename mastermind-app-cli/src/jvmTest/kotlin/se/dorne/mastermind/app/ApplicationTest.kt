package se.dorne.mastermind.app

import kotlin.test.Test
import kotlin.test.assertEquals

class MainKtTest {
    @Test
    fun `all placed`() {
        val comb = listOf(ColorBulb.BLUE, ColorBulb.CYAN, ColorBulb.GREEN, ColorBulb.PURPLE)
        val solution = listOf(ColorBulb.BLUE, ColorBulb.CYAN, ColorBulb.GREEN, ColorBulb.PURPLE)
        val assessment = evaluate(comb, solution)
        assertEquals(assessment.correctColorOnly, 0)
        assertEquals(assessment.correctlyPlaced, 4)
    }

    @Test
    fun `only right colors`() {
        val comb = listOf(ColorBulb.BLUE, ColorBulb.CYAN, ColorBulb.GREEN, ColorBulb.PURPLE)
        val solution = listOf(ColorBulb.CYAN, ColorBulb.BLUE, ColorBulb.PURPLE, ColorBulb.GREEN)
        val assessment = evaluate(comb, solution)
        assertEquals(assessment.correctColorOnly, 4)
        assertEquals(assessment.correctlyPlaced, 0)
    }

    @Test
    fun `only right colors with duplicates in input`() {
        val comb = listOf(ColorBulb.CYAN, ColorBulb.CYAN, ColorBulb.GREEN, ColorBulb.PURPLE)
        val solution = listOf(ColorBulb.RED, ColorBulb.YELLOW, ColorBulb.CYAN, ColorBulb.BLUE)
        val assessment = evaluate(comb, solution)
        assertEquals(assessment.correctColorOnly, 1)
        assertEquals(assessment.correctlyPlaced, 0)
    }

    @Test
    fun `only right colors with duplicates in solution`() {
        val comb = listOf(ColorBulb.RED, ColorBulb.YELLOW, ColorBulb.CYAN, ColorBulb.BLUE)
        val solution = listOf(ColorBulb.CYAN, ColorBulb.CYAN, ColorBulb.GREEN, ColorBulb.PURPLE)
        val assessment = evaluate(comb, solution)
        assertEquals(assessment.correctColorOnly, 1)
        assertEquals(assessment.correctlyPlaced, 0)
    }

    @Test
    fun `bug 1`() {
        val comb = listOf(ColorBulb.RED, ColorBulb.GREEN, ColorBulb.YELLOW, ColorBulb.BLUE)
        val solution = listOf(ColorBulb.RED, ColorBulb.RED, ColorBulb.YELLOW, ColorBulb.YELLOW)
        val assessment = evaluate(comb, solution)
        assertEquals(assessment.correctColorOnly, 0)
        assertEquals(assessment.correctlyPlaced, 2)
    }
    
    @Test
    fun `bug 2`() {
        val comb = listOf(ColorBulb.CYAN, ColorBulb.PURPLE, ColorBulb.CYAN, ColorBulb.PURPLE)
        val solution = listOf(ColorBulb.RED, ColorBulb.YELLOW, ColorBulb.CYAN, ColorBulb.YELLOW)
        val assessment = evaluate(comb, solution)
        assertEquals(assessment.correctColorOnly, 0)
        assertEquals(assessment.correctlyPlaced, 1)
    }
}
