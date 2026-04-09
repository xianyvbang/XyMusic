package cn.xybbz

import java.io.File

val resourcesDir = File(System.getProperty("compose.application.resources.dir"))

fun main() {
    val path = System.getProperty("compose.application.resources.dir")
    val libs = File(path).resolve("lib")
    println(libs.exists())
}