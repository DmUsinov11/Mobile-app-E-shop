package com.example.e_shop.DB;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/*
 * Класс-обработчик базы данных
 * Этот класс отвечает за создание и обновление базы данных
 */
public class DataBaseHandler extends SQLiteOpenHelper {
    // Имя базы данных
    private static final String DATABASE_NAME = "e_shop.db";
    // Версия базы данных
    private static final int DATABASE_VERSION = 1;

    /**
     * Конструктор, инициализирующий базу данных
     * @param context Контекст приложения
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public DataBaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION); // Инициализация SQLiteOpenHelper
        copyDatabaseFromAssets(context); // Копирование базы данных из assets, если она не существует
    }

    /**
     * Копирует базу данных из папки assets в системное хранилище
     * @param context Контекст приложения
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void copyDatabaseFromAssets(Context context) {
        // Получаем путь к базе данных на устройстве
        String outFileName = context.getDatabasePath(DATABASE_NAME).getPath();
        File databaseFile = new File(outFileName);

        // Если база данных не существует, выполняем копирование
        if (!databaseFile.exists()) {
            InputStream inputStream = null; // Поток для чтения базы данных из assets
            OutputStream outputStream = null; // Поток для записи базы данных на устройство

            try {
                // Открываем базу данных из assets
                inputStream = context.getAssets().open(DATABASE_NAME);
                // Создаем директорию, если ее нет
                databaseFile.getParentFile().mkdirs();
                // Открываем поток для записи в базу данных
                outputStream = new FileOutputStream(outFileName);

                byte[] buffer = new byte[1024]; // Буфер для копирования данных
                int length; // Переменная для хранения количества прочитанных байтов
                // Читаем данные из inputStream и записываем их в outputStream
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                // Сбрасываем данные в поток
                outputStream.flush();
            } catch (IOException e) {
                // Обработка исключения при копировании базы данных
            } finally {
                try {
                    if (outputStream != null) outputStream.close(); // Закрываем поток записи
                    if (inputStream != null) inputStream.close();   // Закрываем поток чтения
                } catch (IOException e) {
                    // Обработка ошибки при закрытии потоков
                }
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Ничего не делаем, так как база предзаполненная
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // При обновлении версии базы вызываем onCreate
        onCreate(db);
    }

    /**
     * Возвращает список всех категорий
     * @return Список названий категорий
     */
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>(); // Список для хранения категорий
        SQLiteDatabase db = this.getReadableDatabase(); // Получаем экземпляр базы для чтения
        Cursor cursor = db.rawQuery("SELECT name FROM Categories", null); // Выполняем запрос к базе
        while (cursor.moveToNext()) { // Итерируем по всем записям курсора
            categories.add(cursor.getString(0)); // Добавляем название категории в список
        }
        cursor.close(); // Закрываем курсор
        return categories; // Возвращаем список категорий
    }
}