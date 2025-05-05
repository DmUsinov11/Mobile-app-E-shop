package com.example.e_shop.DB;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

// Класс для работы с таблицей корзины пользователя
public class UserCartTableHandler {

    private DataBaseHandler dbHandler;  // Поле для хранения объекта DataBaseHandler, необходимого для доступа к базе данных

    public static final String TABLE_USERCART = "UserCart";  // Имя таблицы корзины
    public static final String COLUMN_CART_ID = "id";  // Название колонки с уникальным идентификатором записи
    public static final String COLUMN_USER_ID = "user_id";  // Название колонки с идентификатором пользователя
    public static final String COLUMN_PRODUCT_ID = "product_id";  // Название колонки с идентификатором продукта
    public static final String COLUMN_QUANTITY = "quantity";  // Название колонки с количеством товара в корзине

    /**
     * Конструктор класса UserCartTableHandler
     * @param dbHandler Объект DataBaseHandler для работы с базой данных
     */
    public UserCartTableHandler(DataBaseHandler dbHandler) {
        this.dbHandler = dbHandler;  // Инициализация поля dbHandler полученным объектом
    }

    /**
     * Метод добавления товара в корзину пользователя
     * @param userId Идентификатор пользователя
     * @param productId Идентификатор продукта
     * @param quantity Количество товара
     * @return Возвращает идентификатор добавленной записи в таблице
     */
    public long addItemToCart(int userId, int productId, int quantity) {
        SQLiteDatabase db = dbHandler.getWritableDatabase();  // Получаем базу данных в режиме записи
        ContentValues values = new ContentValues();  // Создаем объект ContentValues для хранения данных
        values.put(COLUMN_USER_ID, userId);  // Добавляем значение userId в ContentValues
        values.put(COLUMN_PRODUCT_ID, productId);  // Добавляем значение productId в ContentValues
        values.put(COLUMN_QUANTITY, quantity);  // Добавляем количество товара в ContentValues
        long id = db.insert(TABLE_USERCART, null, values);  // Выполняем вставку и получаем id новой записи
        db.close();  // Закрываем базу данных после выполнения операции
        return id;  // Возвращаем id добавленной записи
    }

    /**
     * Метод получения всех товаров в корзине для конкретного пользователя
     * @param userId Идентификатор пользователя
     * @return Возвращает Cursor, содержащий записи корзины для данного пользователя
     */
    public Cursor getItemsInCart(int userId) {
        SQLiteDatabase db = dbHandler.getReadableDatabase();  // Получаем базу данных в режиме чтения
        return db.rawQuery("SELECT * FROM " + TABLE_USERCART + " WHERE " + COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)});  // Выполняем запрос для выбора всех записей, где user_id равен указанному значению
    }

    /**
     * Метод обновления количества товара в корзине
     * @param userId Идентификатор пользователя
     * @param productId Идентификатор продукта
     * @param newQuantity Новое количество товара
     * @return Возвращает количество обновленных строк
     */
    public int updateItemQuantityInCart(int userId, int productId, int newQuantity) {
        SQLiteDatabase db = dbHandler.getWritableDatabase();  // Получаем базу данных в режиме записи
        ContentValues values = new ContentValues();  // Создаем ContentValues для обновляемых данных
        values.put(COLUMN_QUANTITY, newQuantity);  // Обновляем значение количества товара
        // Выполняем обновление записи с условием совпадения user_id и product_id
        return db.update(TABLE_USERCART, values, COLUMN_USER_ID + " = ? AND " + COLUMN_PRODUCT_ID + " = ?",
                new String[]{String.valueOf(userId), String.valueOf(productId)});
    }

    /**
     * Метод получения товаров в корзине вместе с информацией о продуктах
     * @param userId Идентификатор пользователя
     * @return Возвращает Cursor с результатами объединенного запроса корзины и таблицы продуктов
     */
    public Cursor getCartProductsForUser(int userId) {
        SQLiteDatabase db = dbHandler.getReadableDatabase();  // Получаем базу данных для чтения
        // Формируем запрос с объединением таблиц корзины и продуктов для получения полной информации о товарах
        String query = "SELECT p.*, uc." + COLUMN_QUANTITY + " AS cart_quantity FROM " + TABLE_USERCART + " uc INNER JOIN " +
                ProductsTableHandler.TABLE_PRODUCTS + " p ON uc." + COLUMN_PRODUCT_ID + " = p." + ProductsTableHandler.COLUMN_ID + " WHERE uc." + COLUMN_USER_ID + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(userId)});  // Выполняем запрос и возвращаем Cursor
    }

    /**
     * Метод удаления конкретного товара из корзины
     * @param userId Идентификатор пользователя
     * @param productId Идентификатор продукта для удаления
     */
    public void removeItemFromCart(int userId, int productId) {
        SQLiteDatabase db = dbHandler.getWritableDatabase();  // Получаем базу данных в режиме записи
        // Выполняем удаление записи, где совпадают user_id и product_id
        db.delete(TABLE_USERCART, COLUMN_USER_ID + " = ? AND " + COLUMN_PRODUCT_ID + " = ?",
                new String[]{String.valueOf(userId), String.valueOf(productId)});
        db.close();  // Закрываем соединение с базой данных
    }

    /**
     * Метод для очистки корзины пользователя
     * @param userId Идентификатор пользователя, для которого необходимо очистить корзину
     */
    public void clearUserCart(int userId) {
        SQLiteDatabase db = dbHandler.getWritableDatabase();  // Получаем базу данных для записи
        // Удаляем все записи, соответствующие определенному user_id
        db.delete(TABLE_USERCART, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        db.close();  // Закрываем базу данных после выполнения операции
    }
}