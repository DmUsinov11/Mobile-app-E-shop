package com.example.e_shop.DB;

import android.content.ContentValues;  // Импорт класса для создания набора значений для базы данных
import android.database.Cursor;  // Импорт класса для работы с результатами запроса (курсором)
import android.database.sqlite.SQLiteDatabase;  // Импорт класса для работы с базой данных SQLite

/**
 * Класс-обработчик таблицы деталей заказа
 */
public class OrderDetailsTableHandler {

    private DataBaseHandler dbHandler; // Поле для хранения обработчика базы данных

    public static final String TABLE_ORDER_DETAILS = "OrderDetails"; // Имя таблицы деталей заказа
    public static final String COLUMN_ID = "id"; // Имя колонки с идентификатором записи
    public static final String COLUMN_ORDER_ID = "order_id"; // Имя колонки, содержащей идентификатор заказа
    public static final String COLUMN_PRODUCT_ID = "product_id"; // Имя колонки с идентификатором продукта
    public static final String COLUMN_QUANTITY = "quantity"; // Имя колонки с количеством товара
    public static final String COLUMN_PRICE_PER_ITEM = "price_per_item"; // Имя колонки с ценой за единицу товара

    /**
     * Конструктор класса OrderDetailsTableHandler
     * @param dbHandler Объект базы данных для доступа к данным
     */
    public OrderDetailsTableHandler(DataBaseHandler dbHandler) {
        this.dbHandler = dbHandler; // Инициализируем поле dbHandler переданным объектом
    }

    /**
     * Метод для добавления записи детали заказа
     * @param orderId Идентификатор заказа
     * @param productId Идентификатор продукта
     * @param quantity Количество продукта в заказе
     * @param pricePerItem Цена за единицу продукта
     * @return Идентификатор вставленной записи
     */
    public long addOrderDetail(int orderId, int productId, int quantity, double pricePerItem) {
        SQLiteDatabase db = dbHandler.getWritableDatabase(); // Получаем базу для записи
        ContentValues values = new ContentValues(); // Создаем объект для хранения данных вставки
        values.put(COLUMN_ORDER_ID, orderId); // Устанавливаем значение для колонки order_id
        values.put(COLUMN_PRODUCT_ID, productId); // Устанавливаем значение для колонки product_id
        values.put(COLUMN_QUANTITY, quantity); // Устанавливаем количество товара
        values.put(COLUMN_PRICE_PER_ITEM, pricePerItem); // Устанавливаем цену за единицу товара

        long id = db.insert(TABLE_ORDER_DETAILS, null, values); // Вставляем данные в таблицу и получаем id новой записи
        db.close(); // Закрываем базу для освобождения ресурсов
        return id; // Возвращаем id вставленной записи
    }

    /**
     * Метод для получения деталей заказа по id заказа
     * @param orderId Идентификатор заказа, для которого нужны детали
     * @return Курсор с данными, содержащими детали заказа
     */
    public Cursor getOrderDetailsByOrderId(int orderId) {
        SQLiteDatabase db = dbHandler.getReadableDatabase(); // Получаем базу для чтения
        // Выполняем запрос для выбора всех записей с указанным order_id
        return db.rawQuery("SELECT * FROM " + TABLE_ORDER_DETAILS + " WHERE " + COLUMN_ORDER_ID + " = ?", new String[]{String.valueOf(orderId)});
    }

    /**
     * Метод для обновления записи детали заказа
     * @param orderDetailId Идентификатор записи детали заказа
     * @param quantity Новое количество товара
     * @param pricePerItem Новая цена за единицу товара
     * @return Количество обновленных строк
     */
    public int updateOrderDetail(int orderDetailId, int quantity, double pricePerItem) {
        SQLiteDatabase db = dbHandler.getWritableDatabase(); // Получаем базу для записи
        ContentValues values = new ContentValues(); // Создаем объект для хранения обновляемых данных
        values.put(COLUMN_QUANTITY, quantity); // Обновляем значение количества товара
        values.put(COLUMN_PRICE_PER_ITEM, pricePerItem); // Обновляем цену за единицу товара

        // Выполняем обновление записи с заданным id и возвращаем число измененных строк
        return db.update(TABLE_ORDER_DETAILS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(orderDetailId)});
    }

    /**
     * Метод для удаления записи детали заказа
     * @param orderDetailId Идентификатор записи, которую нужно удалить
     */
    public void deleteOrderDetail(int orderDetailId) {
        SQLiteDatabase db = dbHandler.getWritableDatabase(); // Получаем базу для записи
        // Удаляем запись, где id соответствует orderDetailId
        db.delete(TABLE_ORDER_DETAILS, COLUMN_ID + " = ?", new String[]{String.valueOf(orderDetailId)});
        db.close(); // Закрываем базу для освобождения ресурсов
    }
}
