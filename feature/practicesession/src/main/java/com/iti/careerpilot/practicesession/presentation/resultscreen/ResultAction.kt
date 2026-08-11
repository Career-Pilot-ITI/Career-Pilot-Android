package com.iti.careerpilot.practicesession.presentation.resultscreen

sealed interface ResultAction {
    data object RefreshResult : ResultAction
}