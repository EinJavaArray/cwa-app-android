package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeySubmissionStateDonor @Inject constructor() : DonorModule {

    override suspend fun beginDonation(request: DonorModule.Request): DonorModule.Contribution {
        // TODO
        return object : DonorModule.Contribution {
            override suspend fun injectData(protobufContainer: Any) {
                // TODO
            }

            override suspend fun finishDonation(successful: Boolean) {
                // TODO
            }
        }
    }
}