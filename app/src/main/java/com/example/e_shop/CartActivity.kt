package com.example.e_shop
// Импортируем необходимые классы для работы с намерениями, URI и системой
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.example.e_shop.DB.DataBaseHandler
import com.example.e_shop.DB.OrderDetailsTableHandler
import com.example.e_shop.DB.OrdersTableHandler
import com.example.e_shop.DB.ProductsTableHandler
import com.example.e_shop.DB.UserCartTableHandler
import com.example.e_shop.DataClasses.Product
import com.example.e_shop.DataClasses.User
import com.example.e_shop.ui.theme.E_ShopTheme

// Класс активности корзины
class CartActivity : ComponentActivity() {

    private lateinit var dbHandler: DataBaseHandler // Обработчик базы данных для работы с данными
    private lateinit var userCartTableHandler: UserCartTableHandler // Обработчик таблицы корзины
    private var currentUser: User? = null // Текущий пользователь (если авторизован)

    /**
     * Функция-компонент для вывода заголовка экрана корзины
     */
    @Composable
    fun CartScreenHeader() {
        Row( // Горизонтальное расположение элементов
            modifier = Modifier
                .fillMaxWidth() // Заполнение всей ширины экрана
                .padding(16.dp), // Отступ 16 dp со всех сторон
            horizontalArrangement = Arrangement.SpaceBetween // Элементы распределяются по краям
        ) {
            Icon( // Иконка "Домой"
                imageVector = Icons.Filled.Home, // Используем иконку Home
                contentDescription = "Главная страница", // Описание для доступности
                modifier = Modifier.clickable { // Модификатор, делающий элемент кликабельным
                    onBackPressed() // При клике возвращаемся назад (на главную)
                }
            )
            Text( // Вывод текста "Корзина"
                text = "Корзина", // Текст заголовка
                fontWeight = FontWeight.Bold, // Жирное начертание текста
                fontSize = 24.sp // Размер шрифта 24 sp
            )
            Spacer(modifier = Modifier.width(24.dp)) // Пробел шириной 24 dp для балансировки
        }
    }

    /**
     * Функция-компонент для отображения основного экрана корзины
     */
    @Composable
    fun CartScreen() {
        // Получаем курсор с данными о продуктах, добавленных в корзину для текущего пользователя
        val userCartItemsCursor = userCartTableHandler.getCartProductsForUser(currentUser?.id ?: -1)
        val products = mutableListOf<Product>() // Список для хранения продуктов из корзины
        val productsInCart = remember { mutableStateMapOf<Int, Int>() } // Изменяемая карта для хранения количества каждого продукта в корзине
        val totalAmountState = remember { mutableStateOf(0.0) } // Изменяемое состояние для общей суммы заказа

        // Если курсор содержит данные, переходим к их чтению
        if (userCartItemsCursor.moveToFirst()) {
            do {
                // Получаем индексы столбцов для необходимых данных
                val productIdColIndex = userCartItemsCursor.getColumnIndex(ProductsTableHandler.COLUMN_ID) // Индекс столбца с id продукта
                val nameColIndex = userCartItemsCursor.getColumnIndex(ProductsTableHandler.COLUMN_NAME) // Индекс столбца с названием продукта
                val descriptionColIndex = userCartItemsCursor.getColumnIndex(ProductsTableHandler.COLUMN_DESCRIPTION) // Индекс столбца с описанием продукта
                val priceColIndex = userCartItemsCursor.getColumnIndex(ProductsTableHandler.COLUMN_PRICE) // Индекс столбца с ценой
                val imageUrlColIndex = userCartItemsCursor.getColumnIndex(ProductsTableHandler.COLUMN_IMAGE_URL) // Индекс столбца с URL изображения
                val quantityColIndex = userCartItemsCursor.getColumnIndex(ProductsTableHandler.COLUMN_QUANTITY) // Индекс столбца с количеством продукта на складе
                val cartQuantityColIndex = userCartItemsCursor.getColumnIndex("cart_quantity") // Индекс столбца с количеством товара в корзине

                // Проверяем, что индексы получены корректно
                if (productIdColIndex != -1 && nameColIndex != -1 && descriptionColIndex != -1 && priceColIndex != -1 && imageUrlColIndex != -1) {
                    // Считываем данные продукта из курсора
                    val productId = userCartItemsCursor.getInt(productIdColIndex) // Считываем id продукта
                    val name = userCartItemsCursor.getString(nameColIndex) // Считываем название продукта
                    val description = userCartItemsCursor.getString(descriptionColIndex) // Считываем описание продукта
                    val price = userCartItemsCursor.getDouble(priceColIndex) // Считываем цену продукта
                    val imageUrl = userCartItemsCursor.getString(imageUrlColIndex) // Считываем URL изображения
                    // Считываем количество товара на складе, если индекс корректен, иначе устанавливаем 0
                    val quantityStock = if (quantityColIndex != -1) userCartItemsCursor.getInt(quantityColIndex) else 0
                    // Считываем количество товара в корзине, если индекс корректен, иначе устанавливаем 0
                    val cartQuantity = if (cartQuantityColIndex != -1) userCartItemsCursor.getInt(cartQuantityColIndex) else 0
                    // Создаем объект Product на основе полученных данных
                    val product = Product(productId, name, description, price, imageUrl, quantityStock)
                    products.add(product) // Добавляем продукт в список
                    productsInCart[productId] = cartQuantity // Сохраняем количество товара в корзине по его id
                }

            } while (userCartItemsCursor.moveToNext()) // Повторяем для всех записей курсора
        }
        userCartItemsCursor.close() // Закрываем курсор после чтения данных

        Column( // Вертикальное расположение элементов на экране
            modifier = Modifier.fillMaxSize(), // Занимает всё доступное пространство
            horizontalAlignment = Alignment.CenterHorizontally // Центрирование по горизонтали
        ) {
            Row( // Горизонтальное расположение для шапки экрана корзины
                modifier = Modifier
                    .fillMaxWidth() // Ряд занимает всю ширину экрана
                    .padding(16.dp), // Отступ 16 dp со всех сторон
                horizontalArrangement = Arrangement.SpaceBetween // Элементы распределяются по краям
            ) {
                Icon( // Иконка для перехода на главную
                    imageVector = Icons.Filled.Home, // Используем иконку "Домой"
                    contentDescription = "Главная страница", // Описание для доступности
                    modifier = Modifier.clickable { // Делает иконку кликабельной
                        val intent = Intent(this@CartActivity, MainActivity::class.java) // Создаем намерение для MainActivity
                        intent.putExtra("CURRENT_USER", currentUser) // Передаем текущего пользователя через интент
                        startActivity(intent) // Запускаем MainActivity
                        finish() // Завершаем текущую активность
                    }
                )
                Text( // Текст заголовка "Корзина"
                    text = "Корзина", // Выводим текст
                    fontWeight = FontWeight.Bold, // Жирное начертание
                    fontSize = 24.sp // Размер шрифта 24 sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp)) // Отступ сверху 24 dp

            val productsState = remember { mutableStateOf(products) } // Состояние списка продуктов в корзине

            // Вычисляем общую сумму заказа
            val totalAmount = products.sumByDouble { product -> product.price * (productsInCart[product.id] ?: 0) }
            totalAmountState.value = totalAmount // Обновляем состояние общей суммы

            LazyColumn(modifier = Modifier.weight(1f)) { // Список, занимающий оставшееся пространство
                itemsIndexed(productsState.value) { _, product: Product ->
                    // Вывод каждого продукта в корзине с использованием компонента ProductItem
                    ProductItem(
                        product = product, // Передаем объект продукта
                        userId = currentUser?.id, // Передаем идентификатор текущего пользователя
                        cartHandler = userCartTableHandler, // Передаем обработчик корзины
                        quantityInCart = productsInCart[product.id] ?: 0, // Количество данного продукта в корзине
                        productsInCart = productsInCart, // Карта количеств продуктов
                        onProductRemoved = { // Функция, вызываемая при удалении продукта
                            productsState.value = fetchCartProducts() // Обновляем список продуктов после удаления
                        },
                        productsState = productsState, // Передаем состояние списка продуктов
                        totalAmountState = totalAmountState // Передаем состояние общей суммы заказа
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Отступ снизу 16 dp
            Text( // Вывод текста общей суммы
                text = "Общая сумма: $${totalAmountState.value}", // Форматированный текст суммы
                style = MaterialTheme.typography.bodyLarge // Используем стиль текста из MaterialTheme
            )
            Button(onClick = onClick@{ // Кнопка для оформления заказа
                if (productsState.value.isEmpty()) { // Проверяем, пуста ли корзина
                    Toast.makeText(applicationContext, "Корзина пуста", Toast.LENGTH_SHORT).show() // Сообщаем, что корзина пуста
                    return@onClick // Прерываем выполнение обработчика
                }
                // Создаем элемент ввода для адреса доставки
                val addressInputDialog = EditText(applicationContext) // Поле для ввода адреса доставки
                AlertDialog.Builder(this@CartActivity) // Создаем билдер диалогового окна
                    .setTitle("Введите адрес доставки") // Устанавливаем заголовок диалога
                    .setView(addressInputDialog) // Устанавливаем пользовательский вид для ввода
                    .setPositiveButton("OK") { _, _ -> // Устанавливаем кнопку подтверждения
                        val deliveryAddress = addressInputDialog.text.toString() // Получаем введенный адрес доставки
                        if (deliveryAddress.isNotBlank()) { // Если адрес не пустой
                            val ordersTableHandler = OrdersTableHandler(dbHandler) // Создаем обработчик заказов
                            // Добавляем новый заказ и получаем его идентификатор
                            val orderId = ordersTableHandler.addOrder(currentUser?.id ?: -1, totalAmountState.value, deliveryAddress).toInt()
                            if (orderId == -1) { // Если произошла ошибка при добавлении заказа
                                Toast.makeText(applicationContext, "Ошибка при добавлении заказа", Toast.LENGTH_SHORT).show() // Выводим сообщение об ошибке
                                return@setPositiveButton // Прерываем выполнение
                            }
                            val orderDetailsTableHandler = OrderDetailsTableHandler(dbHandler) // Создаем обработчик деталей заказа
                            // Для каждого продукта в корзине добавляем его детали в заказ
                            for (product in productsState.value) {
                                val quantity = productsInCart[product.id] ?: 0 // Получаем количество продукта из корзины
                                orderDetailsTableHandler.addOrderDetail(orderId, product.id, quantity, product.price) // Добавляем деталь заказа
                            }
                            // Очищаем корзину пользователя после оформления заказа
                            userCartTableHandler.clearUserCart(currentUser?.id ?: -1)
                            Toast.makeText(applicationContext, "Заказ оформлен", Toast.LENGTH_SHORT).show() // Сообщаем об успешном оформлении
                            productsState.value = fetchCartProducts() // Обновляем список продуктов в корзине
                            totalAmountState.value = 0.0 // Обнуляем общую сумму заказа
                        } else { // Если адрес пустой
                            Toast.makeText(applicationContext, "Адрес не может быть пустым", Toast.LENGTH_SHORT).show() // Выводим предупреждение
                        }
                    }
                    .setNegativeButton("Отмена") { dialog, _ -> // Устанавливаем кнопку отмены
                        dialog.dismiss() // Закрываем диалоговое окно
                    }
                    .show() // Показываем диалоговое окно
            }) {
                Text("Заказать") // Текст кнопки оформления заказа
            }
        }
    }

    /**
     * Компонент для отображения одного элемента продукта в корзине
     *
     * @param product Объект продукта для отображения
     * @param userId Идентификатор текущего пользователя
     * @param cartHandler Обработчик операций с корзиной
     * @param quantityInCart Количество данного продукта в корзине
     * @param productsInCart Изменяемая карта количеств продуктов
     * @param onProductRemoved Функция, вызываемая при удалении продукта из корзины
     * @param productsState Состояние списка продуктов в корзине
     * @param totalAmountState Состояние общей суммы заказа
     * @param modifier Модификатор для компонента
     */
    @Composable
    fun ProductItem(
        product: Product, // Объект продукта
        userId: Int?, // Идентификатор пользователя (если он авторизован)
        cartHandler: UserCartTableHandler?, // Обработчик корзины
        quantityInCart: Int, // Количество продукта в корзине
        productsInCart: MutableMap<Int, Int>, // Карта количества продуктов
        onProductRemoved: () -> Unit, // Callback при удалении продукта из корзины
        productsState: MutableState<MutableList<Product>>, // Состояние списка продуктов в корзине
        totalAmountState: MutableState<Double>, // Состояние общей суммы заказа
        modifier: Modifier = Modifier // Модификатор, по умолчанию пустой
    ) {
        var quantityState = remember { mutableStateOf(quantityInCart) }
        Card(
            modifier = modifier
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = product.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f).padding(8.dp))
                    IconButton(onClick = {
                        cartHandler?.removeItemFromCart(userId ?: -1, product.id)
                        onProductRemoved()
                        val newTotal = productsState.value.sumByDouble { prod -> prod.price * (productsInCart[prod.id] ?: 0) }
                        totalAmountState.value = newTotal
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить из корзины")
                    }
                }

                product.image_url?.let { imageUrl -> // Если URL изображения не пустой
                    val imageUri = Uri.parse("file:///android_asset/$imageUrl") // Преобразуем строку в URI
                    Image(
                        painter = rememberImagePainter(imageUri), // Загружаем изображение через Coil
                        contentDescription = "Product Image", // Описание изображения
                        modifier = Modifier
                            .fillMaxWidth() // Изображение занимает всю ширину карточки
                            .aspectRatio(1f) // Соотношение сторон 1:1
                    )
                }

                Spacer(modifier = Modifier.height(8.dp)) // Вертикальный отступ между элементами

                Row( // Ряд для вывода цены и изменения количества продукта
                    horizontalArrangement = Arrangement.SpaceBetween, // Элементы располагаются по краям
                    modifier = Modifier.fillMaxWidth() // Ряд занимает всю ширину
                ) {
                    Text(
                        text = "$${product.price}", // Выводим цену продукта
                        style = MaterialTheme.typography.bodyLarge // Применяем стиль текста
                    )

                    Row( // Ряд для управления количеством продукта
                        verticalAlignment = Alignment.CenterVertically // Центрирование элементов по вертикали
                    ) {
                        Text("${quantityState.value}") // Вывод текущего количества продукта
                        IconButton(
                            onClick = {
                                if (quantityState.value > 1) { // Если количество больше единицы
                                    quantityState.value -= 1 // Уменьшаем количество на 1
                                    cartHandler?.updateItemQuantityInCart(userId ?: -1, product.id, quantityState.value) // Обновляем количество в базе
                                    productsInCart[product.id] = quantityState.value // Обновляем карту количеств
                                    onProductRemoved() // Обновляем корзину после изменения
                                }
                            }
                        ) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Прибавить") // Иконка уменьшения количества
                        }
                        IconButton(
                            onClick = {
                                if (quantityState.value < product.quantity) { // Если количество меньше доступного на складе
                                    quantityState.value += 1 // Увеличиваем количество на 1
                                    cartHandler?.updateItemQuantityInCart(userId ?: -1, product.id, quantityState.value) // Обновляем количество в базе
                                    productsInCart[product.id] = quantityState.value // Обновляем карту количеств
                                    onProductRemoved() // Обновляем корзину после изменения
                                } else { // Если превышен лимит количества
                                    Toast.makeText(applicationContext, "Недостаточно товаров на складе", Toast.LENGTH_SHORT).show() // Сообщаем об ошибке
                                }
                            }
                        ) {
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Убавить") // Иконка увеличения количества
                        }
                    }
                }
                Text(
                    text = "Сумма: $${product.price * quantityState.value}", // Вычисляем и выводим сумму за продукт
                    style = MaterialTheme.typography.bodyLarge // Применяем стиль текста
                )
            }
        }
    }

    /**
     * Функция для проверки наличия продукта в корзине
     * @param userId Идентификатор пользователя
     * @param productId Идентификатор продукта
     * @param cartHandler Обработчик корзины
     * @return true, если продукт найден в корзине, иначе false
     */
    fun isProductInCart(userId: Int, productId: Int, cartHandler: UserCartTableHandler?): Boolean {
        cartHandler?.let { // Если обработчик корзины не null
            val cursor = it.getItemsInCart(userId) // Получаем курсор с элементами корзины
            val productIdColumnIndex = cursor.getColumnIndex(UserCartTableHandler.COLUMN_PRODUCT_ID) // Получаем индекс столбца product_id
            if (productIdColumnIndex != -1) { // Если индекс корректный
                while (cursor.moveToNext()) { // Проходим по всем записям в курсоре
                    if (cursor.getInt(productIdColumnIndex) == productId) { // Если найден продукт с нужным id
                        cursor.close() // Закрываем курсор
                        return true // Возвращаем true
                    }
                }
            }
            cursor.close() // Закрываем курсор, если продукт не найден
        }
        return false // Возвращаем false, если обработчик null или продукт не найден
    }

    // Переопределяем метод onCreate для активности CartActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Вызываем базовую реализацию onCreate
        this.currentUser = intent.getParcelableExtra("CURRENT_USER") // Получаем текущего пользователя из интента
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Проверяем версию ОС
            this.dbHandler = DataBaseHandler(this) // Инициализируем обработчик базы данных
        }
        this.userCartTableHandler = UserCartTableHandler(dbHandler) // Инициализируем обработчик таблицы корзины

        setContent { // Устанавливаем контент для Compose
            E_ShopTheme { // Применяем тему приложения
                this.CartScreen() // Вызываем функцию CartScreen для отображения экрана корзины
            }
        }
    }

    /**
     * Функция для извлечения списка продуктов из корзины
     * @return Изменяемый список продуктов, присутствующих в корзине
     */
    private fun fetchCartProducts(): MutableList<Product> {
        val userCartItemsCursor = userCartTableHandler.getCartProductsForUser(currentUser?.id ?: -1) // Получаем курсор с данными корзины
        val products = mutableListOf<Product>() // Создаем пустой список для продуктов

        if (userCartItemsCursor.moveToFirst()) { // Если курсор не пустой
            do {
                // Получаем индексы столбцов для продукта
                val productIdColIndex = userCartItemsCursor.getColumnIndex(ProductsTableHandler.COLUMN_ID) // Индекс id продукта
                val nameColIndex = userCartItemsCursor.getColumnIndex(ProductsTableHandler.COLUMN_NAME) // Индекс названия продукта
                val descriptionColIndex = userCartItemsCursor.getColumnIndex(ProductsTableHandler.COLUMN_DESCRIPTION) // Индекс описания продукта
                val priceColIndex = userCartItemsCursor.getColumnIndex(ProductsTableHandler.COLUMN_PRICE) // Индекс цены продукта
                val imageUrlColIndex = userCartItemsCursor.getColumnIndex(ProductsTableHandler.COLUMN_IMAGE_URL) // Индекс URL изображения
                val quantityColIndex = userCartItemsCursor.getColumnIndex(ProductsTableHandler.COLUMN_QUANTITY) // Индекс количества на складе

                if (productIdColIndex != -1 && nameColIndex != -1 && descriptionColIndex != -1 && priceColIndex != -1 && imageUrlColIndex != -1) { // Если все индексы корректны
                    val productId = userCartItemsCursor.getInt(productIdColIndex) // Считываем id продукта
                    val name = userCartItemsCursor.getString(nameColIndex) // Считываем название продукта
                    val description = userCartItemsCursor.getString(descriptionColIndex) // Считываем описание продукта
                    val price = userCartItemsCursor.getDouble(priceColIndex) // Считываем цену продукта
                    val imageUrl = userCartItemsCursor.getString(imageUrlColIndex) // Считываем URL изображения
                    // Определяем количество продукта на складе или 0, если индекс некорректен
                    val quantityStock = if (quantityColIndex != -1) userCartItemsCursor.getInt(quantityColIndex) else 0
                    // Создаем объект Product и добавляем его в список
                    val product = Product(productId, name, description, price, imageUrl, quantityStock)
                    products.add(product)
                }
            } while (userCartItemsCursor.moveToNext()) // Повторяем для всех записей в курсоре
        }
        userCartItemsCursor.close() // Закрываем курсор
        return products // Возвращаем список продуктов из корзины
    }
}
