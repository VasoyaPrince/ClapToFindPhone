package com.example.claptofindphone.utils

import com.example.claptofindphone.clapToFindPhone.model.ResponseData
import com.example.claptofindphone.model.FeaturesIcon

sealed class ApiState {
    object Loading : ApiState()
    object Empty : ApiState()
    class SuccessFeatures(val FeaturesIcon: FeaturesIcon?) : ApiState()
    class FailureFeatures(val error: Throwable) : ApiState()

    class SuccessResponse(val response: ResponseData?) : ApiState()
    class FailureResponse(val error: Throwable) : ApiState()
}