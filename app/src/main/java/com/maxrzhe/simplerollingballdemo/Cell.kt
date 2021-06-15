package com.maxrzhe.simplerollingballdemo

data class Cell(
    val column: Int,
    val row: Int,
    var topWall: Boolean = true,
    var leftWall: Boolean = true,
    var rightWall: Boolean = true,
    var bottomWall: Boolean = true,
    var visited: Boolean = false
)