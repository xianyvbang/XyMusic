package cn.xybbz.api.client.data


/**
 * 回应实体类
 * @author xybbz
 * @date 2025/12/02
 * @constructor 创建[XyResponse]
 * @param [items] 返回数据列表
 * @param [totalRecordCount] 总条数
 * @param [startIndex] 起始索引
 */
data class XyResponse<T>(

    /**
     * 返回数据列表
     */
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
