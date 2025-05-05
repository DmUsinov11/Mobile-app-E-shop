package com.example.e_shop

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.e_shop.DB.DataBaseHandler
import com.example.e_shop.DB.ProductsTableHandler
import com.example.e_shop.DB.UserCartTableHandler
import com.example.e_shop.DataClasses.Product
import com.example.e_shop.DataClasses.User
import com.example.e_shop.ui.theme.E_ShopTheme

// Класс ProductDetailActivity наследуется от ComponentActivity для работы с Compose
class ProductDetailActivity : ComponentActivity() {

    private var currentUser: User? = null  // Переменная для хранения текущего пользователя
    private var productId: Int? = null  // Переменная для хранения id продукта

    private lateinit var dbHandler: DataBaseHandler  // Обработчик базы данных
    private lateinit var productsTableHandler: ProductsTableHandler  // Обработчик таблицы продуктов
    private lateinit var cartHandler: UserCartTableHandler  // Обработчик таблицы корзины

    /**
     * Метод onCreate вызывается при создании активности
     * @param savedInstanceState состояние активности, если оно было сохранено
     */
    @RequiresApi(Build.VERSION_CODES.O)  // Требуется API уровня O
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)  // Вызов базового onCreate
        dbHandler = DataBaseHandler(this)  // Инициализация базы данных с контекстом активности
        productsTableHandler = ProductsTableHandler(dbHandler)  // Создание объекта для работы с таблицей продуктов
        cartHandler = UserCartTableHandler(dbHandler)  // Создание объекта для работы с таблицей корзины

        currentUser = intent.getParcelableExtra("CURRENT_USER")  // Извлекаем пользователя из интента
        productId = intent.getIntExtra("PRODUCT_ID", -1)  // Извлекаем id продукта из интента

        // Если пользователь не найден, перенаправляем на экран авторизации
        if (currentUser == null) {
            val intent = Intent(this, UserActivity::class.java)  // Создаем намерение для UserActivity
            startActivity(intent)  // Запускаем UserActivity
            finish()  // Завершаем текущую активность
            return  // Прерываем выполнение метода
        }

        // Устанавливаем содержимое экрана через Compose
        setContent {
            E_ShopTheme {  // Применяем тему приложения
                ProductDetailScreen()  // Отображаем экран деталей продукта
            }
        }
    }

    // Переопределяем onBackPressed для возврата результата
    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)  // Устанавливаем RESULT_OK, чтобы MainActivity обновилась
        super.onBackPressed()  // Вызываем базовую реализацию onBackPressed
    }

    /**
     * Компонент для отображения экрана деталей продукта
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ProductDetailScreen() {
        val product = remember { loadProductById(productId) }  // Запоминаем продукт, полученный из базы
        /*
           Параметр initialInCart определяет, добавлен ли продукт в корзину
           @param initialInCart Логическое значение, true если продукт в корзине, иначе false
        */
        val initialInCart = product?.let {
            currentUser?.id?.let { userId ->
                isProductInCart(userId, product.id)  // Проверяем наличие продукта в корзине
            }
        } ?: false
        val inCartState = remember { mutableStateOf(initialInCart) }  // Создаем изменяемое состояние для корзины

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Детали товара") },  // Заголовок верхней панели
                    navigationIcon = {
                        IconButton(onClick = {
                            setResult(Activity.RESULT_OK)  // Передаем результат об обновлении
                            finish()  // Завершаем активность и возвращаемся
                        }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")  // Иконка возврата
                        }
                    }
                )
            }
        ) { paddingValues ->
            product?.let {
                Column(
                    modifier = Modifier
                        .fillMaxSize()  // Заполняем всё доступное пространство
                        .padding(paddingValues)  // Учитываем отступы от Scaffold
                ) {
                    ProductDetailContent(it, inCartState)  // Вызываем компонент для вывода деталей продукта
                }
            } ?: run {
                Text(
                    text = "Товар не найден",  // Сообщение, если продукт не найден
                    modifier = Modifier.padding(16.dp)  // Отступы вокруг текста
                )
            }
        }
    }

    /**
     * Компонент для вывода подробной информации о продукте
     * @param product Объект продукта для отображения
     * @param inCartState Изменяемое состояние, указывающее, добавлен ли продукт в корзину
     */
    @Composable
    fun ProductDetailContent(product: Product, inCartState: MutableState<Boolean>) {
        val context = LocalContext.current  // Получаем текущий контекст из Compose
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {  // Создаем колонку с отступами
            product.image_url?.let { imageUrl ->  // Если URL изображения существует
                val imageUri = Uri.parse("file:///android_asset/$imageUrl")  // Преобразуем строку в URI
                Image(
                    painter = rememberImagePainter(imageUri),  // Загружаем изображение с помощью Coil
                    contentDescription = "Product Image",  // Описание изображения для доступности
                    modifier = Modifier
                        .fillMaxWidth()  // Изображение занимает всю ширину
                        .clip(RoundedCornerShape(8.dp))  // Скругляем углы изображения
                )
            }
            Spacer(modifier = Modifier.height(16.dp))  // Создаем вертикальный отступ
            Text(text = product.name, style = MaterialTheme.typography.titleLarge)  // Выводим название продукта
            Spacer(modifier = Modifier.height(8.dp))  // Отступ между элементами
            Text(text = product.description.orEmpty(), style = MaterialTheme.typography.bodyMedium)  // Вывод описания продукта, если оно не null
            Spacer(modifier = Modifier.height(8.dp))  // Дополнительный отступ
            Text(text = "Цена: $${product.price}", style = MaterialTheme.typography.titleMedium)  // Вывод цены продукта
            Spacer(modifier = Modifier.height(16.dp))  // Отступ перед кнопкой
            Button(
                onClick = {
                    if (currentUser == null) {  // Проверяем, авторизован ли пользователь
                        Toast.makeText(context, "Сначала необходимо авторизоваться", Toast.LENGTH_SHORT).show()  // Выводим сообщение, если нет
                        context.startActivity(Intent(context, UserActivity::class.java))  // Перенаправляем на экран авторизации
                    } else {
                        if (!inCartState.value) {  // Если продукт еще не добавлен в корзину
                            cartHandler.addItemToCart(currentUser!!.id, product.id, 1)  // Добавляем продукт в корзину
                            Toast.makeText(context, "Товар добавлен в корзину", Toast.LENGTH_SHORT).show()  // Сообщаем об успехе
                            inCartState.value = true  // Обновляем состояние, что товар в корзине
                            setResult(Activity.RESULT_OK)  // Сообщаем родительской активности об изменении
                        } else {  // Если товар уже в корзине
                            Toast.makeText(context, "Товар уже в корзине", Toast.LENGTH_SHORT).show()  // Выводим сообщение
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()  // Кнопка занимает всю ширину
            ) {
                if (inCartState.value) {  // Если товар в корзине
                    Icon(Icons.Filled.Check, contentDescription = "Добавлено")  // Выводим иконку подтверждения
                    Spacer(modifier = Modifier.width(4.dp))  // Отступ между иконкой и текстом
                    Text(text = "Уже в корзине")  // Текст кнопки
                } else {  // Если товар не добавлен
                    Icon(Icons.Filled.Add, contentDescription = "Добавить")  // Выводим иконку добавления
                    Spacer(modifier = Modifier.width(4.dp))  // Отступ между иконкой и текстом
                    Text(text = "Добавить в корзину")  // Текст кнопки
                }
            }
            Spacer(modifier = Modifier.height(16.dp))  // Отступ перед ссылкой возврата
            Text(
                text = "Вернуться на главную",  // Текст ссылки возврата
                modifier = Modifier
                    .clickable {  // Делаем текст кликабельным
                        setResult(Activity.RESULT_OK)  // Устанавливаем результат для обновления главного экрана
                        finish()  // Завершаем активность, возвращаясь на главную
                    }
                    .padding(8.dp)  // Отступ вокруг текста
            )
        }
    }

    /**
     * Функция для загрузки продукта по его id
     * @param productId Идентификатор продукта
     * @return Объект Product или null, если продукт не найден
     */
    private fun loadProductById(productId: Int?): Product? {
        if (productId == null || productId == -1) return null  // Если id недопустимый, возвращаем null
        val cursor = productsTableHandler.getAllProducts()  // Получаем курсор со всеми продуктами
        var product: Product? = null  // Переменная для хранения найденного продукта
        while (cursor.moveToNext()) {  // Итерируем по всем записям курсора
            val idIndex = cursor.getColumnIndex(ProductsTableHandler.COLUMN_ID)  // Получаем индекс колонки id
            val nameIndex = cursor.getColumnIndex(ProductsTableHandler.COLUMN_NAME)  // Получаем индекс колонки названия
            val descriptionIndex = cursor.getColumnIndex(ProductsTableHandler.COLUMN_DESCRIPTION)  // Получаем индекс колонки описания
            val priceIndex = cursor.getColumnIndex(ProductsTableHandler.COLUMN_PRICE)  // Получаем индекс колонки цены
            val imageUrlIndex = cursor.getColumnIndex(ProductsTableHandler.COLUMN_IMAGE_URL)  // Получаем индекс колонки изображения
            if (idIndex != -1 && nameIndex != -1 && descriptionIndex != -1 && priceIndex != -1) {  // Если все индексы корректны
                val id = cursor.getInt(idIndex)  // Считываем id продукта
                if (id == productId) {  // Если найден нужный id
                    val name = cursor.getString(nameIndex)  // Считываем название
                    val description = cursor.getString(descriptionIndex)  // Считываем описание
                    val price = cursor.getDouble(priceIndex)  // Считываем цену
                    val imageUrl = if (imageUrlIndex != -1) cursor.getString(imageUrlIndex) else null  // Считываем URL изображения, если возможно
                    product = Product(id, name, description, price, imageUrl, 1)  // Создаем объект продукта
                    break  // Прерываем цикл, так как продукт найден
                }
            }
        }
        cursor.close()  // Закрываем курсор после чтения
        return product  // Возвращаем найденный продукт или null
    }

    /**
     * Функция для проверки наличия продукта в корзине
     * @param userId Идентификатор пользователя
     * @param productId Идентификатор продукта
     * @return true если продукт найден в корзине, иначе false
     */
    private fun isProductInCart(userId: Int, productId: Int): Boolean {
        val cursor = cartHandler.getItemsInCart(userId)  // Получаем курсор с элементами корзины для пользователя
        val productIdColumnIndex = cursor.getColumnIndex(UserCartTableHandler.COLUMN_PRODUCT_ID)  // Получаем индекс колонки с id продукта
        var inCart = false  // Флаг, указывающий, найден ли продукт
        if (productIdColumnIndex != -1) {  // Если индекс корректный
            while (cursor.moveToNext()) {  // Перебираем записи курсора
                if (cursor.getInt(productIdColumnIndex) == productId) {  // Если найден нужный id
                    inCart = true  // Устанавливаем флаг в true
                    break  // Прерываем цикл, так как продукт найден
                }
            }
        }
        cursor.close()  // Закрываем курсор
        return inCart  // Возвращаем результат проверки
    }
}
