package com.iti.careerpilot.home.presentation.home.screen.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.CareerPilotShapes
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.shimmerLoading

@Composable
fun HomeShimmerLoading(
    modifier: Modifier = Modifier,
) {
    val cardShape: Shape = CareerPilotShapes.medium
    val headerShape: Shape = RoundedCornerShape(Dimens.SpaceS)
    val baseColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXL),
    ) {
        // 1. Top card skeleton (Subscription card placeholder)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 20.dp)
                    .background(baseColor, shape = cardShape)
                    .shimmerLoading(isLoading = true, shape = cardShape)
            )
        }

        // 2. Overall score card skeleton
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(horizontal = 20.dp)
                    .background(baseColor, shape = cardShape)
                    .shimmerLoading(isLoading = true, shape = cardShape)
            )
        }

        // 3. Practice interview card skeleton
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(horizontal = 20.dp)
                    .background(baseColor, shape = cardShape)
                    .shimmerLoading(isLoading = true, shape = cardShape)
            )
        }

        // 4. Section header skeleton + horizontal row of 3 interview track card skeletons
        item {
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .width(160.dp)
                    .height(20.dp)
                    .background(baseColor, shape = headerShape)
                    .shimmerLoading(isLoading = true, shape = headerShape)
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
                contentPadding = PaddingValues(horizontal = 20.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(3) {
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .height(120.dp)
                            .background(baseColor, shape = cardShape)
                            .shimmerLoading(isLoading = true, shape = cardShape)
                    )
                }
            }
        }

        // 5. Section header skeleton + vertical list of 2 session row skeletons
        item {
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .width(140.dp)
                    .height(20.dp)
                    .background(baseColor, shape = headerShape)
                    .shimmerLoading(isLoading = true, shape = headerShape)
            )
        }

        items(2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 20.dp)
                    .background(baseColor, shape = cardShape)
                    .shimmerLoading(isLoading = true, shape = cardShape)
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeShimmerLoadingPreview() {
    CareerPilotTheme {
        HomeShimmerLoading()
    }
}
