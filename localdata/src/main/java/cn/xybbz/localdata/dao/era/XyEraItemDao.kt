package cn.xybbz.localdata.dao.era

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cn.xybbz.localdata.data.era.XyEraItem

/**
 * 年代数据
 */
@Dao
interface XyEraItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBatch(vararg data: XyEraItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBatch(data: List<XyEraItem>)

    @Query("select * from xy_era_item where era = :era")
    suspend fun selectOneByEra(era: Int): XyEraItem?

    @Update
    suspend fun updateById(data: XyEraItem)

    @Query("select * from xy_era_item order by era desc")
    suspend fun selectList(): List<XyEraItem>

    @Query("delete from xy_era_item")
    suspend fun removeByConnectionId()

    @Query("delete from xy_era_item")
    suspend fun removeAll()
}