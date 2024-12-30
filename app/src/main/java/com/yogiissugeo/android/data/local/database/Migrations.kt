package com.yogiissugeo.android.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 데이터베이스 버전 1에서 2로의 Migration.
 *
 * - 새로운 테이블 `favorites_table` 추가.
 * - `binId`는 수거함 ID를 저장하며, PRIMARY KEY로 설정.
 *
 * @param database Migration이 실행되는 데이터베이스 인스턴스.
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