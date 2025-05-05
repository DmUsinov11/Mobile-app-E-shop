package com.example.e_shop.DataClasses

/**
 * Класс данных для заказа
 *
 * @param id Уникальный идентификатор заказа
 * @param userId Идентификатор пользователя, разместившего заказ
 * @param orderDate Дата размещения заказа
 * @param status Статус заказа
 * @param totalPrice Общая стоимость заказа (может быть null)
 * @param deliveryAddress Адрес доставки заказа
 */
data class Order(
    val id: Int,                // Уникальный идентификатор заказа
    val userId: Int,            // Идентификатор пользователя, разместившего заказ
    val orderDate: String,      // Дата размещения заказа
    val status: String,         // Текущий статус заказа
    val totalPrice: Double?,    // Общая стоимость заказа (может быть null)
    val deliveryAddress: String // Адрес доставки
)