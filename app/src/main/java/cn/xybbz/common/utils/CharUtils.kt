package cn.xybbz.common.utils

object CharUtils {


    fun isEnglishLetter(c: Char): Boolean {
        return (c in 'A'..'Z') || (c in 'a'..'z')
    }
}