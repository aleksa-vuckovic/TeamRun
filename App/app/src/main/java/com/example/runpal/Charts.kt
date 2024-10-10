package com.example.runpal

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.toSize
import com.example.runpal.models.PathPoint


/**
 * Represents one data set of T's mapped to Doubles.
 * Calculates min,max and avg values.
 */
open class ChartDataset<T>(
    val data: List<T>,
    val xValue: (T) -> Double,
    val yValue: (T) -> Double
) {
    val minX: Double
    val maxX: Double
    val minY: Double
    val maxY: Double
    open val avgY: Double

    init {
        minX = data.minOfOrNull(xValue) ?: 0.0
        maxX = data.maxOfOrNull(xValue) ?: 0.0
        minY = data.minOfOrNull(yValue) ?: 0.0
        maxY = data.maxOfOrNull(yValue) ?: 0.0
        avgY = data.sumOf(yValue) / if (data.size == 0) 1 else data.size
    }

    /**
     * Rendering huge datasets can be computationally intensive,
     * so we'll sometimes reduce the datasets by sampling.
     *
     * @param max The max number of elements in the returned dataset
     * @return The reduced dataset
     */
    fun reduce(max: Int): ChartDataset<T> {
        if (data.size <= max) return this
        val result = mutableListOf<T>()
        val interval = data.size/(data.size-max+1).toDouble()
        var next = interval
        var toSkip = 0
        for (i in data.indices) {
            if (i > next) {
                toSkip++
                next += interval
            }
            if (toSkip > 0 && canSkip(data[i])) {
                toSkip--
                continue
            }
            result.add(data[i])
        }
        return ChartDataset(result, xValue, yValue)
    }

    open protected fun canSkip(elem: T): Boolean = true
}


/**
 * A ChartDataset specific to paths, since the average in a path
 * must take into account the time as well.
 */
class PathChartDataset(
    data: List<PathPoint>,
    xValue: (PathPoint) -> Double,
    yValue: (PathPoint) -> Double
): ChartDataset<PathPoint>(data, xValue, yValue) {
    companion object {
        val EMPTY: PathChartDataset = PathChartDataset(data = listOf(PathPoint.INIT), xValue = {0.0}, yValue = {0.0})
    }
    override val avgY: Double
    init {
        var t = 0.0
        var p = 0.0
        for (i in 0..data.size-2)
            if (!data[i+1].end) {val dt = data[i+1].time-data[i].time; t+=dt; p+=dt*(yValue(data[i]) + yValue(data[i+1]))/2; }
        if (t > 0.0) avgY = p/t
        else avgY = 0.0
    }

    override fun canSkip(elem: PathPoint): Boolean = !elem.end
}


/**
 * This class is used for calculating the range of values presented on the chart,
 * an mapping values to chart coordinates based on the value range, chartSize,
 * chartOffset and axes options.
 */
class ChartConfig(val datasets: List<ChartDataset<*>>,
                  val axes: AxesOptions,
                  val chartSize: Size,
                  val chartOffset: Size
) {

    val originX: Double
    val originY: Double
    val spanX: Double
    val spanY: Double

    init {
        val minX = datasets.minOf { it.minX }
        val maxX = datasets.maxOf { it.maxX }
        val minY = datasets.minOf { it.minY }
        val maxY = datasets.maxOf { it.maxY }


        val spanX = (maxX-minX)*axes.xExpandFactor
        if (spanX < axes.xSpanMin) this.spanX = axes.xSpanMin
        else this.spanX = spanX
        val originX = minX - (this.spanX-(maxX-minX))/2
        if (originX < 0.0) this.originX = 0.0
        else this.originX = originX
        //Log.d("CHARTS", "minX = ${minX}, maxX = ${maxX}, spanX = ${spanX}, axes.xSpanMin=${axes.xSpanMin}, this.spanX=${this.spanX}, originX=${originX}")


        val spanY = (maxY-minY)*axes.yExpandFactor
        if (spanY < axes.ySpanMin) this.spanY = axes.ySpanMin
        else this.spanY = spanY
        val originY = minY - (this.spanY-(maxY-minY))/2
        if (originY < 0.0) this.originY = 0.0
        else this.originY = originY
    }

    /**
     * @return The horizontal canvas coordinate
     * corresponding to value x.
     */
    fun mapX(x: Double): Float {
        if (spanX == 0.0) return 0f
        return ((x-originX)/spanX).toFloat()*chartSize.width + chartOffset.width
    }
    /**
     * @return The vertical canvas coordinate
     * corresponding to value y.
     */
    fun mapY(y: Double): Float {
        val t = chartSize.height
        if (spanY == 0.0) return t
        else return t - t * ((y - originY) / spanY).toFloat()
    }
    /**
     * @return The canvas Offset
     * corresponding to the (x,y) value pair.
     */
    fun map(x: Double, y: Double): Offset {
        return Offset(mapX(x), mapY(y))
    }

    /**
     * @return The canvas Offset
     * corresponding to the horizontal tick for value x.
     */
    fun mapTickX(x: Double): Offset {
        return Offset(mapX(x), chartSize.height)
    }
    /**
     * @return The canvas Offset
     * corresponding to the horizontal tick for value y.
     */
    fun mapTickY(y: Double): Offset {
        return Offset(chartOffset.width ,mapY(y))
    }
    /**
     * @return The canvas Offset
     * corresponding to the chart origin.
     */
    val chartOrigin = Offset(chartOffset.width, chartSize.height)
    val chartTopLeft = Offset(chartOffset.width, 0f)
    val chartTopRight = Offset(chartOffset.width + chartSize.width,  0f)
    val chartBottomRight = Offset(chartOffset.width + chartSize.width, chartSize.height)
}

enum class ChartType {
    LINE, SCATTER
}

/**
 * Options for a single dataset.
 */
class ChartOptions(
    val color: Color = Color.Black,
    val shade: Boolean = false,
    val width: Float = 3f,
    val markers: Boolean = false,
    val markerLabel: Formatter<Double>? = null,
    val markerLabelStyle: TextStyle = TextStyle.Default,
    val show: Boolean = true,
    val type: ChartType = ChartType.LINE,
    val reducer: (Int) -> Int = {
        if (it <= 250) it
        else (250 + 250*(1-Math.exp(1-it/250.0))).toInt()
    }
)

/**
 * Axis options for the entire chart.
 *
 * @param yExpandFactor Specifies the expansion factor for the y axis span,
 * relative to the y value span of the dataset. This is to prevent the curve from hitting
 * the very top and bottom of the chart. This value should be equal to or greater than 1.
 * @param ySpanMin The minimum span of the y axis. If the span calculated using yExpandFactor
 * is less than this, then the span will be expanded to fit this value.
 */
data class AxesOptions(
    val xLabel: Formatter<Double> = EmptyFormatter,
    val yLabel: Formatter<Double> = EmptyFormatter,
    val labelStyle: TextStyle = TextStyle.Default,
    val xTickCount: Int = 0,
    val yTickCount: Int = 0,
    val xExpandFactor: Double = 1.0,
    val xSpanMin: Double = 0.0,
    val yExpandFactor: Double = 1.1,
    val ySpanMin: Double = 3.0,
)

@Composable
private fun PathChartLine(dataset: PathChartDataset,
                          options: ChartOptions,
                          chartConfig: ChartConfig,
                          touchPositionState: State<Offset>,
                          bounds: Path
                  ) {
    if (!options.show) return
    val textMeasurer = rememberTextMeasurer()
    val chartOrigin = chartConfig.chartOrigin
    val dataset = remember(dataset) {
        dataset.reduce(options.reducer(dataset.data.size))
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        clipPath(
            path = bounds,
            clipOp = ClipOp.Intersect
        ) {
            for (i in 0..dataset.data.size - 2) {
                if (!dataset.data[i].end) {
                    val startX = dataset.xValue(dataset.data[i])
                    val startY = dataset.yValue(dataset.data[i])
                    val endX = dataset.xValue(dataset.data[i + 1])
                    val endY = dataset.yValue(dataset.data[i + 1])
                    val start = chartConfig.map(startX, startY)
                    val end = chartConfig.map(endX, endY)
                    if (options.shade) {
                        val path = Path().apply {
                            moveTo(start.x, start.y)
                            lineTo(end.x, end.y)
                            lineTo(end.x, chartOrigin.y)
                            lineTo(start.x, chartOrigin.y)
                            close()
                        }
                        drawPath(
                            path = path,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    options.color.copy(alpha = 0.7f),
                                    options.color.copy(alpha = 0f)
                                )
                            )
                        )
                    }
                    drawLine(
                        color = options.color,
                        strokeWidth = options.width,
                        start = start,
                        end = end,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
        if (options.markers && touchPositionState.value != Offset.Zero) {
            val touch = touchPositionState.value
            val point =
                binarySearch(dataset.data, { chartConfig.mapX(dataset.xValue(it)) }, touch.x)
            if (point == null) return@Canvas
            val selectedX = dataset.xValue(point)
            val selectedY = dataset.yValue(point)
            val selected = chartConfig.map(selectedX, selectedY)
            drawLine(
                color = options.color,
                start = selected.copy(x = chartOrigin.x),
                end = selected,
                strokeWidth = options.width / 2,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            )
            drawCircle(brush = Brush.radialGradient(
                colors = listOf(options.color, options.color.copy(alpha = 0f)),
                center = selected,
                radius = options.width*3),
                radius = options.width * 3,
                center = selected)
            drawCircle(color = Color.White, center = selected, radius = options.width/2)

            if (options.markerLabel != null) {
                val label = options.markerLabel.format(selectedY).join()
                val text = textMeasurer.measure(label, options.markerLabelStyle)
                /*
                val topLeftText = Offset(
                    selected.x - text.size.width - options.width * 4,
                    selected.y - text.size.height * 1.2f
                )
                */
                val topLeftText = Offset(
                    selected.x - text.size.width / 2,
                    selected.y - text.size.height - options.width * 4
                )
                drawMarkerLabel(text, topLeftText, options.color)
            }
        }
    }
}

@Composable
private fun <T> ScatterChart(dataset: ChartDataset<T>,
                             options: ChartOptions,
                             chartConfig: ChartConfig,
                             touchPositionState: State<Offset>,
                             bounds: Path
) {
    if (!options.show) return
    val textMeasurer = rememberTextMeasurer()
    Canvas(modifier = Modifier.fillMaxSize()) {
        clipPath(
            path = bounds,
            clipOp = ClipOp.Intersect
        ) {
            for (i in 0..dataset.data.size - 1) {
                val startX = dataset.xValue(dataset.data[i])
                val startY = dataset.yValue(dataset.data[i])
                val start = chartConfig.map(startX, startY)
                drawCircle(
                    color = options.color,
                    radius = options.width,
                    center = start
                )
            }

            if (options.markers && touchPositionState.value != Offset.Zero) {
                val touch = touchPositionState.value
                val point =
                    binarySearch(dataset.data, { chartConfig.mapX(dataset.xValue(it)) }, touch.x)
                if (point == null) return@Canvas
                val selectedX = dataset.xValue(point)
                val selectedY = dataset.yValue(point)
                val selected = chartConfig.map(selectedX, selectedY)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(options.color, options.color.copy(alpha = 0.3f)),
                        center = selected,
                        radius = options.width * 2
                    ),
                    radius = options.width * 3,
                    center = selected
                )

                if (options.markerLabel != null) {
                    val label = options.markerLabel.format(selectedY).join()
                    val text = textMeasurer.measure(label, options.markerLabelStyle)
                    val topLeftText = Offset(
                        selected.x - text.size.width / 2,
                        selected.y - text.size.height - options.width * 4
                    )
                    drawMarkerLabel(text, topLeftText, options.color)
                }
            }
        }
    }
}

fun DrawScope.drawMarkerLabel(text: TextLayoutResult, topLeftText: Offset, color: Color, pad: Float = 10f) {
    val topLeftRect = Offset(topLeftText.x - pad, topLeftText.y - pad)
    drawRoundRect(
        color = color.lightness(0.9f),
        topLeft = topLeftRect,
        size = Size(text.size.width + 2 * pad, text.size.height + 2 * pad),
        cornerRadius = CornerRadius(pad, pad)
    )
    drawText(text, topLeft = topLeftText)
}

@Composable
fun Chart(datasets: List<ChartDataset<*>>,
          options: List<ChartOptions>,
          axesOptions: AxesOptions,
          modifier: Modifier = Modifier) {

    var size by remember {
        mutableStateOf(Size(0f, 0f))
    }
    var chartOffset by remember {
        mutableStateOf(Size(90f, 45f))
    }
    val chartSize = remember(chartOffset, size) {
        size.copy(width = size.width - chartOffset.width, height = size.height - chartOffset.height)
    }
    val chartConfig = remember(datasets, axesOptions, chartOffset, size) {
        ChartConfig(datasets = datasets, axes = axesOptions, chartSize = chartSize, chartOffset = chartOffset)
    }
    val textMeasurer = rememberTextMeasurer()

    val touchPosition = remember {
        mutableStateOf(Offset.Zero)
    }
    Box(modifier = modifier
        .onSizeChanged { size = it.toSize() }
        .pointerInput(Unit) {
            awaitPointerEventScope {
                val on = listOf(
                    PointerEventType.Enter,
                    PointerEventType.Press,
                    PointerEventType.Move
                )
                val off = listOf(
                    PointerEventType.Exit,
                    PointerEventType.Release,
                    PointerEventType.Unknown
                )
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Main)
                    if (on.contains(event.type)) {
                        if (event.changes.size > 0) touchPosition.value =
                            event.changes[0].position
                        event.changes.forEach { it.consume() }
                    } else {
                        touchPosition.value = Offset.Zero
                    }
                }
            }

        }) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            //drawing the axes
            val chartOrigin = chartConfig.chartOrigin
            drawLine(color = Color.Black, strokeWidth = 1f, start = chartOrigin, end = chartOrigin.copy(y = 0f))
            drawLine(color = Color.Black, strokeWidth = 1f, start = chartOrigin, end = chartOrigin.copy(x = size.width))

            //drawing the ticks and labels

            if (axesOptions.xTickCount!= 0) {
                val stepX = chartConfig.spanX / axesOptions.xTickCount
                for (i in 0..axesOptions.xTickCount) {
                    val x = chartConfig.originX + i*stepX
                    val pos = chartConfig.mapTickX(x)
                    drawLine(color = Color.Black, strokeWidth = 10f, start = pos, end = pos.copy(y = pos.y + 6f))


                    val label = axesOptions.xLabel.format(x).join()
                    val text = textMeasurer.measure(label, axesOptions.labelStyle)
                    drawText(text, topLeft = pos.copy(x = pos.x - text.size.width/2, y = pos.y+6f))
                }
            }

            var maxWidth = 0
            if (axesOptions.yTickCount != 0) {
                val stepY = chartConfig.spanY / axesOptions.yTickCount
                for (i in 0..axesOptions.yTickCount) {
                    val y = chartConfig.originY + i*stepY
                    val pos = chartConfig.mapTickY(y)
                    drawLine(color = Color.Black, strokeWidth = 10f, start = pos, end = pos.copy(x = pos.x - 5f))

                    val label = axesOptions.yLabel.format(y).join()
                    val text = textMeasurer.measure(label, axesOptions.labelStyle)
                    drawText(text, topLeft = pos.copy(x = pos.x - chartOffset.width, y = pos.y - (if(i!=axesOptions.yTickCount) text.size.height/2  else 0)))
                    if (maxWidth< text.size.width) maxWidth = text.size.width
                }
            }
            val width = maxWidth.toFloat() + 20f
            if (chartOffset.width < width-10f || chartOffset.width > width+10f) chartOffset = chartOffset.copy(width = width)
        }
        val bounds = remember(chartConfig) {
            Path().apply {
                moveTo(chartConfig.chartTopLeft.x, chartConfig.chartTopLeft.y)
                lineTo(chartConfig.chartTopRight.x, chartConfig.chartTopRight.y)
                lineTo(chartConfig.chartBottomRight.x, chartConfig.chartBottomRight.y)
                lineTo(chartConfig.chartOrigin.x, chartConfig.chartOrigin.y)
                close()
            }
        }
        //Draw the lines
        //for (i in datasets.size - 1 downTo 0) {
        for (i in 0 .. datasets.size - 1) {
            if (options[i].type == ChartType.SCATTER) ScatterChart(
                dataset = datasets[i],
                options = options[i],
                chartConfig = chartConfig,
                touchPositionState = touchPosition,
                bounds = bounds
            )
            else PathChartLine(
                dataset = datasets[i] as PathChartDataset,
                options = options[i],
                chartConfig = chartConfig,
                touchPositionState = touchPosition,
                bounds = bounds
            )

        }
    }
}