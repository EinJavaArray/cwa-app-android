package de.rki.coronawarnapp.appconfig.mapping

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.SurveyConfig
import de.rki.coronawarnapp.appconfig.internal.ApplicationConfigurationInvalidException
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid
import de.rki.coronawarnapp.server.protocols.internal.v2.PpddEdusParameters
import okhttp3.HttpUrl.Companion.toHttpUrl
import timber.log.Timber
import javax.inject.Inject

@Reusable
class SurveyConfigMapper @Inject constructor() : SurveyConfig.Mapper {
    override fun map(rawConfig: AppConfigAndroid.ApplicationConfigurationAndroid): SurveyConfig = try {
        rawConfig.toSurveyConfig()
    } catch (e: Exception) {
        e.report(
            exceptionCategory = ExceptionCategory.HTTP,
            prefix = SurveyConfigMapper::class.java.simpleName,
            suffix = "Invalid survey config. Treat user survey as disabled"
        )
        SurveyConfigContainer()
    }.also { Timber.v("SurveyConfig=%s", it) }

    private fun AppConfigAndroid.ApplicationConfigurationAndroid.toSurveyConfig(): SurveyConfig {
        val surveyConfig: SurveyConfig
        if (hasEventDrivenUserSurveyParameters() && eventDrivenUserSurveyParameters.hasCommon()) {
            eventDrivenUserSurveyParameters.common.also {
                surveyConfig = SurveyConfigContainer(
                    otpQueryParameterName = it.otpQueryParameterName(),
                    surveyOnHighRiskEnabled = it.surveyOnHighRiskEnabled,
                    surveyOnHighRiskUrl = it.surveyOnHighRiskUrl()
                )
            }
        } else {
            throw ApplicationConfigurationInvalidException(message = "Event driven user survey parameters are missing")
        }

        return surveyConfig
    }

    private fun PpddEdusParameters.PPDDEventDrivenUserSurveyParametersCommon.otpQueryParameterName(): String =
        when (otpQueryParameterName.isNotBlank()) {
            true -> otpQueryParameterName
            else -> throw ApplicationConfigurationInvalidException(message = "OTP query parameter name is invalid")
        }

    private fun PpddEdusParameters.PPDDEventDrivenUserSurveyParametersCommon.surveyOnHighRiskUrl(): String = try {
        surveyOnHighRiskUrl.toHttpUrl() // Just to validate the given url
        surveyOnHighRiskUrl
    } catch (e: Exception) {
        throw ApplicationConfigurationInvalidException(cause = e, message = "Survey on high risk url is invalid")
    }

    data class SurveyConfigContainer(
        override val otpQueryParameterName: String = "",
        override val surveyOnHighRiskEnabled: Boolean = false,
        override val surveyOnHighRiskUrl: String = ""
    ) : SurveyConfig
}
