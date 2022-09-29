package com.yml.paparazzi.imagelist

data class Image(
    val name: String,
    val fileUri: String,
    val height : Int,
    val width : Int
) {
    companion object {
        val emptyImage = Image("", "", 0, 0)
    }
}