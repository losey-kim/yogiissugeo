package com.yogiissugeo.android.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 데이터베이스 버전 1에서 2로 Migration.
 *
 * - 새로운 테이블 `favorites_table` 추가.
 * - `binId`는 수거함 ID를 저장하며, PRIMARY KEY로 설정.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Favorite 테이블 생성
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS favorites_table (
                binId TEXT PRIMARY KEY NOT NULL
            )
            """
        )
    }
}

/**
 * 데이터베이스 버전 2에서 3로 Migration.
 *
 * - 테이블 'clothing_bin_table'에서 'isBookmarked'필드 추가.
 * - 기본 값 false로 설정
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE clothing_bin_table ADD COLUMN isBookmarked INTEGER NOT NULL DEFAULT 0")
    }
}

/**
 * 데이터베이스 버전 3에서 4로 Migration.
 *
 * - 테이블 'favorites_table'에서 'bookmark_table'로 이름 변경.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 테이블 이름 변경
        database.execSQL("ALTER TABLE favorites_table RENAME TO bookmark_table")
    }
}