package com.iti.careerpilot.nestednavigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationItemIconPosition
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource

@Composable
fun BottomNavigationButton(
    onClick: () -> Unit,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    @StringRes label: Int,
) {
    ShortNavigationBarItem(
        selected = selected,
        onClick = onClick,
        iconPosition = NavigationItemIconPosition.Start,
        icon = {
            Icon(
                imageVector = ImageVector.vectorResource(icon),
                contentDescription = null,
            )
        },
        label = {
            Text(
                text = stringResource(label),
            )
        },
        modifier = modifier,
    )
}
