package minesweeper

import kotlin.random.Random

enum class Indicators(val str: String) {
  MINE("X"), EMPTY("."), MARKED("*")
}
class GameField(val maxMines: Int = 10, val height: Int = 9, val width: Int = 9) {
  private val field: MutableList<MutableList<Indicators>> =  mutableListOf()
  private val playerField: MutableList<MutableList<String>> =  MutableList(9) {MutableList(width) {Indicators.EMPTY.str}}
  private val randomizerMargin = height * width / (maxMines + 3)
  init {
    var plantedCount = 0
    for (i in 0 until height) {
      val row = mutableListOf<Indicators>()
      for (col in 0 until width) {
        val rand = Random.nextInt(0, randomizerMargin)

        val item = if (plantedCount < maxMines && rand == 0) Indicators.MINE else Indicators.EMPTY
        if (item == Indicators.MINE) {
          plantedCount += 1
        }
        row.add(item)
      }
      field.add(row)
    }
    lookAround()
  }

  private fun lookAroundCell(row: Int, col: Int): Int {
    var minesCount = 0

    for (y in -1..1) {
      for (x in -1..1) {
        if (row + y in 0 until height && col + x  in 0 until width) {
          val item = field[row + y][col + x]
          if (item == Indicators.MINE) {
            minesCount += 1
          }
        }
      }
    }
    return minesCount
  }

  private fun lookAround() {
    for (y in 0 until height) {
      for (x in 0 until width) {
        if (field[y][x] == Indicators.EMPTY) {
          val mines = lookAroundCell(y, x)
          if (mines > 0) {
            playerField[y][x] = mines.toString()
          }
        }
      }
    }
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
  fun toggleMark(offsetX: Int, offsetY: Int): Boolean {
    if (offsetX !in 1..width || offsetY !in 1..height) {
      return false
    }
    val x = offsetX - 1
    val y = offsetY - 1
    var noError = true
    when (playerField[y][x]) {
      Indicators.MARKED.str -> playerField[y][x] = Indicators.EMPTY.str
      Indicators.EMPTY.str -> playerField[y][x] = Indicators.MARKED.str
      else -> noError = false
    }

    return noError
  }

  fun getAllMinesCleared(): Boolean {
    var notMarked = maxMines
    for (y in 0 until height) {
      for (x in 0 until width) {
        if (field[y][x] == Indicators.MINE && playerField[y][x] == Indicators.MARKED.str) {
          notMarked -= 1
        }
      }
    }

    return notMarked == 0
  }
}

fun main() {
  println("How many mines do you want on the field?")
  val maxMines = readln().toInt()
//  val maxMines = 10
  val field = GameField(maxMines)

  while (!field.getAllMinesCleared()) {
    field.printPlayerField()
    println("Set/delete mines marks (x and y coordinates):")
    val (x, y) = readln().split(" ").map { it -> it.toInt() }
    val isCorrectMove = field.toggleMark(x, y)
    if (!isCorrectMove) {
      println("There is a number here!")
    }
  }

  println("Congratulations! You found all the mines")
}
