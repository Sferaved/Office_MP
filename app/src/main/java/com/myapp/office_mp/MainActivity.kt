package com.myapp.office_mp


import android.content.Context
import android.os.Bundle
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
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OfficeMPTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    OfficeMPApp(applicationContext)
//                    val readEmailTask = ReadEmailTask(applicationContext)
//                    readEmailTask.execute()

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
    // Вызываем ReadEmailTask в coroutine при первом запуске
    LaunchedEffect(Unit) {
        // Запускаем в глобальной coroutine
        while (true) {
            GlobalScope.launch(Dispatchers.IO) {
                val readEmailTask = ReadEmailTask(context)
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
    Scaffold (
        topBar = {
            OfficeMPTopAppBar(
                modifier = Modifier,
                isChecking)
        }
    ){ it->
        LazyColumn (contentPadding = it) {
            items(resultList.sortedByDescending { it.modificationDate }) {
                ResultItem(it)
            }
        }
    }
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
            .padding(dimensionResource(id = R.dimen.padding_small)),
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
    isChecking: Boolean) {
    CenterAlignedTopAppBar(
        title = {
            Column {
                Row (
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ResultIcon()
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.clickable {
                            // Действие при нажатии на текстовый элемент (закрытие приложения)
                            exitProcess(0) // Закрыть приложение с кодом 0
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))

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
