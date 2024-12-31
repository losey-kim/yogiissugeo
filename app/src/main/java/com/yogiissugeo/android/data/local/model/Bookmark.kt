package com.yogiissugeo.android.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 저장한 수거함 정보를 관리하는 엔티티 클래스
 *
 * @property binId ClothingBin의 id
 */
@Entity(tableName = "bookmark_table")
data class Bookmark(
    @PrimaryKey val binId: String, // ClothingBin의 id와 연결
    val createdAt: Long
)