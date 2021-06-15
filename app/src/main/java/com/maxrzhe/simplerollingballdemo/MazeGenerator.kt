package com.maxrzhe.simplerollingballdemo

import java.util.*
import kotlin.random.Random

class MazeGenerator {


    fun createMaze(columns: Int, rows: Int): Array<Array<Cell>> {
        var cells = arrayOf<Array<Cell>>()

        for (i in 0 until columns) {
            var array = arrayOf<Cell>()
            for (j in 0 until rows) {
                array += Cell(i, j)
            }
            cells += array
        }

        val stack: Stack<Cell> = Stack()
        var currentCell = Cell(0, 0)
        currentCell.visited = true
        do {
            val nextCell: Cell? = getNeighbour(cells, currentCell)
            if (nextCell != null) {
                removeWall(currentCell, nextCell)
                stack.push(currentCell)
                currentCell = nextCell
                currentCell.visited = true
            } else {
                currentCell = stack.pop()
            }
        } while (!stack.empty())

        return cells
    }

    private fun removeWall(currentCell: Cell, nextCell: Cell) {
        if (currentCell.column == nextCell.column && currentCell.row == nextCell.row + 1) {
            currentCell.topWall = false
            nextCell.bottomWall = false
        }
        if (currentCell.column == nextCell.column && currentCell.row == nextCell.row - 1) {
            currentCell.bottomWall = false
            nextCell.topWall = false
        }
        if (currentCell.row == nextCell.row && currentCell.column == nextCell.column + 1) {
            currentCell.leftWall = false
            nextCell.rightWall = false
        }
        if (currentCell.row == nextCell.row && currentCell.column == nextCell.column - 1) {
            currentCell.rightWall = false
            nextCell.leftWall = false
        }
    }

    private fun getNeighbour(cells: Array<Array<Cell>>, currentCell: Cell): Cell? {
        var neighbours = listOf<Cell>()

        currentCell.apply {
            //check left neighbour
            if (column > 0) {
                cells[column - 1][row].apply {
                    if (!visited) {
                        neighbours = neighbours + this
                    }
                }
            }
            //check right neighbour
            if (column < cells.size - 1) {
                cells[column + 1][row].apply {
                    if (!visited) {
                        neighbours = neighbours + this
                    }
                }
            }
            //check top neighbour
            if (row > 0) {
                cells[column][row - 1].apply {
                    if (!visited) {
                        neighbours = neighbours + this
                    }
                }
            }
            //check bottom neighbour
            if (row < cells[column].size - 1) {
                cells[column][row + 1].apply {
                    if (!visited) {
                        neighbours = neighbours + this
                    }
                }
            }
        }
        return if (neighbours.isNotEmpty()) neighbours[Random.nextInt(neighbours.size)] else null
    }
}