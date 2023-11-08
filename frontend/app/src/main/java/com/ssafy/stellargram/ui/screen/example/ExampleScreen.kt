package com.ssafy.stellargram.ui.screen.example

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ssafy.stellargram.module.DBModule
import com.ssafy.stellargram.module.ScreenModule
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExampleScreen(navController : NavController, modifier: Modifier){

    // TODO: 기기로부터 정보를 받는 거로 고쳐야 함.
    val longitude: Double = 127.039611
    val latitude: Double = 37.501254

    var offsetX: Double by remember { mutableStateOf(0.0) }
    var offsetY: Double by remember { mutableStateOf(0.0) }
    var theta: Double by remember { mutableStateOf(0.0)}
    var phi: Double by remember { mutableStateOf(0.0) }
    var isDragging: Boolean by remember { mutableStateOf(false) }
    var LST: Double by remember{mutableStateOf(0.0)}
    var i: Int by remember{mutableStateOf(0)}

    val viewModel: ExampleViewModel = viewModel()

    LaunchedEffect(Unit) {
        viewModel.createStarData(DBModule.gettingStarArray(), DBModule.gettingNameMap())
        viewModel.setScreenSize(ScreenModule.gettingWidth(), ScreenModule.gettingHeight())
        while (true) {
            i++
            delay(1000L) // 1초마다 함수 호출
            LST = viewModel.getMeanSiderealTime(longitude)
            viewModel.getSight(longitude, latitude, LST, theta, phi, viewModel.starData.value)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){

    }

}

//@Preview
//@Composable
//fun plotAllStars(){
//
//
//    Canvas(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(color = Color.Black),
//        onDraw = {
//
//            drawCircle(
//                brush = Brush.radialGradient(colors = listOf(Color.Yellow, Color.Black), radius = 12.0f),
//                radius = 12.0f
//            )
//            drawCircle(
//                color = Color.Yellow,
//                radius = 4.0F
//            )
//        }
//    )
//}