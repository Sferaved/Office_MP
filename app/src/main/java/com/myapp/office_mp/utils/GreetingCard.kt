package com.myapp.office_mp.utils
import android.annotation.SuppressLint
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.myapp.office_mp.R
import com.myapp.office_mp.model.ResultIcon

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GreetingCard(
    navController: NavController,
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
                navController = navController,
            )
        }
    ) { it ->
        val image = painterResource(id = R.drawable.mylogo)
        Column(
            modifier =  Modifier
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
                    modifier = Modifier
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

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GreetingCardAppBar(
    modifier: Modifier,
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
                    )
                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = stringResource(id = R.string.app_code),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
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

