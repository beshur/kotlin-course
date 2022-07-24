package minesweeper

import kotlin.random.Random

enum class Indicators(val str: String) {
  MINE("X"), EMPTY("."), MARKED("*"), CHECKED("/")
}
enum class Action(val str: String) {
  MINE("mine"), FREE("free"), NULL("null")
}

enum class ActionResult {
  CHECKED, DETONATED, INVALID
}

data class Point(val y: Int, val x: Int)

class GameField(val maxMines: Int = 10, val height: Int = 9, val width: Int = 9) {
  private val field: MutableList<MutableList<Indicators>> = MutableList(9) { MutableList(width) { Indicators.EMPTY } }
  private val playerField: MutableList<MutableList<String>> =
    MutableList(9) { MutableList(width) { Indicators.EMPTY.str } }
  private val randomizerMargin = height * width / (maxMines + 3)
  private var turnCount = 0
  var lost = false
    private set

  private fun plantMines() {
    var plantedCount = 0
    for (y in field.indices) {
      for (x in field[y].indices) {
        val rand = Random.nextInt(0, randomizerMargin)

        val playerChecked = playerField[y][x] == Indicators.CHECKED.str
        val item = if (plantedCount < maxMines && rand == 0 && !playerChecked) Indicators.MINE else Indicators.EMPTY
        if (item == Indicators.MINE) {
          plantedCount += 1
        }
        field[y][x] = item
      }
    }
  }

  private fun lookAroundCell(row: Int, col: Int): Boolean {
    var minesCount = 0
    val clearCells = mutableListOf<Point>()
    println("looking around $row $col")
    for (nextY in -1..1) {
      for (nextX in -1..1) {
        val y = row + nextY
        val x = col + nextX
        if (y in 0 until height && x in 0 until width) {
          val item = field[y][x]
          if (item == Indicators.MINE) {
            minesCount += 1
          } else {
//            clearCells.add(Point(y, x))
//            explore(x, y)
          }
        }
      }
    }

    if (minesCount > 0) {
      playerField[row][col] = minesCount.toString()
      clearCells.clear()
      return false
    } else {
      return if (playerField[row][col] != Indicators.CHECKED.str) {
        playerField[row][col] = Indicators.CHECKED.str
        true
      } else {
        false
      }
    }
  }

  private fun exploreClearCells(clearCells: MutableList<Point>) {
    printPlayerField()

    println("clearCells ${clearCells.joinToString()}")

    for (point in clearCells) {
      explore(point.x, point.y)
    }
  }

  private fun lookAround(row: Int, col: Int) {
    val nextClearCells = mutableListOf<Point>()

    for (nextY in -1..1) {
      for (nextX in -1..1) {
        val y = row + nextY
        val x = col + nextX
        if (y in 0 until height && x in 0 until width) {
          if (lookAroundCell(y, x)) {
            // clear cell
            nextClearCells.add(Point(y, x))
          }
        }
      }
    }
    exploreClearCells(nextClearCells)
  }

  fun printPlayerField() {
    println("")
    println(" |123456789|")
    println("-|---------|")
    for ((index, row) in playerField.withIndex()) {
      print("${index + 1}|")
      print(row.joinToString(""))
      println("|")
    }
    println("-|---------|")
  }

  private fun toggleMark(x: Int, y: Int): ActionResult {
    when (playerField[y][x]) {
      Indicators.MARKED.str -> playerField[y][x] = Indicators.EMPTY.str
      Indicators.EMPTY.str -> playerField[y][x] = Indicators.MARKED.str
    }
    return ActionResult.CHECKED
  }

  private fun renderAllMines() {
    for (y in field.indices) {
      for (x in field[y].indices) {
        if (field[y][x] == Indicators.MINE) {
          playerField[y][x] = Indicators.MINE.str
        }
      }
    }
  }

  private fun explore(x: Int, y: Int): ActionResult {
    return if (field[y][x] == Indicators.MINE) {
      renderAllMines()
      ActionResult.DETONATED
    } else {
      // look around
      lookAround(y, x)
      ActionResult.CHECKED
    }
  }

  fun playerTurn(offsetX: Int, offsetY: Int, action: Action): ActionResult {
    if (offsetX !in 1..width || offsetY !in 1..height) {
      return ActionResult.INVALID
    }
    val x = offsetX - 1
    val y = offsetY - 1

    if (playerField[y][x] == Indicators.CHECKED.str) {
      return ActionResult.INVALID
    }

    if (turnCount == 0) {
      playerField[y][x] = Indicators.CHECKED.str
      plantMines()
    }
    turnCount += 1


    return if (action == Action.MINE) {
      toggleMark(x, y)
    } else {
      explore(x, y)
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
        if (field[y][x] == Indicators.MINE && playerField[y][x] == Indicators.MARKED.str) {
          notMarked -= 1
        }
      }
    }

    return notMarked == 0
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
  val field = GameField(maxMines)

  while (!field.getAllMinesCleared() || field.lost) {
    field.printPlayerField()
    println("Set/unset mine marks or claim a cell as free (x and y coordinates with either mine or free):")
    val (xStr, yStr, actionStr) = readln().split(" ")
    val (x, y) = listOf(xStr, yStr).map { it -> it.toInt() }
    val action = when (actionStr) {
      Action.MINE.str -> Action.MINE
      Action.FREE.str -> Action.FREE
      else -> Action.MINE
    }

    val turnResult = field.playerTurn(x, y, action)
    when (turnResult) {
      ActionResult.INVALID -> println("There is a number here!")
      ActionResult.DETONATED -> return field.playerLost()
      else -> print("")
    }

  }

  field.playerWon()
}
