package com.myapp.office_mp


import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.myapp.office_mp.email.EmailData
import com.myapp.office_mp.email.ReadEmailTask
import com.myapp.office_mp.ui.theme.OfficeMPTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OfficeMPTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    OfficeMPApp()
                    val readEmailTask = ReadEmailTask()
                    readEmailTask.execute()

                }
            }
        }
    }
}

/**
 * Composable that displays an app bar and a list of dogs.
 */

@Composable
fun OfficeMPApp() {
    var resultList by remember { mutableStateOf<List<EmailData>>(emptyList()) }

    // Вызываем ReadEmailTask в coroutine при первом запуске
    LaunchedEffect(Unit) {
        // Запускаем в глобальной coroutine
        while (true) {
            GlobalScope.launch(Dispatchers.IO) {
                val readEmailTask = ReadEmailTask()
                val result = readEmailTask.execute().get()

                // Обновляем состояние на главном потоке
                withContext(Dispatchers.Main) {
                    resultList = result
                }
            }
            delay( 2 * 60 * 1000) // Задержка перед следующей проверкой
        }
    }
    Scaffold (
        topBar = {
            OfficeMPTopAppBar()
        }
    ){ it->
        LazyColumn (contentPadding = it) {
            items(resultList.sortedByDescending { it.docNumber }) {
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
                    .animateContentSize (
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                    .background(color = color)
            )  {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(id = R.dimen.padding_small)
                        )
                ) {

                    Text(
                        text ="# ${result.comment}",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small))
                    )
                    Spacer(modifier = Modifier.weight(0.5f))
                    ItemButton(
                        expanded = expanded,
                        onClick = {expanded = !expanded}
                    )
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
            text = "Время: ${result.modificationDate}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text ="Декларация: ${result.docNumber}",
            style = MaterialTheme.typography.bodyLarge
        )


    }
}


/**
 * Composable that displays what the UI of the app looks like in light theme in the design tab.
 */
@Preview
@Composable
fun OfficeMPPreview() {
    OfficeMPTheme(darkTheme = false) {
        OfficeMPApp()
    }
}

@Preview
@Composable
fun OfficeMPDarkThemePreview() {
    OfficeMPTheme(darkTheme = true) {
        OfficeMPApp()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficeMPTopAppBar(modifier: Modifier = Modifier) {
    CenterAlignedTopAppBar(
        title = {
            Row (
                verticalAlignment = Alignment.CenterVertically
            ) {
                ResultIcon()
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.displayLarge
                )
            }

        },
        modifier = modifier
    )
}

@Composable
private fun ItemButton(
    expanded: Boolean,
    onClick: ()->Unit,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier.size(dimensionResource(id = R.dimen.image_size))
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = "",
            tint = MaterialTheme.colorScheme.secondary
        )
    }
}

