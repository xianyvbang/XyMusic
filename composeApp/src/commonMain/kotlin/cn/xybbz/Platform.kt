package cn.xybbz

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform