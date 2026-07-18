package com.iti.careerpilot.login.presentation.login.screen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.CareerPilotShapes
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.softShadow
import com.iti.careerpilot.login.R
import com.joelkanyi.jcomposecountrycodepicker.component.KomposeCountryCodePicker
import com.joelkanyi.jcomposecountrycodepicker.component.rememberKomposeCountryCodePickerState

@Composable
fun PhoneInputField(
    phoneNumber: String,
    isLoading: Boolean,
    onPhoneNumberChange: (String) -> Unit,
    onRegionChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val countryCodePickerState = rememberKomposeCountryCodePickerState(
        defaultCountryCode = "eg",
        showCountryCode = true,
        showCountryFlag = true,
    )

    val regionCode = countryCodePickerState.countryCode
    LaunchedEffect(regionCode) {
        onRegionChange(regionCode)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.ButtonHeight + Dimens.SpaceM + Dimens.SpaceXS)
            .softShadow()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = CareerPilotShapes.medium
            )
            .border(
                width = 1.2.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = CareerPilotShapes.medium
            )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KomposeCountryCodePicker(
                state = countryCodePickerState,
                text = "",
                onValueChange = {},
                showOnlyCountryCodePicker = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                dropDownIcon = {
                    Icon(
                        imageVector = Icons.Rounded.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                },
                modifier = Modifier
                    .padding(horizontal = Dimens.SpaceM)
                    .fillMaxHeight(),
            )

            Spacer(
                modifier = Modifier
                    .width(1.2.dp)
                    .fillMaxHeight()
                    .background(CareerPilotPalette.gray100)
            )

            BasicTextField(
                value = phoneNumber,
                onValueChange = onPhoneNumberChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Dimens.SpaceL),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                singleLine = true,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                decorationBox = { innerTextField ->
                    if (phoneNumber.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.phone_placeholder),
                            style = MaterialTheme.typography.bodyLarge,
                            color = CareerPilotPalette.gray400
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}