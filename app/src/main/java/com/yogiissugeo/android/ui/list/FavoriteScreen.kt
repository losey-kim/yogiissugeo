package com.yogiissugeo.android.ui.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun FavoriteScreen(binListViewModel: BinListViewModel = hiltViewModel()) {
    val favoriteBins by binListViewModel.favoriteBins.collectAsState(initial = emptyList())

    //TODO UI 개발 필요
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(favoriteBins) { bin ->
            Column {
                Text(text = "주소: ${bin.address ?: "주소 없음"}")
                Text(text = "구 이름: ${bin.district ?: "구 이름 없음"}")
                Button(onClick = { binListViewModel.toggleFavorite(bin.id) }) {
                    Text("즐겨찾기 삭제")
                }
            }
        }
    }
}