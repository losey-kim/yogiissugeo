package com.yogiissugeo.android.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yogiissugeo.android.data.local.model.Bookmark

@Dao
interface BookmarkDao {
    /**
     * 저장 추가.
     *
     * - 저장 데이터를 `bookmark_table`에 삽입.
     * - 동일한 `binId`가 이미 존재하면 덮어씀.
     *
     * @param bookmark 추가할 저장 데이터.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark)

    /**
     * 저장 삭제.
     *
     * - `bookmark_table`에서 해당 즐겨찾기 데이터를 삭제.
     *
     * @param bookmark 삭제할 저장 데이터.
     */
    @Delete
    suspend fun deleteBookmark(bookmark: Bookmark)

    /**
     * 특정 수거함이 저장 상태인지 확인.
     *
     * - `bookmark_table`에 해당 `binId`가 존재하는지 검사.
     *
     * @param binId 확인할 수거함의 ID.
     * @return Boolean - 저장 상태(true: 존재, false: 미존재).
     */
    @Query("SELECT EXISTS(SELECT 1 FROM bookmark_table WHERE binId = :binId)")
    suspend fun isBookmark(binId: String): Boolean
}