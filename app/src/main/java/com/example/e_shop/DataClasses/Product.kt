package com.example.e_shop.DataClasses

/**
 * Класс данных для продукта
 *
 * @param id Уникальный идентификатор продукта
 * @param name Название продукта
 * @param description Описание продукта (необязательно)
 * @param price Цена продукта
 * @param image_url URL изображения продукта (необязательно)
 * @param quantity Количество товара на складе
 */
data class Product(
    val id: Int,                // Уникальный идентификатор продукта
    val name: String,           // Название продукта
    val description: String?,   // Описание продукта (необязательно)
    val price: Double,          // Цена продукта
    val image_url: String?,     // URL изображения продукта (необязательно)
    val quantity: Int           // Количество товара на складе
)