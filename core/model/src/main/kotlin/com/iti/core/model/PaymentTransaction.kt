package com.iti.core.model

data class PaymentTransaction(
    val id: Long, 
    val amount: Double, 
    val currency: String, 
    val status: String, 
    val paymentMethod: String, 
    val merchantOrderId: String, 
    val coinPackSize: Int?, 
    val tierPurchased: String?, 
    val createdAt: String, 
    val confirmedAt: String?
)
