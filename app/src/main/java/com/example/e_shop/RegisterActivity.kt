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

// Класс регистрации пользователя, наследует ComponentActivity
class RegisterActivity : ComponentActivity() {

    // lateinit переменная для работы с базой данных
    private lateinit var dbHandler: DataBaseHandler  // Обработчик базы данных будет инициализирован в onCreate
    // lateinit переменная для работы с таблицей пользователей
    private lateinit var userTableHandler: UsersTableHandler  // Объект для выполнения операций с таблицей пользователей

    // lateinit переменные для полей ввода и кнопки регистрации
    private lateinit var loginEditText: EditText  // Текстовое поле для ввода логина
    private lateinit var passwordEditText: EditText  // Текстовое поле для ввода пароля
    private lateinit var emailEditText: EditText  // Текстовое поле для ввода email
    private lateinit var phoneEditText: EditText  // Текстовое поле для ввода телефона
    private lateinit var registerBtn: Button  // Кнопка регистрации пользователя

    // Метод onCreate вызывается при создании активности
    @RequiresApi(Build.VERSION_CODES.O)  // Требуемый уровень API не ниже O
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)  // Вызываем метод родительского класса
        setContentView(R.layout.activity_register)  // Устанавливаем XML-разметку для активности

        dbHandler = DataBaseHandler(this)  // Инициализируем объект базы данных с текущим контекстом
        userTableHandler = UsersTableHandler(dbHandler)  // Инициализируем обработчик таблицы пользователей с помощью dbHandler

        loginEditText = findViewById(R.id.regLoginEditText)  // Получаем ссылку на EditText логина по ID из разметки
        passwordEditText = findViewById(R.id.regPasswordEditText)  // Получаем ссылку на EditText пароля по ID из разметки
        emailEditText = findViewById(R.id.regEmailEditText)  // Получаем ссылку на EditText email по ID из разметки
        phoneEditText = findViewById(R.id.regPhoneEditText)  // Получаем ссылку на EditText телефона по ID из разметки
        registerBtn = findViewById(R.id.registerBtn)  // Получаем ссылку на кнопку регистрации по ID из разметки

        // Устанавливаем обработчик клика на кнопку регистрации
        registerBtn.setOnClickListener {
            val login = loginEditText.text.toString().trim()  // Получаем введенный логин и удаляем пробелы по краям
            val password = passwordEditText.text.toString().trim()  // Получаем введенный пароль и удаляем пробелы
            val email = emailEditText.text.toString().trim()  // Получаем введенный email и удаляем пробелы
            val phone = phoneEditText.text.toString().trim()  // Получаем введенный телефон и удаляем пробелы

            // Проверяем логин и пароль
            if (login.isEmpty() || password.isEmpty()) {
                Toast.makeText(applicationContext, "Логин и пароль обязательны!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Проверяем корректность email, если он введён
            val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.(ru|com)$")
            if (email.isNotEmpty() && !email.matches(emailRegex)) {
                Toast.makeText(applicationContext, "Некорректный Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Вызываем метод для добавления пользователя в таблицу пользователей и получаем его идентификатор
            val userId = userTableHandler.addUser(login, password, phone, email)  // Добавляем пользователя в базу данных и сохраняем его id

            // Если идентификатор больше 0, значит пользователь успешно добавлен
            if (userId > 0) {
                Toast.makeText(applicationContext, "Пользователь зарегистрирован!", Toast.LENGTH_SHORT).show()

                // Авторизуем пользователя и получаем курсор
                val cursor = userTableHandler.authenticateUser(login, password)
                if (cursor != null && cursor.moveToFirst()) {
                    val newUser = User.createFromCursor(cursor)  // Создаем объект User из курсора
                    cursor.close()  // Закрываем курсор

                    // Сохраняем ID в SharedPreferences
                    val prefs = getSharedPreferences("EshopPrefs", Context.MODE_PRIVATE)
                    prefs.edit().putInt("SAVED_USER_ID", newUser.id).apply()

                    // Переходим на экран профиля
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("CURRENT_USER", newUser as Parcelable)
                    startActivity(intent)
                    finish()  // Завершаем активность регистрации
                }
            } else {
                Toast.makeText(applicationContext, "Ошибка при регистрации!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
