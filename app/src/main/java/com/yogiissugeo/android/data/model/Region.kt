package com.yogiissugeo.android.data.model

import androidx.annotation.StringRes
import com.yogiissugeo.android.R

enum class Region(@StringRes val displayNameRes: Int) {
    SEOUL(R.string.seoul),
    GYEONGGI(R.string.gyeonggi),
    GANGWON(R.string.gangwon),
    GYEONGSANGNAM(R.string.gyeongsangnam),
    DAEJEON(R.string.daejeon)
}