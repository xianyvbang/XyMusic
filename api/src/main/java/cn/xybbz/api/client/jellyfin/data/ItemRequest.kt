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
 * @param [limit] 分页大小
 * @param [artistIds] 艺术家ids
 * @param [startIndex] 起始索引
 * @param [sortBy] 排序
 * @param [sortOrder] 排序顺序
 * @param [includeItemTypes] 包含数据类型
 * @param [excludeItemTypes] 排除数据类型
 * @param [recursive] 是否递归
 * @param [isFavorite] 是否收藏
 * @param [filters] 过滤类型
 * @param [fields] 额外字段类型
 * @param [imageTypeLimit] 图片类型数量
 * @param [enableImageTypes] 启用图像类型
 * @param [searchTerm] 搜索内容
 * @param [parentId] 上级数据id
 * @param [ids] id集合
 * @param [years] 所属年
 * @param [mediaTypes] 媒体类型
 * @param [nameStartsWith] 以xx开始的名字
 * @param [nameLessThan] 名字小于xxx
 * @param [genreIds] 流派ids
 * @param [albumIds] 专辑ids
 * @param [userId] 用户ID
 * @param [enableTotalRecordCount] 是否返回总分页大小
 * @param [path] 按路径查询
 */
@JsonClass(generateAdapter = true)
data class ItemRequest(
    val limit: Int? = null,
    val artistIds: List<String>? = emptyList(),
    val startIndex: Int? = null,
    val sortBy: List<ItemSortBy>? = emptyList(),
    val sortOrder: List<SortOrder>? = emptyList(),
    val includeItemTypes: List<BaseItemKind>? = emptyList(),
    val excludeItemTypes: List<BaseItemKind>? = emptyList(),
    val recursive: Boolean? = null,
    val isFavorite: Boolean? = null,
    val filters: List<ItemFilter>? = emptyList(),
    val fields: List<ItemFields>? = emptyList(),
    val imageTypeLimit: Int? = null,
    val enableImageTypes: List<ImageType>? = emptyList(),
    val searchTerm: String? = null,
    val parentId: String? = null,
    val ids: List<String>? = emptyList(),
    val years: List<Int>? = emptyList(),
    val mediaTypes: Collection<MediaType>? = emptyList(),
    val nameStartsWith: String? = null,
    val nameLessThan: String? = null,
    val genreIds: List<String>? = null,
    val albumIds:List<String>? = null,
    val userId:String? = null,
    val enableTotalRecordCount: Boolean = true,
    @param:Json(name = "Path")
    val path: String? = null,
) : Request()
