package minesweeper

import kotlin.random.Random

enum class Indicator(val str: String) {
  MINE("X"), EMPTY("."), MARKED("*"), CHECKED("/")
}
enum class Action(val str: String) {
  MINE("mine"), FREE("free")
}

enum class ActionResult {
  CHECKED, DETONATED, INVALID
}

class FieldPoint(
  var hasMine: Boolean = false,
  var explored: Boolean = false,
  var marked: Boolean = false,
  var minesAround: Int = 0) {
  fun toString(withMines: Boolean): String {
    return when {
      withMines && hasMine -> Indicator.MINE.str
      marked -> Indicator.MARKED.str
      explored && !hasMine && minesAround > 0 -> minesAround.toString()
      explored && !hasMine && minesAround == 0 -> Indicator.CHECKED.str
      else -> Indicator.EMPTY.str
    }
  }

  fun setExplored() {
    explored = true
  }
  fun toggleMarked() {
    marked = !marked
  }

  fun setMine() {
    hasMine = true
  }

  fun setMinesCount(count: Int) {
    minesAround = count
  }
}


class GameField(private val maxMines: Int = 10, private val height: Int = 9, private val width: Int = 9) {
  private val field: MutableList<MutableList<FieldPoint>> = MutableList(height) { MutableList(width) { FieldPoint() } }
  private val randomizerMargin = height * width / (maxMines + 3)

  fun getPoint(y: Int, x: Int): FieldPoint {
    return field[y][x]
  }
  fun setExplored(y: Int, x: Int): Boolean {
    val point = getPoint(y, x)
    point.setExplored()

    if (!point.hasMine) {
      exploreAround(y, x)
    }

    return point.hasMine
  }

  private fun exploreAround(y: Int, x: Int) {
    for (nextY in y - 1..y + 1) {
      for (nextX in x - 1..x + 1) {
        if (nextY in 0 until height && nextX in 0 until width) {
          val nextPoint = getPoint(nextY, nextX)
          if (!nextPoint.hasMine && !nextPoint.marked && !nextPoint.explored) {
            nextPoint.setExplored()
            if (nextPoint.minesAround == 0) {
              setExplored(nextY, nextX)
            }
          }
        }
      }
    }
  }

  private fun printRow(row: MutableList<FieldPoint>, withMines: Boolean): String {
    return row.joinToString( separator = "", transform = { it.toString(withMines) })
  }

  fun printField(withMines: Boolean): MutableList<String> {
    val result = mutableListOf <String>()
    for (row in field) {
      result.add(printRow(row, withMines))
    }
    return result
  }


  private fun plantMines() {
    var plantedCount = 0
    for (y in field.indices) {
      for (x in field[y].indices) {
        val rand = Random.nextInt(0, randomizerMargin)
        val point = getPoint(y, x)
        if (plantedCount < maxMines && rand == 0 && !point.explored) {
          plantedCount += 1
          point.setMine()
        }
      }
    }
  }

  private fun lookAroundCell(row: Int, col: Int): Int {
    var minesCount = 0
    println("looking around $row $col")
    for (nextY in -1..1) {
      for (nextX in -1..1) {
        val y = row + nextY
        val x = col + nextX
        if (y in 0 until height && x in 0 until width) {
          if (getPoint(y, x).hasMine) {
            minesCount += 1
          }
        }
      }
    }
    return minesCount
  }

  private fun lookAround() {
    for (row in field.indices) {
      for (col in field[row].indices) {
        val count = lookAroundCell(row, col)
        getPoint(row, col).setMinesCount(count)
      }
    }
  }

  fun onFirstTurn(y: Int, x: Int) {
    getPoint(y, x).setExplored()
    plantMines()
    lookAround()

    // DEBUG
    for (row in printField(true)) {
      println(row)
    }

  }

  fun getAllMinesCleared(): Boolean {
    var notMarked = maxMines
//    var allCleared = width * height - maxMines
    for (y in 0 until height) {
      for (x in 0 until width) {
//        if (playerField[y][x] != Indicators.EMPTY.str) {
//          allCleared -= 1
//        }
        val point = getPoint(y, x)
        if (point.marked && point.hasMine) {
          notMarked -= 1
        }
      }
    }

    return notMarked == 0
  }


}

class Game(maxMines: Int = 10, private  val height: Int = 9, private val width: Int = 9) {
  private val field = GameField(maxMines, height, width)
  private val playerField: MutableList<MutableList<String>> =
    MutableList(9) { MutableList(width) { Indicator.EMPTY.str } }
  private var turnCount = 0
  var lost = false
    private set

  fun printPlayerField() {
    println("")
    println(" |123456789|")
    println("-|---------|")
    for ((index, row) in field.printField(lost).withIndex()) {
      println("${index + 1}|$row|")
    }
    println("-|---------|")
  }

  private fun toggleMark(x: Int, y: Int): ActionResult {
    field.getPoint(y, x).toggleMarked()
    return ActionResult.CHECKED
  }

  private fun renderAllMines() {
    field.printField(true)
  }

  private fun explore(x: Int, y: Int): ActionResult {
    val hasMine = field.setExplored(y, x)
    return if (hasMine) {
      renderAllMines()
      ActionResult.DETONATED
    } else {
      ActionResult.CHECKED
    }
  }

  fun playerTurn(offsetX: Int, offsetY: Int, action: Action): ActionResult {
    if (offsetX !in 1..width || offsetY !in 1..height) {
      return ActionResult.INVALID
    }
    val x = offsetX - 1
    val y = offsetY - 1

    if (playerField[y][x] == Indicator.CHECKED.str) {
      return ActionResult.INVALID
    }

    if (turnCount == 0) {
      field.onFirstTurn(y, x)
    }
    turnCount += 1


    return if (action == Action.MINE) {
      toggleMark(x, y)
    } else {
      explore(x, y)
    }
  }

  fun getAllMinesCleared(): Boolean {
    return field.getAllMinesCleared()
  }

  fun playerLost() {
    lost = true
    println("You stepped on a mine and failed")
    printPlayerField()
  }

  fun playerWon() {
    println("You won!")
    printPlayerField()
  }
}

fun main() {
  println("How many mines do you want on the field?")
  val maxMines = readln().toInt()
//  val maxMines = 10
  val game = Game(maxMines)

  while (!game.getAllMinesCleared() || game.lost) {
    game.printPlayerField()
    println("Set/unset mine marks or claim a cell as free (x and y coordinates with either mine or free):")
    val (xStr, yStr, actionStr) = readln().split(" ")
    val (x, y) = listOf(xStr, yStr).map { it.toInt() }
    val action = when (actionStr) {
      Action.MINE.str -> Action.MINE
      Action.FREE.str -> Action.FREE
      else -> Action.MINE
    }

    when (game.playerTurn(x, y, action)) {
      ActionResult.INVALID -> println("There is a number here!")
      ActionResult.DETONATED -> return game.playerLost()
      else -> print("")
    }

  }

  game.playerWon()
}
