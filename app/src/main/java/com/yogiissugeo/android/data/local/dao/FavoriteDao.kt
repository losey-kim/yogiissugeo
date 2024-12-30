package com.yogiissugeo.android.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yogiissugeo.android.data.local.model.Favorite

@Dao
interface FavoriteDao {
    /**
     * 즐겨찾기 추가.
     *
     * - 즐겨찾기 데이터를 `favorites_table`에 삽입.
     * - 동일한 `binId`가 이미 존재하면 덮어씀.
     *
     * @param favorite 추가할 즐겨찾기 데이터.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite)

    /**
     * 즐겨찾기 삭제.
     *
     * - `favorites_table`에서 해당 즐겨찾기 데이터를 삭제.
     *
     * @param favorite 삭제할 즐겨찾기 데이터.
     */
    @Delete
    suspend fun deleteFavorite(favorite: Favorite)

    /**
     * 특정 수거함이 즐겨찾기 상태인지 확인.
     *
     * - `favorites_table`에 해당 `binId`가 존재하는지 검사.
     *
     * @param binId 확인할 수거함의 ID.
     * @return Boolean - 즐겨찾기 상태(true: 존재, false: 미존재).
     */
    @Query("SELECT EXISTS(SELECT 1 FROM favorites_table WHERE binId = :binId)")
    suspend fun isFavorite(binId: String): Boolean
}