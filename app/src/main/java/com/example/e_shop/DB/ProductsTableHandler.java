package com.example.e_shop.DB;

import android.content.ContentValues;  // Импортируем ContentValues для создания пар ключ-значение
import android.database.Cursor;  // Импортируем Cursor для работы с результатами запросов
import android.database.sqlite.SQLiteDatabase;  // Импортируем SQLiteDatabase для работы с базой данных

/**
 * Класс для работы с таблицей продуктов в базе данных
 */
public class ProductsTableHandler {

    private DataBaseHandler dbHandler;  // Хранит ссылку на объект базы данных для выполнения операций
    public static final String TABLE_PRODUCTS = "Products";  // Имя таблицы, где хранятся продукты
    public static final String COLUMN_ID = "id";  // Название колонки с идентификатором продукта
    public static final String COLUMN_NAME = "name";  // Название колонки с названием продукта
    public static final String COLUMN_DESCRIPTION = "description";  // Название колонки с описанием продукта
    public static final String COLUMN_PRICE = "price";  // Название колонки с ценой продукта
    public static final String COLUMN_QUANTITY = "quantity";  // Название колонки с количеством продукта
    public static final String COLUMN_IMAGE_URL = "image_url";  // Название колонки с URL изображения продукта
    public static final String COLUMN_CATEGORY = "category";  // Название колонки с категорией продукта

    /**
     * Конструктор для инициализации объекта ProductsTableHandler
     * @param dbHandler Экземпляр DataBaseHandler для доступа к базе данных
     */
    public ProductsTableHandler(DataBaseHandler dbHandler) {
        this.dbHandler = dbHandler;  // Инициализируем объект dbHandler
    }

    /**
     * Метод для добавления нового продукта в базу данных
     * @param name Название продукта
     * @param description Описание продукта
     * @param price Цена продукта
     * @param quantity Количество продукта
     * @param imageUrl URL изображения продукта
     * @param category Категория продукта
     * @return Возвращает идентификатор вновь добавленной записи
     */
    public long addProduct(String name, String description, double price, int quantity, String imageUrl, String category) {
        SQLiteDatabase db = dbHandler.getWritableDatabase();  // Получаем базу данных в режиме записи
        ContentValues values = new ContentValues();  // Создаем ContentValues для хранения данных продукта
        values.put(COLUMN_NAME, name);  // Сохраняем название продукта
        values.put(COLUMN_DESCRIPTION, description);  // Сохраняем описание продукта
        values.put(COLUMN_PRICE, price);  // Сохраняем цену продукта
        values.put(COLUMN_QUANTITY, quantity);  // Сохраняем количество продукта
        values.put(COLUMN_IMAGE_URL, imageUrl);  // Сохраняем URL изображения продукта
        values.put(COLUMN_CATEGORY, category);  // Сохраняем категорию продукта
        long id = db.insert(TABLE_PRODUCTS, null, values);  // Выполняем вставку и получаем id новой записи
        db.close();  // Закрываем соединение с базой данных
        return id;  // Возвращаем идентификатор добавленного продукта
    }

    /**
     * Метод для получения всех продуктов из таблицы
     * @return Возвращает Cursor для итерации по списку продуктов
     */
    public Cursor getAllProducts() {
        SQLiteDatabase db = dbHandler.getReadableDatabase();  // Получаем базу данных в режиме чтения
        return db.rawQuery("SELECT * FROM " + TABLE_PRODUCTS, null);  // Выполняем SQL-запрос для извлечения всех продуктов
    }

    /**
     * Метод для обновления данных продукта
     * @param id Идентификатор продукта, данные которого обновляются
     * @param name Новое название продукта
     * @param description Новое описание продукта
     * @param price Новая цена продукта
     * @param quantity Новое количество продукта
     * @param imageUrl Новый URL изображения продукта
     * @param category Новая категория продукта
     * @return Возвращает количество строк, затронутых обновлением
     */
    public int updateProduct(int id, String name, String description, double price, int quantity, String imageUrl, String category) {
        SQLiteDatabase db = dbHandler.getWritableDatabase();  // Получаем базу данных в режиме записи
        ContentValues values = new ContentValues();  // Создаем объект для хранения обновляемых данных
        values.put(COLUMN_NAME, name);  // Обновляем название продукта
        values.put(COLUMN_DESCRIPTION, description);  // Обновляем описание продукта
        values.put(COLUMN_PRICE, price);  // Обновляем цену продукта
        values.put(COLUMN_QUANTITY, quantity);  // Обновляем количество продукта
        values.put(COLUMN_IMAGE_URL, imageUrl);  // Обновляем URL изображения продукта
        values.put(COLUMN_CATEGORY, category);  // Обновляем категорию продукта
        return db.update(TABLE_PRODUCTS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});  // Выполняем обновление по id и возвращаем число затронутых строк
    }

    /**
     * Метод для удаления продукта из таблицы
     * @param id Идентификатор продукта, который необходимо удалить
     */
    public void deleteProduct(int id) {
        SQLiteDatabase db = dbHandler.getWritableDatabase();  // Открываем базу данных в режиме записи
        db.delete(TABLE_PRODUCTS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});  // Удаляем запись, удовлетворяющую условию по id
        db.close();  // Закрываем базу данных после выполнения операции
    }

    /**
     * Метод для поиска продуктов по названию с учетом категории
     * @param query Строка поиска для названия продукта
     * @param categoryId Идентификатор категории для фильтрации
     * @return Возвращает Cursor с продуктами, удовлетворяющими условиям поиска
     */
    public Cursor searchProducts(String query, int categoryId) {
        SQLiteDatabase db = dbHandler.getReadableDatabase();  // Получаем базу данных в режиме чтения
        return db.rawQuery("SELECT * FROM Products WHERE LOWER(name) LIKE ? AND category_id = ?", new String[]{'%' + query.toLowerCase() + '%', String.valueOf(categoryId)});  // Выполняем запрос с фильтрацией по имени и категории
    }

    /**
     * Метод для поиска продуктов только по названию
     * @param query Строка поиска для названия продукта
     * @return Возвращает Cursor с продуктами, найденными по имени
     */
    public Cursor searchByProductName(String query) {
        SQLiteDatabase db = dbHandler.getReadableDatabase();  // Получаем базу данных в режиме чтения
        return db.rawQuery("SELECT * FROM Products WHERE LOWER(name) LIKE ?", new String[]{'%' + query.toLowerCase() + '%'});  // Выполняем запрос для поиска по имени
    }

    /**
     * Метод для поиска продуктов по категории
     * @param categoryId Идентификатор категории
     * @return Возвращает Cursor с продуктами, принадлежащими заданной категории
     */
    public Cursor searchByCategory(int categoryId) {
        SQLiteDatabase db = dbHandler.getReadableDatabase();  // Получаем базу данных для чтения
        return db.rawQuery("SELECT * FROM Products WHERE category_id = ?", new String[]{String.valueOf(categoryId)});  // Выполняем запрос для поиска продуктов по категории
    }
}
