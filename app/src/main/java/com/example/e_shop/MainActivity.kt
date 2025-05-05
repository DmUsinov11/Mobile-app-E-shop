package com.example.e_shop

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.example.e_shop.DB.DataBaseHandler
import com.example.e_shop.DB.ProductsTableHandler
import com.example.e_shop.DB.UserCartTableHandler
import com.example.e_shop.DB.UsersTableHandler
import com.example.e_shop.DataClasses.Product
import com.example.e_shop.DataClasses.User
import com.example.e_shop.ui.theme.E_ShopTheme

// Основная активность приложения, отвечающая за отображение главного экрана
class MainActivity : ComponentActivity() {

    private lateinit var dbHandler: DataBaseHandler // Обработчик базы данных
    private lateinit var productsTableHandler: ProductsTableHandler // Обработчик таблицы продуктов
    private var currentUser: User? = null // Переменная для хранения текущего пользователя

    /**
     * Устанавливает текущего пользователя
     * @param user Объект пользователя
     */
    fun setCurrentUser(user: User?) {
        currentUser = user // Сохраняем пользователя в переменной
    }

    /**
     * Получает текущего пользователя
     * @return Объект текущего пользователя или null, если пользователь не авторизован
     */
    fun getCurrentUser(): User? {
        return currentUser // Возвращаем текущего пользователя
    }

    /**
     * Метод onCreate активности
     * @param savedInstanceState Состояние сохраненной активности
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Вызов базового метода onCreate
        dbHandler = DataBaseHandler(this) // Инициализация обработчика базы данных
        productsTableHandler = ProductsTableHandler(dbHandler) // Инициализация обработчика таблицы продуктов

        // 1) Проверяем, есть ли сохранённый userId
        val prefs = getSharedPreferences("EshopPrefs", Context.MODE_PRIVATE) // Получаем SharedPreferences для хранения настроек
        val savedUserId = prefs.getInt("SAVED_USER_ID", -1) // Извлекаем сохранённый идентификатор пользователя
        if (savedUserId != -1) { // Если сохранённый userId найден
            // Если есть, пробуем получить пользователя из БД
            val userCursor = UsersTableHandler(dbHandler).getUserData(savedUserId) // Получаем данные пользователя из БД
            if (userCursor != null && userCursor.moveToFirst()) { // Если данные найдены
                val user = User.createFromCursor(userCursor) // Создаем объект пользователя из курсора
                userCursor.close() // Закрываем курсор после использования
                setCurrentUser(user) // Устанавливаем текущего пользователя
            }
            userCursor?.close() // Дополнительная проверка и закрытие курсора
        } else { // Если сохранённого userId нет
            // Если в интенте передаётся пользователь (например, после авторизации),
            // берем его в качестве currentUser
            val intentUser = intent.getParcelableExtra<User>("CURRENT_USER") // Получаем пользователя из интента
            if (intentUser != null) { // Если пользователь передан
                setCurrentUser(intentUser) // Устанавливаем текущего пользователя
            }
        }

        // Запуск пользовательского интерфейса
        setContent {
            E_ShopTheme { // Применяем тему приложения
                MainScreen() // Вызываем функцию MainScreen для отображения главного экрана
            }
        }
    }

    // Метод onResume для обновления UI при возврате в активность
    override fun onResume() {
        super.onResume() // Вызов базового метода onResume
        setContent { // Обновляем содержимое через Compose
            E_ShopTheme { // Применяем тему
                MainScreen() // Вызываем функцию главного экрана
            }
        }
    }

    /**
     * Метод для обработки результата из дочерних активностей
     * @param requestCode Код запроса
     * @param resultCode Код результата
     * @param data Данные, переданные в результате
     */
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data) // Вызов базового метода
        if (requestCode == 200 && resultCode == Activity.RESULT_OK) { // Если результат соответствует ожиданиям
            setContent { // Обновляем содержимое
                E_ShopTheme { // Применяем тему
                    MainScreen() // Вызываем главный экран
                }
            }
        }
    }

    /**
     * Главный экран приложения
     */
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalCoilApi::class, ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen() {
        val context = LocalContext.current // Получаем локальный контекст
        var searchText by remember { mutableStateOf("") } // Состояние строки поиска
        var selectedCategoryIndex by remember { mutableStateOf(-1) } // Состояние выбранной категории

        Scaffold( // Основной макет экрана с верхней панелью и контентом
            topBar = {
                TopAppBar( // Верхняя панель приложения
                    title = { Text("E-Shop") }, // Заголовок панели
                    actions = { // Действия на панели (иконки)
                        // Иконка "Профиль" или "Авторизация"
                        IconButton(onClick = {
                            if (currentUser == null) { // Если пользователь не авторизован
                                val intent = Intent(context, UserActivity::class.java) // Создаем намерение для экрана авторизации
                                context.startActivity(intent) // Запускаем активность авторизации
                            } else { // Если пользователь авторизован
                                val intent = Intent(context, ProfileActivity::class.java) // Создаем намерение для экрана профиля
                                intent.putExtra("CURRENT_USER", currentUser as Parcelable) // Передаем текущего пользователя
                                context.startActivity(intent) // Запускаем активность профиля
                            }
                        }) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = "Личный кабинет") // Вывод иконки профиля
                        }

                        // Иконка "Корзина"
                        IconButton(onClick = {
                            if (currentUser == null) { // Если пользователь не авторизован
                                Toast.makeText(
                                    applicationContext, // Контекст приложения
                                    "Сначала необходимо авторизоваться", // Сообщение
                                    Toast.LENGTH_SHORT // Длительность показа сообщения
                                ).show() // Показываем сообщение
                            } else { // Если пользователь авторизован
                                val intent = Intent(context, CartActivity::class.java) // Создаем намерение для экрана корзины
                                intent.putExtra("CURRENT_USER", currentUser as Parcelable) // Передаем текущего пользователя
                                context.startActivity(intent) // Запускаем активность корзины
                            }
                        }) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "Корзина") // Вывод иконки корзины
                        }
                    }
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize() // Колонка занимает все пространство
                    .padding(top = 56.dp) // Отступ сверху для учета панели
            ) {
                // Вывод поля поиска и фильтрации по категориям
                SearchAndCategoryFilter(
                    searchText = searchText, // Передаем текущее значение строки поиска
                    onSearchTextChanged = { searchText = it }, // Callback обновления строки поиска
                    selectedCategoryIndex = selectedCategoryIndex, // Передаем выбранный индекс категории
                    onSelectedCategoryIndexChanged = { selectedCategoryIndex = it } // Callback изменения выбранной категории
                )
                // Отображение списка продуктов
                DisplayProducts(searchText, selectedCategoryIndex)
            }
        }
    }

    /**
     * Компонент для отображения поля поиска и выбора категории
     *
     * @param searchText Текущий текст поиска
     * @param onSearchTextChanged Callback для изменения текста поиска
     * @param selectedCategoryIndex Индекс выбранной категории
     * @param onSelectedCategoryIndexChanged Callback для изменения выбранной категории
     */
    @OptIn(ExperimentalCoilApi::class, ExperimentalMaterial3Api::class)
    @Composable
    fun SearchAndCategoryFilter(
        searchText: String, // Текущий текст поиска
        onSearchTextChanged: (String) -> Unit, // Callback для обновления текста поиска
        selectedCategoryIndex: Int, // Текущий индекс выбранной категории
        onSelectedCategoryIndexChanged: (Int) -> Unit // Callback для обновления выбранной категории
    ) {
        var isDropdownExpanded by remember { mutableStateOf(false) } // Состояние показа выпадающего списка
        val categories = listOf("Все категории") + dbHandler.getAllCategories() // Формирование списка категорий

        Row(
            modifier = Modifier
                .fillMaxWidth() // Ряд занимает всю ширину
                .padding(8.dp), // Отступ 8 dp
            verticalAlignment = Alignment.CenterVertically, // Центрирование по вертикали
            horizontalArrangement = Arrangement.SpaceBetween // Распределение элементов по краям
        ) {
            TextField(
                value = searchText, // Значение поля поиска
                onValueChange = onSearchTextChanged, // Callback при изменении текста
                label = { Text("Поиск") }, // Метка поля ввода
                singleLine = true, // Однострочное поле ввода
                modifier = Modifier.weight(9f) // Занимает 90% ширины ряда
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth() // Бокс занимает всю ширину
                .padding(start = 8.dp, end = 8.dp, top = 4.dp) // Отступы вокруг бокса
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), // Ряд занимает всю ширину бокса
                verticalAlignment = Alignment.CenterVertically, // Центрирование по вертикали
                horizontalArrangement = Arrangement.SpaceBetween // Элементы распределяются по краям
            ) {
                Text("Выберите категорию") // Вывод статического текста
                if (selectedCategoryIndex in categories.indices) { // Если выбран валидный индекс
                    Text(categories[selectedCategoryIndex]) // Вывод выбранной категории
                }
                Icon( // Вывод иконки стрелки вниз для показа выпадающего списка
                    Icons.Default.ArrowDropDown, // Иконка "Стрелка вниз"
                    contentDescription = null, // Нет описания, так как иконка декоративная
                    modifier = Modifier.clickable { isDropdownExpanded = true } // По клику открывается список
                )
            }

            DropdownMenu(
                expanded = isDropdownExpanded, // Показ выпадающего меню
                onDismissRequest = { isDropdownExpanded = false } // Callback скрытия меню
            ) {
                categories.forEachIndexed { index, category -> // Проходим по списку категорий с индексами
                    DropdownMenuItem(
                        onClick = { // Callback выбора категории
                            onSelectedCategoryIndexChanged(index) // Устанавливаем выбранный индекс
                            isDropdownExpanded = false // Скрываем выпадающее меню
                        },
                        text = { Text(text = category) } // Выводим название категории
                    )
                }
            }
        }
    }

    /**
     * Компонент для отображения списка продуктов
     *
     * @param searchText Текст для поиска продуктов
     * @param selectedCategoryIndex Индекс выбранной категории для фильтрации
     */
    @Composable
    fun DisplayProducts(searchText: String, selectedCategoryIndex: Int) {
        // Получаем курсор в зависимости от введенного текста и выбранной категории
        val productsCursor = when {
            searchText.isNotEmpty() && selectedCategoryIndex > 0 ->
                productsTableHandler.searchProducts(searchText, selectedCategoryIndex) // Поиск с фильтром по категории
            searchText.isNotEmpty() && selectedCategoryIndex == 0 ->
                productsTableHandler.searchByProductName(searchText) // Поиск только по названию
            selectedCategoryIndex > 0 ->
                productsTableHandler.searchByCategory(selectedCategoryIndex) // Фильтрация по категории
            else ->
                productsTableHandler.getAllProducts() // Получение всех продуктов
        }

        val products = mutableListOf<Product>() // Создаем список для хранения продуктов
        while (productsCursor.moveToNext()) { // Проходим по каждой записи курсора
            val idIndex = productsCursor.getColumnIndex(ProductsTableHandler.COLUMN_ID) // Получаем индекс колонки id
            val nameIndex = productsCursor.getColumnIndex(ProductsTableHandler.COLUMN_NAME) // Получаем индекс колонки названия
            val descriptionIndex = productsCursor.getColumnIndex(ProductsTableHandler.COLUMN_DESCRIPTION) // Получаем индекс колонки описания
            val priceIndex = productsCursor.getColumnIndex(ProductsTableHandler.COLUMN_PRICE) // Получаем индекс колонки цены
            val imageUrlIndex = productsCursor.getColumnIndex(ProductsTableHandler.COLUMN_IMAGE_URL) // Получаем индекс колонки URL изображения

            if (idIndex != -1 && nameIndex != -1 && descriptionIndex != -1 && priceIndex != -1) { // Проверяем корректность индексов
                val id = productsCursor.getInt(idIndex) // Считываем id продукта
                val name = productsCursor.getString(nameIndex) // Считываем название продукта
                val description = productsCursor.getString(descriptionIndex) // Считываем описание продукта
                val price = productsCursor.getDouble(priceIndex) // Считываем цену продукта
                val imageUrl = if (imageUrlIndex != -1) productsCursor.getString(imageUrlIndex) else null // Считываем URL изображения, если он существует

                products.add(Product(id, name, description, price, imageUrl, 1)) // Создаем объект продукта и добавляем его в список
            }
        }
        productsCursor.close() // Закрываем курсор после чтения

        // Отображаем список продуктов в колонке
        LazyColumn(
            modifier = Modifier
                .fillMaxSize() // Заполняет все доступное пространство
                .padding(8.dp) // Отступ 8 dp со всех сторон
        ) {
            itemsIndexed(products.chunked(2)) { _, rowProducts ->
                // Выводим ряд, содержащий по два продукта
                Row(
                    modifier = Modifier.fillMaxWidth(), // Ряд занимает всю ширину
                    horizontalArrangement = Arrangement.SpaceBetween // Элементы распределяются равномерно
                ) {
                    rowProducts.forEach { product ->
                        ProductItem(
                            product = product, // Передаем объект продукта
                            userId = getCurrentUser()?.id, // Передаем id текущего пользователя (если он есть)
                            cartHandler = UserCartTableHandler(dbHandler), // Создаем новый обработчик корзины
                            modifier = Modifier.weight(1f) // Каждый продукт занимает равную долю ширины ряда
                        )
                    }
                }
            }
        }
    }

    /**
     * Компонент для отображения одного продукта в списке
     *
     * @param product Объект продукта для отображения
     * @param userId Идентификатор текущего пользователя
     * @param cartHandler Обработчик операций с корзиной
     * @param modifier Модификатор для компонента
     */
    @Composable
    fun ProductItem(
        product: Product, // Объект продукта
        userId: Int?, // Идентификатор пользователя (может быть null)
        cartHandler: UserCartTableHandler?, // Обработчик корзины
        modifier: Modifier = Modifier // Модификатор по умолчанию
    ) {
        val context = LocalContext.current // Получаем текущий контекст

        // Проверяем, добавлен ли продукт в корзину (возвращает true/false)
        val productInCart = userId?.let { isProductInCart(it, product.id, cartHandler) } ?: false

        Card(
            modifier = modifier
                .padding(8.dp) // Внешние отступы карточки
                .clip(RoundedCornerShape(8.dp)) // Скругляем углы карточки
                .fillMaxWidth() // Карточка занимает всю ширину
                .clickable { // Обработка клика по карточке
                    val intent = Intent(context, ProductDetailActivity::class.java).apply {
                        putExtra("CURRENT_USER", currentUser as Parcelable) // Передаем текущего пользователя
                        putExtra("PRODUCT_ID", product.id) // Передаем id выбранного продукта
                    }
                    (context as? Activity)?.startActivityForResult(intent, 200) // Запускаем активность с ожиданием результата
                },
            elevation = CardDefaults.cardElevation(4.dp) // Высота тени карточки
        ) {
            Column(
                modifier = Modifier.padding(16.dp) // Внутренние отступы внутри карточки
            ) {
                product.image_url?.let { imageUrl -> // Если URL изображения существует
                    val imageUri = Uri.parse("file:///android_asset/$imageUrl") // Преобразуем строку в URI
                    Image(
                        painter = rememberImagePainter(imageUri), // Загружаем изображение с помощью Coil
                        contentDescription = "Product Image", // Описание изображения для доступности
                        modifier = Modifier
                            .fillMaxWidth() // Изображение занимает всю ширину карточки
                            .aspectRatio(1f) // Соотношение сторон 1:1
                    )
                }

                Spacer(modifier = Modifier.height(8.dp)) // Вертикальный отступ

                Text(
                    text = product.name, // Вывод названия продукта
                    style = MaterialTheme.typography.bodyMedium // Применяем стиль текста
                )
                Text(
                    text = product.description.orEmpty(), // Вывод описания продукта (или пустой строки, если null)
                    style = MaterialTheme.typography.bodySmall, // Стиль мелкого текста
                    maxLines = 2 // Ограничение на 2 строки текста
                )

                Spacer(modifier = Modifier.height(8.dp)) // Ещё один отступ

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween, // Элементы ряда распределяются по краям
                    modifier = Modifier.fillMaxWidth() // Ряд занимает всю ширину карточки
                ) {
                    Text(
                        text = "$${product.price}", // Вывод цены продукта
                        style = MaterialTheme.typography.bodyLarge // Стиль для крупного текста
                    )

                    IconButton(
                        onClick = {
                            // Если пользователь авторизован и продукт еще не добавлен в корзину
                            if (userId != null && cartHandler != null && !productInCart) {
                                cartHandler.addItemToCart(userId, product.id, 1) // Добавляем продукт в корзину
                                Toast.makeText(
                                    applicationContext, // Контекст приложения
                                    "Товар добавлен в корзину", // Сообщение об успехе
                                    Toast.LENGTH_SHORT // Кратковременное сообщение
                                ).show() // Показываем сообщение
                                // Обновляем UI, чтобы отобразить изменение (например, появление галочки)
                                setContent {
                                    E_ShopTheme {
                                        MainScreen() // Перерисовываем главный экран
                                    }
                                }
                            } else if (userId == null) { // Если пользователь не авторизован
                                Toast.makeText(
                                    applicationContext,
                                    "Сначала необходимо авторизоваться",
                                    Toast.LENGTH_SHORT
                                ).show() // Выводим сообщение об ошибке
                            }
                        }
                    ) {
                        // Если продукт уже в корзине, показываем иконку галочки, иначе иконку плюса
                        if (productInCart) {
                            Icon(Icons.Filled.Check, contentDescription = "Товар добавлен") // Иконка "Товар добавлен"
                        } else {
                            Icon(Icons.Filled.Add, contentDescription = "Добавить в корзину") // Иконка "Добавить в корзину"
                        }
                    }
                }
            }
        }
    }

    /**
     * Функция для проверки, добавлен ли продукт в корзину
     * @param userId Идентификатор пользователя
     * @param productId Идентификатор продукта
     * @param cartHandler Обработчик корзины
     * @return true если продукт найден в корзине, иначе false
     */
    fun isProductInCart(userId: Int, productId: Int, cartHandler: UserCartTableHandler?): Boolean {
        cartHandler?.let { // Если обработчик корзины не равен null
            val cursor = it.getItemsInCart(userId) // Получаем курсор с элементами корзины
            val productIdColumnIndex = cursor.getColumnIndex(UserCartTableHandler.COLUMN_PRODUCT_ID) // Получаем индекс колонки с id продукта
            if (productIdColumnIndex != -1) { // Если индекс корректный
                while (cursor.moveToNext()) { // Проходим по всем записям
                    if (cursor.getInt(productIdColumnIndex) == productId) { // Если найден продукт с нужным id
                        cursor.close() // Закрываем курсор
                        return true // Возвращаем true
                    }
                }
            }
            cursor.close() // Закрываем курсор после проверки
        }
        return false // Если обработчик null или продукт не найден, возвращаем false
    }
}
