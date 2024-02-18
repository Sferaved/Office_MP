package com.myapp.office_mp.utils
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myapp.office_mp.R
import com.myapp.office_mp.email.EmailData
import com.myapp.office_mp.email.ReadEmailTask
import com.myapp.office_mp.model.MenuItem
import com.myapp.office_mp.model.ResultIcon
import com.myapp.office_mp.model.TimePickerDialog
import com.myapp.office_mp.model.setAlarm
import com.myapp.office_mp.model.unreadEmails
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GreetingCard(
    modifier: Modifier = Modifier
) {
    var isMenuOpen by remember { mutableStateOf(false) }

    // Функция для открытия бокового меню
    fun openMenu() {
        isMenuOpen = true
    }
    Scaffold (
        topBar = {
            GreetingCardAppBar(
                modifier = Modifier,
                onMenuClick = { openMenu() }
            )
        }
    ) { it ->
        val image = painterResource(id = R.drawable.mylogo)
        Column(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            Row(
                modifier = Modifier
                    .padding(
                        top = 100.dp,
                        bottom = 8.dp
                    )
                    .weight(1f)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = modifier
                        .fillMaxWidth()
                ) {
                    Image(
                        painter = image,
                        contentDescription = null,
                        modifier = Modifier
                            .size(200.dp) // Задайте размер, который вам нужен
                    )
                    Text(
                        text = stringResource(R.string.android_developer),
                        textAlign = TextAlign.Center,
                        fontSize = 30.sp,
                        fontFamily = FontFamily.Serif,
                        modifier = Modifier
                            .padding(
                                top = 8.dp,
                                bottom = 8.dp
                            )
                            .align(alignment = Alignment.CenterHorizontally)
                    )
                }
            }
            val context = LocalContext.current
            val email = context.getString(R.string.my_email)
            val subject = context.getString(R.string.subject)
            val message = context.getString(R.string.message)

            Row(
                modifier = Modifier
                    .padding(
                        start = 110.dp,
                    )
                    .clickable(onClick = {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = "plain/text"
                        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
                        intent.putExtra(Intent.EXTRA_TEXT, message)
                        intent.flags = FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(Intent.createChooser(intent, "Send Email"))
                    })
            )
            {
                val image = painterResource(id = R.drawable.mail_black_24dp)
                Image(
                    painter = image,
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp) // Задайте размер, который вам нужен

                )
                Text(
                    text = stringResource(R.string.my_email),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(
                        start = 10.dp
                    )

                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            Row(
                modifier = Modifier
                    .align(alignment = Alignment.CenterHorizontally)
                    .padding(
                        bottom = 30.dp
                    )
            )
            {

                Text(
                    text = stringResource(R.string.year),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(
                        start = 10.dp
                    )

                )
            }

        }
    }
//    SettingsMenu(
//        isOpen = isMenuOpen,
//        onMenuDismiss = { isMenuOpen = false },
//        context,
//        accessCode,
//        onSettingsChangedList = { newResultList ->
//            resultList = newResultList
//        },
//        onSettingsChangedProgress = { newIsChecking ->
//            isChecking = newIsChecking
//        }
//    )
}
@SuppressLint("ScheduleExactAlarm")
@OptIn(DelicateCoroutinesApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SettingsMenu(
    isOpen: Boolean,
    onMenuDismiss: () -> Unit,
    context: Context,
    accessCode: String,
    onSettingsChangedList: (List<EmailData>) -> Unit,
    onSettingsChangedProgress: (Boolean) -> Unit
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
                        GlobalScope.launch(Dispatchers.IO) {
                            val readEmailTask = ReadEmailTask(context, accessCode)
                            val result = readEmailTask.execute().get()

                            // Обновляем состояние на главном потоке с помощью обратного вызова
                            withContext(Dispatchers.Main) {
                                onSettingsChangedList(result)
                                onSettingsChangedProgress(false)
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
                    MenuItem(text = "🚪" + " Выход") {
                        // Обработчик нажатия на третью опцию
                        exitProcess(0) // Закрыть приложение
                    }

                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GreetingCardAppBar(modifier: Modifier, onMenuClick: () -> Unit) {
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
                    )
                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = stringResource(id = R.string.app_code),
                        style = MaterialTheme.typography.labelSmall
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
    )
}

