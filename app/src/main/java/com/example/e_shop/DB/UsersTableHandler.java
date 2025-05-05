package com.example.e_shop.DB;

import android.content.ContentValues; // Импортируем класс для создания набора значений для базы
import android.database.Cursor; // Импортируем класс для работы с курсором базы данных
import android.database.sqlite.SQLiteDatabase; // Импортируем класс для работы с SQLite базой данных

/**
 * Класс-обработчик таблицы пользователей
 */
public class UsersTableHandler {

    private DataBaseHandler dbHandler; // Объект базы данных

    public static final String TABLE_USERS = "Users"; // Имя таблицы пользователей
    public static final String COLUMN_USER_ID = "id"; // Колонка с идентификатором пользователя
    public static final String COLUMN_LOGIN = "username"; // Колонка с логином пользователя
    public static final String COLUMN_PASSWORD = "password"; // Колонка с паролем пользователя
    public static final String COLUMN_ROLE = "role"; // Колонка с ролью пользователя
    public static final String COLUMN_PHONE = "phone"; // Колонка с номером телефона
    public static final String COLUMN_EMAIL = "email"; // Колонка с email

    /**
     * Конструктор класса UsersTableHandler
     * @param dbHandler Объект базы данных для доступа к таблице пользователей
     */
    public UsersTableHandler(DataBaseHandler dbHandler) {
        this.dbHandler = dbHandler; // Сохраняем объект базы данных для дальнейших операций
    }

    /**
     * Метод для добавления нового пользователя
     * @param login Логин пользователя
     * @param password Пароль пользователя
     * @param phone Номер телефона пользователя
     * @param email Email пользователя
     * @return Идентификатор вставленной записи пользователя
     */
    public long addUser(String login, String password, String phone, String email) {
        SQLiteDatabase db = dbHandler.getWritableDatabase(); // Получаем базу для записи
        ContentValues values = new ContentValues(); // Создаем объект для набора значений
        values.put(COLUMN_LOGIN, login); // Добавляем логин в набор значений
        values.put(COLUMN_PASSWORD, password); // Добавляем пароль в набор значений
        values.put(COLUMN_PHONE, phone); // Добавляем номер телефона в набор значений
        values.put(COLUMN_EMAIL, email); // Добавляем email в набор значений

        // Устанавливаем роль пользователя по умолчанию как "user"
        values.put(COLUMN_ROLE, "user"); // Устанавливаем роль

        long id = db.insert(TABLE_USERS, null, values); // Вставляем запись в таблицу и получаем id
        db.close(); // Закрываем базу для освобождения ресурсов
        return id; // Возвращаем id вставленной записи
    }

    /**
     * Метод для аутентификации пользователя по логину и паролю
     * @param login Логин пользователя
     * @param password Пароль пользователя
     * @return Курсор с данными пользователя, если аутентификация прошла успешно
     */
    public Cursor authenticateUser(String login, String password) {
        SQLiteDatabase db = dbHandler.getReadableDatabase(); // Получаем базу для чтения
        // Выполняем запрос для поиска пользователя с заданными логином и паролем
        return db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE username = ? AND password = ?",
                new String[]{login, password}
        );
    }

    /**
     * Метод для получения данных пользователя по его идентификатору
     * @param userId Идентификатор пользователя
     * @return Курсор с данными пользователя
     */
    public Cursor getUserData(int userId) {
        SQLiteDatabase db = dbHandler.getReadableDatabase(); // Получаем базу для чтения
        // Выполняем запрос для выбора пользователя по id
        return db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE id = ?",
                new String[]{String.valueOf(userId)}
        );
    }

    /**
     * Метод для обновления данных пользователя
     * @param userId Идентификатор пользователя
     * @param login Новый логин пользователя
     * @param password Новый пароль пользователя
     * @param phone Новый номер телефона
     * @param email Новый email пользователя
     * @return Количество обновленных строк
     */
    public int updateUser(
            int userId,
            String login,
            String password,
            String phone,
            String email
    ) {
        SQLiteDatabase db = dbHandler.getWritableDatabase(); // Получаем базу для записи
        ContentValues values = new ContentValues(); // Создаем набор новых значений
        values.put(COLUMN_LOGIN, login); // Обновляем логин пользователя
        values.put(COLUMN_PASSWORD, password); // Обновляем пароль пользователя
        values.put(COLUMN_PHONE, phone); // Обновляем номер телефона
        values.put(COLUMN_EMAIL, email); // Обновляем email

        // Выполняем обновление записи, где id соответствует заданному, и возвращаем количество затронутых строк
        return db.update(
                TABLE_USERS,
                values,
                COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );
    }

    /**
     * Метод для удаления пользователя по его идентификатору
     * @param userId Идентификатор пользователя для удаления
     */
    public void deleteUser(int userId) {
        SQLiteDatabase db = dbHandler.getWritableDatabase(); // Получаем базу для записи
        // Удаляем пользователя, где id равен заданному значению
        db.delete(TABLE_USERS, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        db.close(); // Закрываем базу для освобождения ресурсов
    }
}