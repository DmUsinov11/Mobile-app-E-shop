package com.example.e_shop

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.e_shop.DB.DataBaseHandler
import com.example.e_shop.DB.OrderDetailsTableHandler
import com.example.e_shop.DB.OrdersTableHandler
import com.example.e_shop.DB.UsersTableHandler
import com.example.e_shop.DataClasses.Order
import com.example.e_shop.DataClasses.User
import com.example.e_shop.ui.theme.E_ShopTheme

// Класс ProfileActivity наследуется от ComponentActivity для работы с Compose
class ProfileActivity : ComponentActivity() { // Начало определения активности профиля

    private var currentUser: User? = null  // Переменная для хранения текущего пользователя (или null)
    private lateinit var dbHandler: DataBaseHandler  // Обработчик базы данных, будет инициализирован позднее
    private lateinit var usersTableHandler: UsersTableHandler  // Обработчик таблицы пользователей
    private lateinit var ordersTableHandler: OrdersTableHandler  // Обработчик таблицы заказов
    private lateinit var orderDetailsTableHandler: OrderDetailsTableHandler  // Обработчик таблицы деталей заказа

    /**
     * Метод onCreate вызывается при создании активности
     * @param savedInstanceState Состояние активности, если было сохранено
     */
    @RequiresApi(Build.VERSION_CODES.O) // Аннотация, указывающая, что метод требует API уровня O и выше
    override fun onCreate(savedInstanceState: Bundle?) { // Переопределение метода onCreate
        super.onCreate(savedInstanceState) // Вызов реализации суперкласса onCreate
        dbHandler = DataBaseHandler(this) // Инициализация обработчика базы данных с текущим контекстом
        usersTableHandler = UsersTableHandler(dbHandler) // Инициализация обработчика таблицы пользователей
        ordersTableHandler = OrdersTableHandler(dbHandler) // Инициализация обработчика таблицы заказов
        orderDetailsTableHandler = OrderDetailsTableHandler(dbHandler) // Инициализация обработчика деталей заказа

        currentUser = intent.getParcelableExtra("CURRENT_USER") // Получение переданного объекта User из Intent

        setContent { // Установка UI с помощью Jetpack Compose
            E_ShopTheme { // Применение темы приложения
                ProfileScreen() // Отображение компонуемого экрана профиля
            }
        }
    } // Конец метода onCreate

    @OptIn(ExperimentalMaterial3Api::class) // Опциональное использование экспериментального API Material3
    @Composable
    fun ProfileScreen() { // Компонуемая функция экрана профиля
        val context = LocalContext.current // Получение текущего контекста внутри Compose
        if (currentUser == null) { // Проверка, авторизован ли пользователь
            LaunchedEffect(Unit) { // Эффект, выполняемый при первой композиции
                Toast.makeText(context, "Необходимо авторизоваться", Toast.LENGTH_SHORT).show() // Показ сообщения
                context.startActivity(Intent(context, UserActivity::class.java)) // Переход на экран входа
                finish() // Завершение текущей активности
            }
            return // Выход из функции, если пользователь не авторизован
        }

        val userOrders = remember { fetchUserOrders(currentUser!!.id) } // Запрос заказов пользователя и запоминание результата
        var showEditDialog by remember { mutableStateOf(false) } // Состояние видимости диалога редактирования

        Scaffold( // Опорный макет Material3 с TopAppBar и контентом
            topBar = { // Определение верхней панели
                TopAppBar( // Компонент верхней панели приложения
                    title = { Text("Профиль") }, // Заголовок панели
                    navigationIcon = { // Иконка навигации (домой)
                        IconButton(onClick = { // Обработчик клика по иконке
                            startActivity(Intent(this@ProfileActivity, MainActivity::class.java)) // Переход на главную активность
                            finish() // Завершение текущей активности
                        }) {
                            Icon(Icons.Filled.Home, contentDescription = "Главная") // Отображение иконки Home
                        }
                    }
                )
            }
        ) { paddingValues -> // Параметры отступов Scaffold
            Column( // Вертикальная компоновка контента
                modifier = Modifier
                    .fillMaxSize() // Заполнение всего доступного пространства
                    .padding(paddingValues) // Применение отступов Scaffold
            ) {
                UserInfoSection() // Отображение секции с данными пользователя

                // Кнопки Редактировать и Выйти
                Row( // Горизонтальная компоновка для кнопок
                    modifier = Modifier
                        .fillMaxWidth() // Занять всю ширину
                        .padding(16.dp), // Отступы вокруг Row
                    horizontalArrangement = Arrangement.SpaceEvenly // Равномерное расположение кнопок
                ) {
                    Button(onClick = { showEditDialog = true }) { // Кнопка открытия диалога редактирования
                        Text("Редактировать данные") // Надпись кнопки
                    }
                    Button( // Кнопка выхода из аккаунта
                        onClick = {
                            val prefs = getSharedPreferences("EshopPrefs", Context.MODE_PRIVATE) // Получение SharedPreferences
                            prefs.edit().remove("SAVED_USER_ID").apply() // Удаление сохранённого ID пользователя
                            startActivity(Intent(this@ProfileActivity, MainActivity::class.java)) // Переход на главную
                            finish() // Завершение активности
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer // Установка цвета кнопки
                        )
                    ) {
                        Text("Выйти") // Надпись кнопки
                    }
                }

                Divider( // Разделитель между секциями
                    modifier = Modifier
                        .fillMaxWidth() // Полная ширина
                        .padding(vertical = 12.dp), // Вертикальные отступы
                    thickness = 1.dp, // Толщина линии
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f) // Цвет с прозрачностью
                )

                Text( // Заголовок секции "Мои заказы"
                    text = "Мои заказы", // Текст заголовка
                    style = MaterialTheme.typography.titleLarge, // Стиль текста
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp) // Отступы вокруг текста
                )

                if (userOrders.isEmpty()) { // Проверка, есть ли заказы у пользователя
                    Text( // Сообщение об отсутствии заказов
                        text = "Заказов ещё не было", // Текст сообщения
                        style = MaterialTheme.typography.bodyMedium, // Стиль текста
                        modifier = Modifier
                            .fillMaxWidth() // Полная ширина
                            .padding(16.dp), // Отступы
                        color = MaterialTheme.colorScheme.onSurfaceVariant // Цвет текста
                    )
                } else {
                    LazyColumn { // Ленивый список для отображения заказов
                        items(userOrders) { order -> // Проход по каждому заказу
                            OrderItem(order) // Вызов компонента отображения одного заказа
                        }
                    }
                }
            }

            if (showEditDialog) { // Если нужно показать диалог редактирования
                EditUserDialog( // Вызов компонуемого диалога
                    user = currentUser!!, // Передача текущего пользователя
                    onDismiss = { showEditDialog = false }, // Скрытие диалога по отмене
                    onSave = { updatedUser -> // Обработка сохранения новых данных
                        val rows = usersTableHandler.updateUser( // Обновление пользователя в БД
                            updatedUser.id,
                            updatedUser.username,
                            updatedUser.password,
                            updatedUser.phone ?: "",
                            updatedUser.email ?: ""
                        )
                        if (rows > 0) { // Если обновление прошло успешно
                            Toast.makeText(context, "Данные обновлены", Toast.LENGTH_SHORT).show() // Показ сообщения об успехе
                            currentUser = updatedUser // Обновление локальной переменной пользователя
                        } else {
                            Toast.makeText(context, "Ошибка при обновлении", Toast.LENGTH_SHORT).show() // Показ сообщения об ошибке
                        }
                        showEditDialog = false // Скрытие диалога после сохранения
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class) // Опциональный Experimental API для Material3
    @Composable
    fun EditUserDialog( // Компонуемая функция диалога редактирования данных пользователя
        user: User, // Текущий пользователь
        onDismiss: () -> Unit, // Лямбда для обработки отмены
        onSave: (User) -> Unit // Лямбда для обработки сохранения
    ) {
        var username by remember { mutableStateOf(user.username) } // Локальное состояние логина
        var password by remember { mutableStateOf(user.password) } // Локальное состояние пароля
        var email by remember { mutableStateOf(user.email ?: "") } // Локальное состояние email
        var phone by remember { mutableStateOf(user.phone ?: "") } // Локальное состояние телефона
        var showErrorDialog by remember { mutableStateOf(false) } // Состояние диалога ошибки Email

        // Регэксп проверяет, что домен заканчивается на .ru или .com
        val emailPattern = remember {
            Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.(ru|com)$") // Паттерн для валидации email
        }

        if (showErrorDialog) { // Если нужно показать ошибку Email
            AlertDialog( // Диалог ошибки
                onDismissRequest = { showErrorDialog = false }, // Скрытие диалога по клику вне
                title = { Text("Ошибка") }, // Заголовок диалога
                text = { Text("Некорректный Email") }, // Текст ошибки
                confirmButton = {
                    TextButton(onClick = { showErrorDialog = false }) { // Кнопка подтверждения
                        Text("ОК") // Надпись кнопки
                    }
                }
            )
        }

        AlertDialog( // Основной диалог редактирования
            onDismissRequest = onDismiss, // Обработка отмены
            title = { Text("Редактировать данные") }, // Заголовок диалога
            text = { // Содержимое диалога с полями ввода
                Column {
                    OutlinedTextField(
                        value = username, // Текущее значение логина
                        onValueChange = { username = it }, // Обновление при вводе
                        label = { Text("Логин") }, // Надпись поля
                        modifier = Modifier.fillMaxWidth() // Полная ширина поля
                    )
                    OutlinedTextField(
                        value = password, // Текущее значение пароля
                        onValueChange = { password = it }, // Обновление при вводе
                        label = { Text("Пароль") }, // Надпись поля
                        modifier = Modifier.fillMaxWidth() // Полная ширина
                    )
                    OutlinedTextField(
                        value = email, // Текущее значение email
                        onValueChange = { email = it }, // Обновление при вводе
                        label = { Text("Email") }, // Надпись поля
                        isError = email.isNotEmpty() && !emailPattern.matches(email), // Показ ошибки при неверном формате
                        modifier = Modifier.fillMaxWidth() // Ширина поля
                    )
                    if (email.isNotEmpty() && !emailPattern.matches(email)) { // Если формат email неверен
                        Text( // Сообщение об условии
                            text = "Email должен оканчиваться на .ru или .com", // Текст подсказки
                            style = MaterialTheme.typography.bodySmall, // Стиль текста
                            color = MaterialTheme.colorScheme.error, // Цвет ошибки
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp) // Отступы
                        )
                    }
                    OutlinedTextField(
                        value = phone, // Текущее значение телефона
                        onValueChange = { phone = it }, // Обновление при вводе
                        label = { Text("Телефон") }, // Надпись поля
                        modifier = Modifier.fillMaxWidth() // Полная ширина
                    )
                }
            },
            confirmButton = { // Кнопка сохранения
                TextButton(onClick = {
                    if (email.isNotEmpty() && !emailPattern.matches(email)) { // Проверка корректности email
                        showErrorDialog = true // Показ диалога ошибки
                    } else {
                        onSave(user.copy(username = username, password = password, email = email, phone = phone)) // Вызов сохранения с новыми данными
                    }
                }) {
                    Text("Сохранить") // Надпись кнопки
                }
            },
            dismissButton = { // Кнопка отмены
                TextButton(onClick = onDismiss) {
                    Text("Отмена") // Надпись кнопки
                }
            }
        )
    }

    @Composable
    fun UserInfoSection() { // Компонуемая функция отображения информации пользователя
        Column(modifier = Modifier.padding(16.dp)) { // Вертикальная компоновка с отступами
            Text(
                text = "Привет, ${currentUser?.username}", // Приветствие с именем пользователя
                style = MaterialTheme.typography.titleLarge // Стиль заголовка
            )
            Spacer(modifier = Modifier.height(8.dp)) // Вертикальный отступ
            Spacer(modifier = Modifier.height(8.dp)) // Вертикальный отступ
            Text(text = "Email: ${currentUser?.email ?: "не указан"}") // Отображение email или текста по умолчанию
            Text(text = "Телефон: ${currentUser?.phone ?: "не указан"}") // Отображение телефона или текста по умолчанию
        }
    }

    @Composable
    fun OrderItem(order: Order) { // Компонуемая функция отображения одного заказа
        Card( // Карточка для заказа
            modifier = Modifier
                .fillMaxWidth() // Полная ширина
                .padding(8.dp), // Отступы вокруг карточки
            elevation = CardDefaults.cardElevation(4.dp) // Тень карточки
        ) {
            Column(modifier = Modifier.padding(16.dp)) { // Вертикальная компоновка внутри карточки
                Text(text = "Номер заказа: ${order.id}") // Отображение ID заказа
                Text(text = "Дата: ${order.orderDate}") // Отображение даты заказа
                Text(text = "Статус: ${order.status}") // Отображение статуса заказа
                Text(text = "Сумма: \$${order.totalPrice}") // Отображение суммы заказа
                Text(text = "Адрес: ${order.deliveryAddress}") // Отображение адреса доставки
            }
        }
    }

    private fun fetchUserOrders(userId: Int): List<Order> { // Функция получения списка заказов из базы
        val cursor = ordersTableHandler.getOrdersByUserId(userId) // Запрос к базе для получения заказов по ID пользователя
        val result = mutableListOf<Order>() // Создание изменяемого списка для результатов
        while (cursor.moveToNext()) { // Итерация по результатам курсора
            val idIndex = cursor.getColumnIndex("id") // Получение индекса столбца id
            val dateIndex = cursor.getColumnIndex("order_date") // Индекс столбца даты
            val statusIndex = cursor.getColumnIndex("status") // Индекс столбца статуса
            val priceIndex = cursor.getColumnIndex("total_price") // Индекс столбца суммы
            val addressIndex = cursor.getColumnIndex("delivery_address") // Индекс столбца адреса
            result.add( // Добавление нового объекта Order в список
                Order(
                    id = cursor.getInt(idIndex), // Чтение id заказа
                    userId = userId, // Установка ID пользователя
                    orderDate = cursor.getString(dateIndex), // Чтение даты заказа
                    status = cursor.getString(statusIndex), // Чтение статуса заказа
                    totalPrice = cursor.getDouble(priceIndex), // Чтение суммы заказа
                    deliveryAddress = cursor.getString(addressIndex) // Чтение адреса доставки
                )
            )
        }
        cursor.close() // Закрытие курсора после работы
        return result // Возврат списка заказов
    }
}