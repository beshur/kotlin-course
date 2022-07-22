package minesweeper

import kotlin.random.Random

fun main() {
  val ITEMS = listOf(".", "X")
  val MINE = ITEMS[1]
  val EMPTY = ITEMS[0]

  val BASIC_FIELD_WIDTH = 9
  val BASIC_FIELD_HEIGHT = 9


  val field: MutableList<MutableList<String>> =  mutableListOf()
  println("How many mines do you want on the field?")
  val maxMines = readln().toInt()
//    val maxMines = 40
  var plantedCount =   0

  val FIELD_WIDTH = BASIC_FIELD_WIDTH// * (maxMines / 10)
  val FIELD_HEIGHT = BASIC_FIELD_HEIGHT// * (maxMines / 10)

  for (i in 0 until FIELD_HEIGHT) {
    val row = mutableListOf<String>()
    for (i in 0 until FIELD_WIDTH) {
      val rand = Random.nextInt(0, 3) * 0.7
      val item = if (plantedCount < maxMines && rand > 0) MINE else EMPTY
      if (item == MINE) {
        plantedCount += 1
      }

      row.add(item)
    }
    field.add(row)
  }

//  println("Planted $plantedCount $maxMines")

  fun lookAround(gameField: MutableList<MutableList<String>>, row: Int, col: Int): Int {
    var minesCount = 0

    for (y in -1..1) {
      for (x in -1..1) {
        if (row + y in 0 until FIELD_HEIGHT && col + x  in 0 until FIELD_WIDTH) {
          val item = gameField[row + y][col + x]
          if (item == MINE) {
            minesCount += 1
          }
        }
      }
    }
    return minesCount
  }

  // look around
  for (y in 0 until FIELD_HEIGHT) {
    for (x in 0 until FIELD_WIDTH) {
      if (field[y][x] == EMPTY) {
        val mines = lookAround(field, y, x)
        if (mines > 0) {
          field[y][x] = mines.toString();
        }
//                println("--- looked")
      }
    }
    println(field[y].joinToString(""))
  }
}
