package cn.xybbz.api.client.jellyfin.data

import cn.xybbz.api.client.data.Request
import cn.xybbz.api.enums.jellyfin.BaseItemKind
import cn.xybbz.api.enums.jellyfin.ImageType
import cn.xybbz.api.enums.jellyfin.ItemFields
import cn.xybbz.api.enums.jellyfin.ItemFilter
import cn.xybbz.api.enums.jellyfin.ItemSortBy
import cn.xybbz.api.enums.jellyfin.MediaType
import cn.xybbz.api.enums.jellyfin.SortOrder
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 数据请求实体类
 * @author xybbz
 * @date 2025/12/02
 * @constructor 创建[ItemRequest]
 */
@JsonClass(generateAdapter = true)
data class ItemRequest(
    /**
     * 分页大小
     */
    val limit: Int? = null,
    /**
     * 艺术家ids
     */
    val artistIds: List<String>? = emptyList(),
    /**
     * 起始索引
     */
    val startIndex: Int? = null,
    /**
     * 排序
     */
    val sortBy: List<ItemSortBy>? = emptyList(),
    /**
     * 排序顺序
     */
    val sortOrder: List<SortOrder>? = emptyList(),
    /**
     * 包含数据类型
     */
    val includeItemTypes: List<BaseItemKind>? = emptyList(),
    /**
     * 排除数据类型
     */
    val excludeItemTypes: List<BaseItemKind>? = emptyList(),
    /**
     * 是否递归
     */
    val recursive: Boolean? = null,
    /**
     * 是否收藏
     */
    val isFavorite: Boolean? = null,
    /**
     * 过滤类型
     */
    val filters: List<ItemFilter>? = emptyList(),
    /**
     * 额外字段类型
     */
    val fields: List<ItemFields>? = emptyList(),
    /**
     * 图片类型数量
     */
    val imageTypeLimit: Int? = null,
    /**
     * 启用图像类型
     */
    val enableImageTypes: List<ImageType>? = emptyList(),
    /**
     * 搜索内容
     */
    val searchTerm: String? = null,
    /**
     * 上级数据id
     */
    val parentId: String? = null,
    /**
     * id集合
     */
    val ids: List<String>? = emptyList(),
    /**
     * 所属年
     */
    val years: List<Int>? = emptyList(),
    /**
     * 媒体类型
     */
    val mediaTypes: Collection<MediaType>? = emptyList(),
    /**
     * 按名称开始
     */
    val nameStartsWith: String? = null,
    /**
     * 名字小于xxx
     */
    val nameLessThan: String? = null,
    /**
     * 流派ids
     */
    val genreIds: List<String>? = null,
    /**
     * 专辑ids
     */
    val albumIds:List<String>? = null,
    /**
     * 用户ID
     */
    val userId:String? = null,
    /**
     * 是否返回总分页大小
     */
    val enableTotalRecordCount: Boolean = true,
    /**
     * 按路径查询
     */
    @param:Json(name = "Path")
    val path: String? = null,
) : Request()
