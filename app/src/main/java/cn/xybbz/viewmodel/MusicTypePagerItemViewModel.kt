package cn.xybbz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

class MusicTypePagerItemViewModel<T : Any> : ViewModel() {

    //尽量1.4t或者1.6T cvt油耗高
//车 德系--朗逸 宝来 速腾 polo 晶锐 明锐   国产---瑞纳 悦纳 K2 起亚k3 悦动 艾瑞泽5(16,17年) 风云2(16年 手动挡) 长安逸动XT  拒绝---世嘉 308 c4l c5 508  科鲁兹  日系---骐达(10年-08,09款) 轩逸(08年,09年 12) 马自达6(不省油,不建议) 马自达3 马自达星骋 马自达睿翼(贵)
//    福克斯
    // 分页数据源
    private var pagingDataFlow: Flow<PagingData<T>>? = null


    fun getPagerFlow(
        loadPagingDataFlowBlock: () -> Flow<PagingData<T>>?,
        ifClearData: Boolean
    ): Flow<PagingData<T>>? {
        if (ifClearData) {
            pagingDataFlow = null
        }
        if (pagingDataFlow == null) {
            pagingDataFlow = loadPagingDataFlowBlock.invoke()
        }
        return pagingDataFlow
    }
}