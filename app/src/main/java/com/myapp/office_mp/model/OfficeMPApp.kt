package com.myapp.office_mp.model

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.myapp.office_mp.R
import com.myapp.office_mp.email.EmailData
import com.myapp.office_mp.email.ReadEmailTask
import com.myapp.office_mp.utils.GreetingCard
import com.myapp.office_mp.utils.OfficeScreen
import com.myapp.office_mp.utils.StartPage
import com.myapp.office_mp.utils.db.DatabaseHelper
import com.myapp.office_mp.utils.update.AppUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Properties
import javax.mail.Flags
import javax.mail.Folder
import javax.mail.Message
import javax.mail.Session
import javax.mail.Store
import kotlin.system.exitProcess

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LOfficeMPApp(
    context: Context,

    navController: NavHostController = rememberNavController(),
    officeMPAppViewModel: OfficeMPAppViewModel = viewModel()
) {
    val activity = LocalContext.current as Activity
    officeMPAppViewModel.setTimeToPush(context)

    Scaffold (

        bottomBar = {
            BottomAppBar(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    IconButton(onClick = { navController.navigate(OfficeScreen.Main.name) }) {
                        Icon(Icons.Default.Home, contentDescription = "Home")
                    }

                    IconButton(onClick = { navController.navigate(OfficeScreen.AboutAuthor.name) }) {
                        Icon(Icons.Default.Info, contentDescription = "About Author")
                    }


                }
            }
        }
    ) { it->

        NavHost(
            navController = navController,
            startDestination = OfficeScreen.Start.name,
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            composable(route = OfficeScreen.Start.name) {
                // Экран "Start"
                StartPage(navController)
            }
            composable(route = OfficeScreen.Main.name) {
                // Экран "Домой"
                OfficeMPApp(
                    context,
                    activity, // Передайте вашу активити
                    navController,
                    officeMPAppViewModel,
                )
            }
            composable(route = OfficeScreen.AboutAuthor.name) {
                // Экран "Об Авторе"
                GreetingCard(navController)
            }
        }

    }
}


@SuppressLint("CoroutineCreationDuringComposition")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OfficeMPApp(
    context: Context,
    activity: Activity,
    navController: NavController,
    viewModel: OfficeMPAppViewModel,
) {

    if (!isNotificationEnabled(context)) openNotificationSettings(context)
    // Проверяем, включено ли разрешение

    var resultList by remember { mutableStateOf<List<EmailData>>(emptyList()) }
    var isChecking by remember { mutableStateOf(true) } // Флаг для отслеживания статуса проверки

    val (showDialog, setShowDialog) = remember { mutableStateOf(true) }
    var isAccessCodeValid by remember { mutableStateOf(false) }
    var accessCode by remember { mutableStateOf("") }

    // Для примера, пусть эти переменные отвечают за выбор элемента меню

    if (showDialog) {
        AccessCodeDialog(
            onSubmit = { inputAccessCode ->
                if (inputAccessCode == "777" || inputAccessCode == "321"  || inputAccessCode == "456" ) {
                    isAccessCodeValid = true
                    accessCode = inputAccessCode;
                    setShowDialog(false)
                } else {
                    Toast.makeText(context, "Неверный код доступа", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { exitProcess(0) }
        )
    } else if (isAccessCodeValid) {
        // Запускаем проверку почты или что-то еще, что нужно выполнить после успешного ввода кода доступа

        // Создание экземпляра задачи
        val readEmailTask = ReadEmailTask(context, accessCode)

// Запуск задачи асинхронно с помощью сопрограмм
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Выполнение задачи и получение результата
                val result = readEmailTask.execute()

                // Обновление состояния на главном потоке
                resultList = result
                isChecking = false
            } catch (e: Exception) {
                // Обработка исключений, если необходимо
            }
        }


    }

    // Состояние для открытия/закрытия бокового меню
    var isMenuOpen by remember { mutableStateOf(false) }

    // Функция для открытия бокового меню
    fun openMenu() {
        isMenuOpen = true
    }
    // Get current back stack entry

    Scaffold (
        topBar = {
            OfficeMPTopAppBar(
                modifier = Modifier,
                isChecking,
                accessCode,
                onMenuClick = { openMenu() },
                navController,
            )
        },

        ){ it->


        if(resultList.isNotEmpty()) {
            LazyColumn (contentPadding = it) {
                items(resultList.sortedByDescending { it.modificationDate }) {
                    ResultItem(it)
                }
            }
        }

    }
    SettingsMenu(
        isOpen = isMenuOpen,
        onMenuDismiss = { isMenuOpen = false },
        context,
        activity,
        accessCode,
        onSettingsChangedList = { newResultList ->
            resultList = newResultList
        },
        onSettingsChangedProgress = { newIsChecking ->
            isChecking = newIsChecking
        },
        viewModel,
    )
}
@SuppressLint("ScheduleExactAlarm")
@OptIn(DelicateCoroutinesApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SettingsMenu(
    isOpen: Boolean,
    onMenuDismiss: () -> Unit,
    context: Context,
    activity: Activity,
    accessCode: String,
    onSettingsChangedList: (List<EmailData>) -> Unit,
    onSettingsChangedProgress: (Boolean) -> Unit,
    viewModel: OfficeMPAppViewModel,
) {
    if (isOpen) {
        val screenWidth = LocalContext.current.resources.displayMetrics.widthPixels.dp
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onMenuDismiss),

            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier

                    .width(screenWidth * 0.25f) // Размер меню - четверть ширины экрана
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Действия",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    MenuItem(text = "🔄"+" Обновить") {
                        onSettingsChangedProgress(true)

                        // Создание экземпляра задачи
                        val readEmailTask = ReadEmailTask(context, accessCode)

// Запуск задачи асинхронно с помощью сопрограмм
                        CoroutineScope(Dispatchers.Main).launch {
                            try {
                                // Выполнение задачи и получение результата
                                val result = readEmailTask.execute()

                                // Обновление состояния на главном потоке
                                onSettingsChangedList(result)
                                onSettingsChangedProgress(false)
                            } catch (e: Exception) {
                                // Обработка исключений, если необходимо
                            }
                        }

                        onMenuDismiss()
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    MenuItem(text = "🗑️" + " Стереть") {
                        Thread {
                            unreadEmails(accessCode)
                        }.start()
                        onSettingsChangedList(emptyList())
                        onMenuDismiss()
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    var selectedHour by remember { mutableStateOf(0) }
                    var selectedMinute by remember { mutableStateOf(0) }

                    var isDialogOpen by remember { mutableStateOf(false) }

                    if (isDialogOpen) {
                        TimePickerDialog(
                            onTimeSelected = { hour, minute ->
                                // Обработка выбранного времени
                                selectedHour = hour
                                selectedMinute = minute

//                                setAlarm(context, hour, minute, viewModel)
                                setAlarm(context, hour, minute)

                                isDialogOpen = false // Закрыть диалог после выбора времени

                                onMenuDismiss()
                            },
                            onDismiss = {
                                isDialogOpen = false
                                onMenuDismiss()
                            } // Закрыть диалог при отмене
                        )
                    }

                    MenuItem(text = "⏰ Будильник") {


                        // При нажатии на пункт меню открываем диалог
                        isDialogOpen = true
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    var isUpdate by remember { mutableStateOf(false) }

                    if (isUpdate) {
                        updateMyApp(activity, context)
                        onMenuDismiss()
                    }
                    MenuItem(text = "\uD83D\uDEE0\uFE0F  Проверить новую версию") {


                        // При нажатии на пункт меню открываем диалог
                        isUpdate = true
                    }


                    Spacer(modifier = Modifier.height(10.dp))
                    MenuItem(text = "🚪" + " Выход") {
                        // Обработчик нажатия на третью опцию
                        exitProcess(0) // Закрыть приложение
                    }

                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MenuItem(text: String, onItemClick: (String) -> Unit) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.clickable { onItemClick(text) }
    )
}


@Composable
fun AccessCodeDialog(onSubmit: (String) -> Unit, onDismiss: () -> Unit) {
    var accessCode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Введите код доступа") },
        text = {
            TextField(
                value = accessCode,
                onValueChange = { accessCode = it },
                label = { Text("Код доступа") }
            )
        },
        confirmButton = {
            Button(onClick = { onSubmit(accessCode) }) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun TimePickerDialog(
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val currentTime = Calendar.getInstance()
    val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
    val currentMinute = currentTime.get(Calendar.MINUTE)


    var hour by remember { mutableStateOf(currentHour) }
    var minute by remember { mutableStateOf(currentMinute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите время") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = hour.toString(),
                    onValueChange = { hour = it.toIntOrNull() ?: 0 },
                    label = { Text("Часы") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                TextField(
                    value = minute.toString(),
                    onValueChange = { minute = it.toIntOrNull() ?: 0 },
                    label = { Text("Минуты") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onTimeSelected(hour, minute) }) {
                Text("ОК")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}


@Composable
fun ResultItem(
    result: EmailData,
    modifier: Modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small))
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    @Composable
    fun getColor(comment: String): Color {
        return when (comment) {
            "Принято в базу" -> MaterialTheme.colorScheme.secondaryContainer// Пример цвета для определенного значения comment
            "ОФОРМЛЕНО" ->  Color.Cyan  // Другой пример цвета
            else -> MaterialTheme.colorScheme.primaryContainer // Цвет по умолчанию
        }
    }

    val color by animateColorAsState(
        targetValue = if (expanded) MaterialTheme.colorScheme.tertiaryContainer
        else getColor(result.comment), // Используем функцию для определения цвета
        label = "",
    )
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                .background(color = color)
        )  {
            Row(
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .fillMaxWidth()
                    .padding(
                        dimensionResource(id = R.dimen.padding_small)

                    )
            ) {
                Text(
                    text ="# ${result.comment} ${result.docInNum}",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .padding(top = dimensionResource(R.dimen.padding_small))
                )
                Spacer(modifier = Modifier.width(5.dp))

            }
            if (expanded){
                ResultInfo(
                    result,
                    modifier = Modifier.padding(
                        start = dimensionResource(id = R.dimen.padding_medium),
                        top = dimensionResource(id = R.dimen.padding_small),
                        end = dimensionResource(id = R.dimen.padding_medium),
                        bottom = dimensionResource(id = R.dimen.padding_medium)
                    )
                )
            }


        }
    }


}

@Composable
fun ResultIcon() {
    Image(
        modifier = Modifier
            .size(dimensionResource(id = R.dimen.image_size))
            .padding(dimensionResource(id = R.dimen.padding_small))
            .clickable {
                // Действие при нажатии на текстовый элемент (закрытие приложения)
                exitProcess(0) // Закрыть приложение с кодом 0
            },
        painter = painterResource(id = R.drawable.bullet_2157465),
        contentDescription = null
    )
}

@Composable
fun ResultInfo(result: EmailData, modifier: Modifier) {
    Column (
        modifier = modifier
    ) {


        Text(
            text = "${result.modificationDate}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text ="${result.docNumber}",
            style = MaterialTheme.typography.bodyLarge
        )
//        Text(
//            text ="${result.subject}",
//            style = MaterialTheme.typography.bodyLarge
//        )
        val telRegex = Regex("""\b(?:\+?3?8)?\s?\(?(?:0\d{2})\)?[-.\s]?\d{3}[-.\s]?\d{2}[-.\s]?\d{2}\b""")
        val annotatedString = buildAnnotatedString {
            append(result.orgName)
            val telMatches = telRegex.findAll(result.orgName)
            telMatches.forEach { matchResult ->
                val startIndex = matchResult.range.first
                val endIndex = matchResult.range.last + 1
                addStyle(
                    SpanStyle(textDecoration = TextDecoration.Underline),
                    startIndex,
                    endIndex
                )
                addStringAnnotation(
                    "PhoneNumber",
                    matchResult.value,
                    startIndex,
                    endIndex
                )
            }
        }

        val context = LocalContext.current
        val isDarkTheme = isSystemInDarkTheme()

        val textColor = if (isDarkTheme) {
            Color.White // Цвет текста для темной темы
        } else {
            Color.Black // Цвет текста для светлой темы
        }

        val style = MaterialTheme.typography.labelLarge.copy(color = textColor)

        val hasPhoneNumber = annotatedString.getStringAnnotations("PhoneNumber", 0, annotatedString.length).isNotEmpty()

        val clickableText = if (hasPhoneNumber) {
            ClickableText(
                text = buildAnnotatedString {
                    append("📞 ") // Добавляем эмодзи перед текстом
                    append(annotatedString) // Добавляем аннотированный текст
                },
                onClick = { offset ->
                    annotatedString.getStringAnnotations("PhoneNumber", offset, offset)
                        .firstOrNull()?.let { phoneNumber ->
                            val uri = Uri.parse("tel:${phoneNumber.item}")
                            val intent = Intent(Intent.ACTION_DIAL, uri)
                            context.startActivity(intent)
                        }
                },
                style = style
            )
        } else {
            Text(
                text = annotatedString,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        clickableText

        Text(
            text ="${result.userName}",
            style = MaterialTheme.typography.bodyLarge
        )

    }
}





/**
 * Composable that displays what the UI of the app looks like in light theme in the design tab.
 */
//@Preview
//@Composable
//fun OfficeMPPreview() {
//    OfficeMPTheme(darkTheme = false) {
//        OfficeMPApp()
//    }
//}
//
//@Preview
//@Composable
//fun OfficeMPDarkThemePreview() {
//    OfficeMPTheme(darkTheme = true) {
//        OfficeMPApp()
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficeMPTopAppBar(
    modifier: Modifier = Modifier,
    isChecking: Boolean,
    accessCode: String,
    onMenuClick: () -> Unit = { /* ваш код для открытия бокового меню */ },
    navController: NavController,
) {
    CenterAlignedTopAppBar(
        title = {
            Column {
                Row (
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ResultIcon()
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.displayMedium,
//                        modifier = Modifier.clickable {
//                            // Действие при нажатии на текстовый элемент (закрытие приложения)
//                            Thread {
//                                unreadEmails(accessCode)
//                            }.start()
//                        }
                    )

                }
                if (isChecking) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .padding(4.dp),
                        color = Color.Green,
                        trackColor = Color.Red,
                        strokeCap = StrokeCap.Butt
                    )
                }
            }

        },
        actions = {
            IconButton(
                onClick = { onMenuClick() } // Вызов обратного вызова при нажатии на меню
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu"
                )
            }
        },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack(OfficeScreen.Start.name, inclusive = false) }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_button)
                )
            }
        }
    )
}


fun unreadEmails(
    accessCode:String
) {
    try {
        // Настройте свойства для подключения
        val props = Properties()
        props.setProperty("mail.store.protocol", "imaps")

        // Создайте сессию и подключитесь к почтовому ящику
        val session: Session = Session.getDefaultInstance(props, null)
        val store: Store = session.store
        when (accessCode) {
            "777" -> store.connect("imap.ukr.net", "sferved.t@ukr.net", "ImVXWwHuw83Q5m16") //Таня
            "321" -> store.connect("imap.ukr.net", "sferved.m@ukr.net", "JMhTvEgCF9GsIyAQ") //Маня
            "456" -> store.connect("imap.ukr.net", "sferved.n@ukr.net", "zyiYFd7LigTv2vyB") //Наташа
        }

        // Откройте папку "inbox" для чтения и записи
        val inbox: Folder = store.getFolder("Inbox")
        inbox.open(Folder.READ_WRITE)

        // Получите все сообщения в папке "inbox"
        val messages: Array<Message> = inbox.messages

        // Отметьте каждое сообщение как прочитанное
        for (message in messages) {
//            message.setFlag(Flags.Flag.SEEN, true)
            message.setFlag(Flags.Flag.DELETED, true)
        }
        inbox.expunge()
        // Закройте папку и соединение
        inbox.close(false)
        store.close()

    } catch (e: Exception) {
        e.printStackTrace()
        Log.d("EmailReader+", "Error: $e")
    }
}

fun isNotificationEnabled(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.areNotificationsEnabled()
    } else {
        // Для Android до версии O нет прямого способа проверки разрешений на уведомления
        // Однако мы можем проверить, включено ли разрешение на уведомления через системные настройки
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}

fun openNotificationSettings(context: Context) {
    val intent = Intent()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    } else {
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", context.packageName, null)
    }
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    context.startActivity(intent)
}

//// Функция для установки будильника
fun setAlarm(
    context: Context,
    hour: Int,
    minute: Int,
//    viewModel: OfficeMPAppViewModel,
) {

    val dbHelper = DatabaseHelper(context)
    dbHelper.updateNotificationTime(hour, minute, 0)

    val newTime:Triple<Int,Int,Int> = Triple (hour, minute, 0)
//    viewModel.updateState(newTime)
//    viewModel.getCurrentState()
}


fun updateMyApp (
    activity: Activity,
    context: Context
) {
    // Создание экземпляра AppUpdater
    val appUpdater = AppUpdater(activity, context)
    Log.d("TAG", "updateMyApp: ")
// Установка слушателя для обновления
    appUpdater.setOnUpdateListener(object : AppUpdater.OnUpdateListener {
        override fun onUpdateCompleted() {
            // Действия при завершении обновления
            appUpdater.unregisterListener()
        }
    })

// Проверка наличия обновлений
    appUpdater.checkForUpdate()

// Регистрация слушателя для обновления состояния установки
    appUpdater.registerListener()

    // После завершения работы необходимо отписаться от слушателя


}
