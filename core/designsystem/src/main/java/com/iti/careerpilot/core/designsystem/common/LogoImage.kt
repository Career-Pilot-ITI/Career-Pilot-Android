package com.iti.careerpilot.core.designsystem.common

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.iti.careerpilot.core.designsystem.R

@Composable
fun LogoImage(
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    Image(
        painter = painterResource(R.drawable.img_logo),
        contentDescription = null,
        modifier = modifier,
        contentScale = contentScale
    )
}