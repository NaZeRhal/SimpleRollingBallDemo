package com.maxrzhe.simplerollingballdemo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Px
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val mazeHandler: MazeHandler = MazeHandler()
    private lateinit var cells: Array<Array<Cell>>
    private lateinit var ball: Ball

    @Px
    private var cellSize: Float = context.dpToPx(DEFAULT_CELL_SIZE)

    @Px
    private var hMargin: Float = context.dpToPx(DEFAULT_HORIZONTAL_MARGIN)

    @Px
    private var vMargin: Float = context.dpToPx(DEFAULT_VERTICAL_MARGIN)

    @Px
    private val ballRadius = context.dpToPx(DEFAULT_BALL_RADIUS)

    @Px
    private var wallStrokeWidth: Float = context.dpToPx(DEFAULT_WALL_THICKNESS)

    @Px
    private var screenWidth = width.toFloat()

    @Px
    private var screenHeight = height.toFloat()

    private val table = RectF()

    private val ballPaint = Paint()
    private val wallPaint = Paint()
    private val exitPaint = Paint()

    private val ballSpeed = DEFAULT_BALL_SPEED_COEFFICIENT

    var deltaX = 0f
        set(value) {
            field = value
            updateBallCenter()
        }
    var deltaY = 0f
        set(value) {
            field = value
            updateBallCenter()
        }

    init {
        initMaze()
        initBall()
        setupPaints()
    }

    private fun initMaze() {
        cells = mazeHandler.createMaze(COLUMNS, ROWS)
    }

    private fun setupPaints() {
        wallPaint.apply {
            isAntiAlias = true
            color = Color.BLACK
            strokeWidth = wallStrokeWidth
        }
        ballPaint.apply {
            isAntiAlias = true
            color = Color.RED
        }
        exitPaint.apply {
            isAntiAlias = true
            color = Color.GRAY
        }
    }

    private fun initBall() {
        ball = Ball(
            0f + ballRadius + wallStrokeWidth,
            0f + ballRadius + wallStrokeWidth,
            ballRadius,
            ballSpeed,
            listOf()
        )
    }

    private fun updateBallCenter() {
        CoroutineScope(Dispatchers.Main).launch {
            ball.centerX += deltaX * ball.speed
            ball.centerY += deltaY * ball.speed
            with(ball) {
                val copyCurrentCells = ArrayList(currentCells)
                copyCurrentCells.forEach { cell ->
                    if (centerX + radius < cell.left ||
                        centerX - radius > cell.right ||
                        centerY - radius > cell.bottom ||
                        centerY + radius < cell.top

                    ) {
                        currentCells = currentCells - listOf(cell)
                        return@forEach
                    }
                    if (centerX - radius - wallStrokeWidth / 2 < cell.left) {
                        if (cell.leftWall) {
                            centerX -= deltaX * ball.speed
                        } else {
                            if (cell.column > 0) {
                                val neighbour = cells[cell.column - 1][cell.row]
                                if (!currentCells.contains(neighbour)) {
                                    currentCells = currentCells + listOf(neighbour)
                                }
                            }
                        }
                    }
                    if (centerX + radius + wallStrokeWidth / 2 > cell.right) {
                        if (cell.rightWall) {
                            centerX -= deltaX * ball.speed
                        } else {
                            if (cell.column < COLUMNS - 1) {
                                val neighbour = cells[cell.column + 1][cell.row]
                                if (!currentCells.contains(neighbour)) {
                                    currentCells = currentCells + listOf(neighbour)
                                }
                            }
                        }
                    }
                    if (centerY - radius - wallStrokeWidth / 2 < cell.top) {
                        if (cell.topWall) {
                            centerY -= deltaY * ball.speed
                        } else {
                            if (cell.row > 0) {
                                val neighbour = cells[cell.column][cell.row - 1]
                                if (!currentCells.contains(neighbour)) {
                                    currentCells = currentCells + listOf(neighbour)
                                }
                            }
                        }
                    }
                    if (centerY + radius + wallStrokeWidth / 2 > cell.bottom) {
                        if (cell.bottomWall) {
                            centerY -= deltaY * ball.speed
                        } else {
                            if (cell.row < ROWS - 1) {
                                val neighbour = cells[cell.column][cell.row + 1]
                                if (!currentCells.contains(neighbour)) {
                                    currentCells = currentCells + listOf(neighbour)
                                }
                            }
                        }
                    }
                }
            }
        }
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenWidth = w.toFloat()
        screenHeight = h.toFloat()
        cellSize = if (screenWidth / screenHeight < COLUMNS.toFloat() / ROWS) {
            screenWidth / (COLUMNS + 1)
        } else {
            screenHeight / (ROWS + 1)
        }
        hMargin = (width - COLUMNS * cellSize) / 2
        vMargin = (height - ROWS * cellSize) / 2
        resolveTableSize()
        resolveCellsCoords()
        ball.currentCells = listOf(cells[0][0])
    }

    private fun resolveCellsCoords() {
        for (x in 0 until COLUMNS) {
            for (y in 0 until ROWS) {
                val cell = cells[x][y]
                cells[x][y] = Cell(
                    column = cell.column,
                    row = cell.row,
                    left = x * cellSize,
                    top = y * cellSize,
                    right = (x + 1) * cellSize,
                    bottom = (y + 1) * cellSize,
                    topWall = cell.topWall,
                    leftWall = cell.leftWall,
                    rightWall = cell.rightWall,
                    bottomWall = cell.bottomWall

                )
            }
        }
    }

    private fun resolveTableSize() {
        table.apply {
            left = hMargin - wallStrokeWidth
            top = vMargin - wallStrokeWidth
            right = left + COLUMNS * cellSize + wallStrokeWidth * 2
            bottom = top + ROWS * cellSize + wallStrokeWidth * 2
        }
    }

    override fun onDraw(canvas: Canvas?) {
        drawMaze(canvas)
        drawBall(canvas)
        drawExit(canvas)
    }

    private fun drawExit(canvas: Canvas?) {
        val exitRadius = cellSize / 2.5f
        canvas?.translate(hMargin, vMargin)
        canvas?.drawCircle(
            (COLUMNS) * cellSize - exitRadius - wallStrokeWidth,
            (ROWS) * cellSize - exitRadius - wallStrokeWidth,
            exitRadius,
            exitPaint
        )
        canvas?.translate(-hMargin, vMargin)
    }

    private fun drawMaze(canvas: Canvas?) {
        canvas?.drawColor(Color.GREEN)
        canvas?.translate(hMargin, vMargin)
        for (x in 0 until COLUMNS) {
            for (y in 0 until ROWS) {
                if (cells[x][y].topWall) {
                    canvas?.drawLine(
                        x * cellSize,
                        y * cellSize,
                        (x + 1) * cellSize,
                        y * cellSize,
                        wallPaint
                    )
                }
                if (cells[x][y].leftWall) {
                    canvas?.drawLine(
                        x * cellSize,
                        y * cellSize,
                        (x) * cellSize,
                        (y + 1) * cellSize,
                        wallPaint
                    )
                }
                if (cells[x][y].rightWall) {
                    canvas?.drawLine(
                        (x + 1) * cellSize,
                        y * cellSize,
                        (x + 1) * cellSize,
                        (y + 1) * cellSize,
                        wallPaint
                    )
                }
                if (cells[x][y].bottomWall) {
                    canvas?.drawLine(
                        (x) * cellSize,
                        (y + 1) * cellSize,
                        (x + 1) * cellSize,
                        (y + 1) * cellSize,
                        wallPaint
                    )
                }
            }
        }
        canvas?.translate(-hMargin, -vMargin)
    }

    private fun drawBall(canvas: Canvas?) {
        canvas?.drawCircle(ball.centerX + hMargin, ball.centerY + vMargin, ball.radius, ballPaint)
    }

    companion object {
        private const val COLUMNS = 6
        private const val ROWS = 12
        private const val DEFAULT_WALL_THICKNESS = 4
        private const val DEFAULT_CELL_SIZE = 20
        private const val DEFAULT_VERTICAL_MARGIN = 8
        private const val DEFAULT_HORIZONTAL_MARGIN = 8

        private const val DEFAULT_BALL_RADIUS = 20
        private const val DEFAULT_BALL_SPEED_COEFFICIENT = 2.5f
    }
}