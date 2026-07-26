package com.iti.careerpilot.features.paywall.domain.model

enum class PaymentFailureReason {
    /** Payment was declined by the bank / card issuer. */
    DECLINED,
    /** Paymob confirmed success but our backend did not reflect it in time. */
    VERIFICATION_TIMEOUT,
}
