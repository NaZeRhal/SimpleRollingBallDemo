package com.maxrzhe.simplerollingballdemo

data class Ball(
    var centerX: Float,
    var centerY: Float,
    val radius: Float,
    val speed: Float,
    var currentCells: List<Cell>
)