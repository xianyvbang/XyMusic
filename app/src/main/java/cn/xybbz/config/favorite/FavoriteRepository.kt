package cn.xybbz.config.favorite

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor() {
    private val _map = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val favoriteMap = _map.asStateFlow()

    fun toggle(id: String) {
        _map.value = _map.value.toMutableMap().apply {
            put(id, !getOrDefault(id,false))
        }
    }

    fun clearData() {
        _map.value = emptyMap()
    }


    fun toggleBoolean(id: String, ifFavorite: Boolean) {
        _map.value = _map.value.toMutableMap().apply {
            put(id, ifFavorite)
        }
    }
}
