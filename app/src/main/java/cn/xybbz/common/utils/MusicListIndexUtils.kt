package cn.xybbz.common.utils

/**
 * 音乐列表的index计算
 */
object MusicListIndexUtils {
    /**
     * 通过index获得页码
     * @param [index] 当前index
     * @param [pageSize] 页面大小
     * @return [Int] 页码
     */
    fun getPageNum(index: Int, pageSize: Int): Int {
        return if (index <= pageSize) 0
        else {
            (index / pageSize)
        }
    }
}