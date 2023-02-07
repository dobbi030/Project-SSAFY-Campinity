package com.ssafy.campinity.common.util

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ssafy.campinity.R

fun glide(
    context: Context,
    url: String,
    overrideSize: Int,
    view: ImageView
) {
    Glide.with(context)
        .load(url)
        .override(overrideSize)
        .centerCrop()
        .placeholder(R.drawable.bg_image_not_found)
        .error(R.drawable.bg_image_not_found)
        .fallback(R.drawable.bg_image_not_found)
        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        .into(view)
}