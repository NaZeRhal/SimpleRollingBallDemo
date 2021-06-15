package com.maxrzhe.simplerollingballdemo

data class Cell(
    val column: Int,
    val row: Int,
    var left: Float = 0f,
    var top: Float = 0f,
    var right: Float = 0f,
    var bottom: Float = 0f,
    var topWall: Boolean = true,
    var leftWall: Boolean = true,
    var rightWall: Boolean = true,
    var bottomWall: Boolean = true,
    var visited: Boolean = false
)