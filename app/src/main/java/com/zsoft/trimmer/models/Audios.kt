package com.zsoft.trimmer.models

import android.net.Uri
import java.time.Duration

data class Audios(
    val name: String,
    val uri: String,
    val duration: Long,
    val size: Long
)