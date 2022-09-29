package com.yml.paparazzi.imagelist

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import kotlin.math.ceil

@Composable
fun ImageListDestination(imageList: List<Image>, title: String) {
    Column(modifier = Modifier.fillMaxSize()) {
        ImageListDestination(imageList)
    }
}

@Composable
fun ImageListDestination(imageList: List<Image>) {
    LazyVerticalGrid(columns = GridCells.Adaptive(100.dp)) {
        imageList.forEach {
            item {
                ImageCard(modifier = Modifier.padding(5.dp), image = it)
            }
        }
    }
}

@Composable
fun ImageCard(modifier: Modifier = Modifier, image: Image) {
    val width = 100
    val height = try {
        width * image.height / image.width
    } catch (e: Exception) {
        100
    }
        Card(
            shape = RoundedCornerShape(5.dp), modifier = modifier
                .height(height.dp)
                .width(width.dp)
        ) {

            Box {
                val painterResource = rememberAsyncImagePainter(
                    image.fileUri
                )
                Image(
                    painter = painterResource,
                    contentDescription = "",
                    Modifier.fillMaxSize()
                )
            }
        }
}

@Composable
fun StaggeredVerticalGrid(
    modifier: Modifier = Modifier,
    maxColumnWidth: Dp,
    children: @Composable () -> Unit
) {
    Layout(
        content = children,
        modifier = modifier
    ) { measurables, constraints ->
        check(constraints.hasBoundedWidth) {
            "Unbounded width not supported"
        }
        val columns = ceil(constraints.maxWidth / maxColumnWidth.toPx()).toInt()
        val columnWidth = constraints.maxWidth / columns
        val itemConstraints = constraints.copy(maxWidth = columnWidth)
        val colHeights = IntArray(columns) { 0 } // track each column's height
        val placeables = measurables.map { measurable ->
            val column = shortestColumn(colHeights)
            val placeable = measurable.measure(itemConstraints)
            colHeights[column] += placeable.height
            placeable
        }

        val height = colHeights.maxOrNull()?.coerceIn(constraints.minHeight, constraints.maxHeight)
            ?: constraints.minHeight
        layout(
            width = constraints.maxWidth,
            height = height
        ) {
            val colY = IntArray(columns) { 0 }
            placeables.forEach { placeable ->
                val column = shortestColumn(colY)
                placeable.place(
                    x = columnWidth * column,
                    y = colY[column]
                )
                colY[column] += placeable.height
            }
        }
    }
}

private fun shortestColumn(colHeights: IntArray): Int {
    var minHeight = Int.MAX_VALUE
    var column = 0
    colHeights.forEachIndexed { index, height ->
        if (height < minHeight) {
            minHeight = height
            column = index
        }
    }
    return column
}