package cn.xybbz.config.image

import android.util.Log
import android.webkit.URLUtil
import cn.xybbz.api.TokenServer.baseUrl
import coil.map.Mapper
import coil.request.Options

class BaseUrlMapper : Mapper<String, String> {

    override fun map(data: String, options: Options): String {
        Log.i("BaseUrlMapper", "map: $data")
        return if (URLUtil.isNetworkUrl(data)) {
            data
        } else {
            baseUrl + data
        }
    }
}