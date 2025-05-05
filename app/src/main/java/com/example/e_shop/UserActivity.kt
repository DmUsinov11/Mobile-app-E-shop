package com.example.e_shop

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import com.example.e_shop.DB.DataBaseHandler
import com.example.e_shop.DB.UsersTableHandler
import com.example.e_shop.DataClasses.User

// Класс UserActivity отвечает за экраны авторизации и регистрации
class UserActivity : ComponentActivity() {

    // Объявляем переменные для элементов пользовательского интерфейса
    private lateinit var loginEditText: EditText  // Текстовое поле для ввода логина
    private lateinit var passwordEditText: EditText  // Текстовое поле для ввода пароля
    private lateinit var loginButton: Button  // Кнопка для входа пользователя
    private lateinit var registerButton: Button  // Кнопка для перехода к регистрации

    // Объявляем переменную для работы с базой данных
    private lateinit var dbHandler: DataBaseHandler  // Обработчик базы данных

    /**
     * Метод onCreate вызывается при запуске активности
     * @param savedInstanceState Сохранённое состояние активности
     */
    @RequiresApi(Build.VERSION_CODES.O)  // Минимальный уровень API – Android O
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)  // Вызов базовой реализации onCreate
        setContentView(R.layout.activity_user)  // Устанавливаем разметку для активности из activity_user.xml

        dbHandler = DataBaseHandler(this)  // Инициализируем объект dbHandler текущим контекстом

        // Инициализируем UI компоненты, используя findViewById с идентификаторами из XML разметки
        loginEditText = findViewById(R.id.loginEditText)  // Находим элемент для ввода логина
        passwordEditText = findViewById(R.id.passwordEditText)  // Находим элемент для ввода пароля
        loginButton = findViewById(R.id.loginButton)  // Находим кнопку для входа
        registerButton = findViewById(R.id.registerButton)  // Находим кнопку для регистрации

        // Получаем SharedPreferences для проверки, сохранён ли пользователь
        val prefs = getSharedPreferences("EshopPrefs", Context.MODE_PRIVATE)  // Открываем SharedPreferences с именем EshopPrefs
        val savedUserId = prefs.getInt("SAVED_USER_ID", -1)  // Считываем сохранённый идентификатор пользователя (-1, если отсутствует)

        // Если сохранённый идентификатор найден, пытаемся получить данные пользователя из базы
        if (savedUserId != -1) {
            val userTableHandler = UsersTableHandler(dbHandler)  // Создаем объект для работы с таблицей пользователей
            val cursor = userTableHandler.getUserData(savedUserId)  // Выполняем запрос на получение данных пользователя по id
            if (cursor != null && cursor.moveToFirst()) {  // Если курсор содержит запись
                val user = User.createFromCursor(cursor)  // Создаем объект User из данных курсора
                cursor.close()  // Закрываем курсор после получения данных
                goToProfile(user)  // Переходим на экран профиля с данным пользователем
            }
            cursor?.close()  // Если курсор не равен null, закрываем его (на случай, если условие не выполнено)
        }

        // Обработчик нажатия кнопки входа
        loginButton.setOnClickListener {
            val login = loginEditText.text.toString().trim()  // Получаем и обрезаем введённый логин (параметр login)
            val password = passwordEditText.text.toString().trim()  // Получаем и обрезаем введённый пароль (параметр password)

            // Если логин и пароль не пусты, продолжаем процесс аутентификации
            if (login.isNotEmpty() && password.isNotEmpty()) {
                val userTableHandler = UsersTableHandler(dbHandler)  // Создаем экземпляр UsersTableHandler для взаимодействия с БД
                val cursor = userTableHandler.authenticateUser(login, password)  // Выполняем запрос для проверки логина и пароля
                if (cursor != null && cursor.moveToFirst()) {  // Если пользователь найден (курсор содержит запись)
                    val user = User.createFromCursor(cursor)  // Создаем объект пользователя из данных курсора
                    cursor.close()  // Закрываем курсор после использования

                    Toast.makeText(applicationContext, "Успешная авторизация!", Toast.LENGTH_SHORT).show()  // Сообщаем об успешной авторизации

                    // Сохраняем идентификатор пользователя в SharedPreferences для поддержки сессии
                    prefs.edit().putInt("SAVED_USER_ID", user.id).apply()  // Сохраняем значение SAVED_USER_ID без точки

                    goToProfile(user)  // Переходим на экран профиля с авторизованным пользователем
                } else {
                    Toast.makeText(applicationContext, "Неверный логин или пароль!", Toast.LENGTH_SHORT).show()  // Выводим сообщение об ошибке, если аутентификация не пройдена
                    cursor?.close()  // Если курсор не null, закрываем его
                }
            } else {
                // Если поля логина или пароля пусты, уведомляем пользователя
                Toast.makeText(applicationContext, "Пожалуйста, введите логин и пароль!", Toast.LENGTH_SHORT).show()  // Выводим сообщение о необходимости заполнения полей
            }
        }

        // Обработчик нажатия кнопки регистрации
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)  // Создаем Intent для перехода на активность регистрации
            startActivity(intent)  // Запускаем активность регистрации
        }
    }

    /**
     * Метод для перехода на экран профиля
     * @param user Объект пользователя, который передается в профиль
     */
    private fun goToProfile(user: User) {
        val intent = Intent(this, ProfileActivity::class.java)  // Создаем Intent для запуска активности профиля
        intent.putExtra("CURRENT_USER", user as Parcelable)  // Добавляем объект пользователя в Intent в формате Parcelable
        startActivity(intent)  // Запускаем активность профиля
        finish()  // Завершаем текущую активность, чтобы пользователь не мог вернуться назад
    }
}
