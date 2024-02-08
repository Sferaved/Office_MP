package com.myapp.office_mp


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myapp.office_mp.email.EmailData
import com.myapp.office_mp.email.ReadEmailTask
import com.myapp.office_mp.ui.theme.OfficeMPTheme
import com.myapp.office_mp.utils.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Flags
import javax.mail.Folder
import javax.mail.Message
import javax.mail.Session
import javax.mail.Store
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    private lateinit var dbHelper: DatabaseHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OfficeMPTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    OfficeMPApp(applicationContext)
                }
            }
        }
    }
}

/**
 * Composable that displays an app bar and a list of dogs.
 */

@Composable
fun OfficeMPApp(context: Context) {
    var resultList by remember { mutableStateOf<List<EmailData>>(emptyList()) }
    var isChecking by remember { mutableStateOf(true) } // Флаг для отслеживания статуса проверки
// В вашем коде OfficeMPApp
    val (showDialog, setShowDialog) = remember { mutableStateOf(true) }
    var isAccessCodeValid by remember { mutableStateOf(false) }
    var accessCode by remember { mutableStateOf("") }

    if (showDialog) {
        AccessCodeDialog(
            onSubmit = { inputAccessCode ->
                if (inputAccessCode == "123" || inputAccessCode == "321" ) {
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
        LaunchedEffect(Unit) {
            // Запускаем в глобальной coroutine
            while (true) {
                GlobalScope.launch(Dispatchers.IO) {
                    val readEmailTask = ReadEmailTask(context, accessCode)
                    val result = readEmailTask.execute().get()

                    // Обновляем состояние на главном потоке
                    withContext(Dispatchers.Main) {
                        resultList = result
                        isChecking = false
                    }
                }
                delay( 2 * 60 * 1000) // Задержка перед следующей проверкой
            }
        }
    }



    // Вызываем ReadEmailTask в coroutine при первом запуске

    Scaffold (
        topBar = {
            OfficeMPTopAppBar(
                modifier = Modifier,
                isChecking,
                accessCode
            )
        }
    ){ it->
        if(resultList.isNotEmpty()) {
            LazyColumn (contentPadding = it) {
                items(resultList.sortedByDescending { it.modificationDate }) {
                    ResultItem(it)
                }
            }
        } else if (!isChecking){
            val message = "Нет новых сообщений"
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        } else {
            val message = "Поиск новых сообщений ..."
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

    }
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
fun ResultItem(
    result: EmailData,
    modifier: Modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small))
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    val color by animateColorAsState(
        targetValue = if (expanded) MaterialTheme.colorScheme.tertiaryContainer
        else MaterialTheme.colorScheme.primaryContainer,
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
                    resultInfo(
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
fun resultInfo(result: EmailData, modifier: Modifier) {
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
        Text(
            text ="${result.orgName}",
            style = MaterialTheme.typography.bodyLarge
        )
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
    accessCode: String
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
                        modifier = Modifier.clickable {
                            // Действие при нажатии на текстовый элемент (закрытие приложения)
                            Thread {
                                unreadEmails(accessCode)
                            }.start()
                        }
                    )
                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = stringResource(id = R.string.app_code),
                        style = MaterialTheme.typography.labelSmall
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
        modifier = modifier
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
            "123" -> store.connect("imap.ukr.net", "sferved.t@ukr.net", "f7K9YvpMeeZTyyKa") //Таня
            "321" -> store.connect("imap.ukr.net", "sferved.m@ukr.net", "JMhTvEgCF9GsIyAQ") //Маня
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
