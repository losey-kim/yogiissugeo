package com.yogiissugeo.android.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * '좋아요'한 수거함 정보를 관리하는 엔티티 클래스
 *
 * @property binId ClothingBin의 id
 */
@Entity(tableName = "favorites_table")
data class Favorite(
    @PrimaryKey val binId: String // ClothingBin의 id와 연결
)