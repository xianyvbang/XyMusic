package cn.xybbz.common.utils

import cn.xybbz.R
import cn.xybbz.common.constants.Constants
import cn.xybbz.localdata.data.library.XyLibrary

object DefaultObjectUtils {

    fun getDefaultXyLibrary(connectionId: Long):XyLibrary{
        return XyLibrary(
            id = Constants.MINUS_ONE_INT.toString(),
            name = R.string.all_media_libraries.toString(),
            connectionId = connectionId,
            collectionType = ""
        )
    }
}