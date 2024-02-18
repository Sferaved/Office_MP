package com.myapp.office_mp.utils
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.myapp.office_mp.R

@Composable
fun StartPage(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val image = painterResource(id = R.drawable.bullet_2157465)
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
                    .clickable(onClick =  { navController.navigate(OfficeScreen.Main.name) })
            ) {
                Image(
                    painter = image,
                    contentDescription = null,
                    modifier = Modifier
                        .size(110.dp) // Задайте размер, который вам нужен
                )
                GreetingText()
            }
        }

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



@Composable
fun GreetingText() {
    Column(
        modifier = Modifier
            .padding(horizontal = 50.dp)


    ) {
       Text(
            text =  stringResource(R.string.app_name_text),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = colorResource(R.color.teal_700),
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
        )

    }
}

