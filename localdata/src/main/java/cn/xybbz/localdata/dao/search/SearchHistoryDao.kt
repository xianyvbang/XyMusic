package cn.xybbz.localdata.dao.search

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cn.xybbz.localdata.data.search.SearchHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(data: SearchHistory)

    @Update
    suspend fun update(data: SearchHistory)

    @Query("select * from search_history where searchQuery = :searchQuery limit 1")
    suspend fun selectOneBySearchQuery(searchQuery: String): SearchHistory?

    @Query("select * from search_history")
    fun selectListAll(): Flow<List<SearchHistory>>

    @Query("delete from search_history")
    suspend fun deleteAll()

}