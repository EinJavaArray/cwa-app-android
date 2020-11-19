package de.rki.coronawarnapp.nearby.modules.version

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.abs

@Singleton
class DefaultENFVersion @Inject constructor(
    private val client: ExposureNotificationClient
) : ENFVersion {

    override suspend fun getENFClientVersion(): Long? = try {
        internalGetENFClientVersion()
    } catch (e: Exception) {
        Timber.w(e, "Failed to get ENFClient version.")
        null
    }

    override suspend fun isAtLeast(compareVersion: Long): Boolean {
        if (!compareVersion.isCorrectVersionLength) throw IllegalArgumentException("given version has incorrect length")

        return try {
            internalGetENFClientVersion() >= compareVersion
        } catch (apiException: ApiException) {
            if (apiException.statusCode != CommonStatusCodes.API_NOT_CONNECTED) {
                throw apiException
            } else {
                return false
            }
        }
    }

    private suspend fun internalGetENFClientVersion(): Long = suspendCoroutine { cont ->
        client.version
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    // check if a raw long has the correct length to be considered an API version
    private val Long.isCorrectVersionLength
        get(): Boolean = abs(this).toString().length == GOOGLE_API_VERSION_FIELD_LENGTH

    companion object {
        private const val GOOGLE_API_VERSION_FIELD_LENGTH = 8
    }
}
