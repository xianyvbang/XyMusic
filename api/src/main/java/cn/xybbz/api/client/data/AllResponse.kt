package cn.xybbz.api.client.data

data class AllResponse<T>(

    val items: List<T>? = null,
    /**
     * 总数
     */
    val totalRecordCount: Int = 0,
    /**
     * 开始位置索引
     */
    val startIndex: Int = 0,
)
