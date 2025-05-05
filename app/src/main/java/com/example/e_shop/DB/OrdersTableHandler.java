package com.example.e_shop.DB;

import android.content.ContentValues; // Импортируем класс для создания набора значений для базы
import android.database.Cursor; // Импортируем класс для работы с курсором
import android.database.sqlite.SQLiteDatabase; // Импортируем класс для работы с базой данных SQLite

/**
 * Класс-обработчик таблицы заказов
 * @param dbHandler Объект базы данных для доступа к таблице заказов
 */
public class OrdersTableHandler {

    private DataBaseHandler dbHandler; // Обработчик базы данных

    public static final String TABLE_ORDERS = "Orders"; // Имя таблицы заказов
    public static final String COLUMN_ORDER_ID = "id"; // Имя колонки с идентификатором заказа
    public static final String COLUMN_USER_ID = "user_id"; // Имя колонки с идентификатором пользователя
    public static final String COLUMN_ORDER_DATE = "order_date"; // Имя колонки с датой заказа
    public static final String COLUMN_STATUS = "status"; // Имя колонки с статусом заказа
    public static final String COLUMN_TOTAL_PRICE = "total_price"; // Имя колонки с общей стоимостью заказа
    public static final String COLUMN_DELIVERY_ADDRESS = "delivery_address"; // Имя колонки с адресом доставки

    /**
     * Конструктор класса OrdersTableHandler
     * @param dbHandler Обработчик базы данных, используемый для выполнения операций с таблицей
     */
    public OrdersTableHandler(DataBaseHandler dbHandler) {
        this.dbHandler = dbHandler; // Сохраняем переданный объект базы данных
    }

    /**
     * Метод для добавления нового заказа
     * @param userId Идентификатор пользователя, разместившего заказ
     * @param totalPrice Общая стоимость заказа
     * @param deliveryAddress Адрес доставки заказа
     * @return Идентификатор вставленной записи заказа
     */
    public long addOrder(int userId, double totalPrice, String deliveryAddress) {
        SQLiteDatabase db = dbHandler.getWritableDatabase(); // Получаем базу данных для записи
        ContentValues values = new ContentValues(); // Создаем объект для набора значений
        values.put(COLUMN_USER_ID, userId); // Записываем идентификатор пользователя
        values.put(COLUMN_TOTAL_PRICE, totalPrice); // Записываем общую стоимость заказа
        values.put(COLUMN_DELIVERY_ADDRESS, deliveryAddress); // Записываем адрес доставки заказа

        long id = db.insert(TABLE_ORDERS, null, values); // Вставляем данные и получаем идентификатор записи
        db.close(); // Закрываем базу данных
        return id; // Возвращаем идентификатор вставленной записи
    }

    /**
     * Метод для получения заказов по идентификатору пользователя
     * @param userId Идентификатор пользователя, заказы которого нужно получить
     * @return Курсор с данными заказов пользователя
     */
    public Cursor getOrdersByUserId(int userId) {
        SQLiteDatabase db = dbHandler.getReadableDatabase(); // Получаем базу данных для чтения
        // Выполняем запрос для выборки заказов, где user_id соответствует переданному значению
        return db.rawQuery("SELECT * FROM " + TABLE_ORDERS + " WHERE " + COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
    }

    /**
     * Метод для обновления статуса заказа
     * @param orderId Идентификатор заказа
     * @param status Новый статус заказа
     * @return Количество обновленных строк
     */
    public int updateOrderStatus(int orderId, String status) {
        SQLiteDatabase db = dbHandler.getWritableDatabase(); // Получаем базу данных для записи
        ContentValues values = new ContentValues(); // Создаем объект для новых значений
        values.put(COLUMN_STATUS, status); // Обновляем значение статуса

        // Обновляем запись, где order_id равен переданному значению, и возвращаем количество обновленных строк
        return db.update(TABLE_ORDERS, values, COLUMN_ORDER_ID + " = ?", new String[]{String.valueOf(orderId)});
    }

    /**
     * Метод для обновления общей стоимости заказа
     * @param orderId Идентификатор заказа
     * @param totalPrice Новая общая стоимость заказа
     * @return Количество обновленных строк
     */
    public int updateOrderTotalPrice(int orderId, double totalPrice) {
        SQLiteDatabase db = dbHandler.getWritableDatabase(); // Получаем базу для записи
        ContentValues values = new ContentValues(); // Создаем объект для новых значений
        values.put(COLUMN_TOTAL_PRICE, totalPrice); // Обновляем общую стоимость заказа

        // Выполняем обновление и возвращаем количество затронутых строк
        return db.update(TABLE_ORDERS, values, COLUMN_ORDER_ID + " = ?", new String[]{String.valueOf(orderId)});
    }

    /**
     * Метод для удаления заказа
     * @param orderId Идентификатор заказа для удаления
     */
    public void deleteOrder(int orderId) {
        SQLiteDatabase db = dbHandler.getWritableDatabase(); // Получаем базу для записи
        // Удаляем заказ, где order_id равен переданному значению
        db.delete(TABLE_ORDERS, COLUMN_ORDER_ID + " = ?", new String[]{String.valueOf(orderId)});
        db.close(); // Закрываем базу данных
    }
}
