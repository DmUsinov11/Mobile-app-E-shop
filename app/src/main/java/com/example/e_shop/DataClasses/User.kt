package com.example.e_shop.DataClasses

import android.database.Cursor
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Класс данных для пользователя
 *
 * @param id Уникальный идентификатор пользователя
 * @param username Имя пользователя (логин)
 * @param password Пароль пользователя
 * @param role Роль пользователя (например, admin или user)
 * @param phone Номер телефона (необязательно)
 * @param email Электронная почта (необязательно)
 */
@Parcelize
data class User(
    val id: Int,            // Уникальный идентификатор пользователя
    val username: String,   // Имя пользователя (логин)
    val password: String,   // Пароль пользователя
    val role: String,       // Роль пользователя
    val phone: String?,     // Номер телефона (необязательно)
    val email: String?      // Электронная почта (необязательно)
) : Parcelable {

    companion object {
        /**
         * Создает объект User из курсора базы данных
         *
         * @param cursor Курсор, указывающий на запись пользователя
         * @return Объект User, созданный на основе данных курсора
         */
        fun createFromCursor(cursor: Cursor): User {
            // Получаем идентификатор пользователя из курсора
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            // Получаем логин из курсора
            val username = cursor.getString(cursor.getColumnIndexOrThrow("username"))
            // Получаем пароль из курсора
            val password = cursor.getString(cursor.getColumnIndexOrThrow("password"))
            // Получаем роль из курсора
            val role = cursor.getString(cursor.getColumnIndexOrThrow("role"))
            // Получаем номер телефона из курсора
            val phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"))
            // Получаем email из курсора
            val email = cursor.getString(cursor.getColumnIndexOrThrow("email"))
            // Возвращаем созданный объект User
            return User(id, username, password, role, phone, email)
        }
    }
}