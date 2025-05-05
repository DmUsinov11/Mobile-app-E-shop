package com.example.e_shop.DataClasses

/**
 * Класс данных для элемента корзины пользователя
 *
 * @param id Уникальный идентификатор элемента корзины
 * @param userId Идентификатор пользователя, которому принадлежит корзина
 * @param productId Идентификатор продукта
 * @param quantity Количество продукта в корзине
 */
data class UserCartItem(
    val id: Int,         // Уникальный идентификатор элемента корзины
    val userId: Int,     // Идентификатор пользователя
    val productId: Int,  // Идентификатор продукта
    val quantity: Int    // Количество продукта в корзине
)