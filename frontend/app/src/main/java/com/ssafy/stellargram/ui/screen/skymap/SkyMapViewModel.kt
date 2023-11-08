package com.ssafy.stellargram.ui.screen.skymap

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ssafy.stellargram.data.db.repository.StarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.Math.PI
import java.lang.Math.abs
import java.lang.Math.asin
import java.lang.Math.cos
import java.lang.Math.sin
import java.lang.Math.sqrt
import javax.inject.Inject
import kotlin.math.ln

@HiltViewModel
class SkyMapViewModel @Inject constructor(
    private val repository: StarRepository
) : ViewModel()  {

    var starData: MutableState<Array<DoubleArray>> = mutableStateOf(arrayOf())
    var names: MutableState<HashMap<Int, String>> = mutableStateOf(hashMapOf())
    var screenWidth by mutableFloatStateOf(0f)
    var screenHeight by mutableFloatStateOf(0f)
    fun setScreenSize(width: Int, height: Int){
        Log.d("check", "${width} ${height}")
        screenWidth = width.toFloat()
        screenHeight = height.toFloat()
        Log.d("check", "${screenWidth} ${screenHeight}")
    }
    fun createStarData(Data: Array<DoubleArray>, Names: HashMap<Int, String>){
        Log.d("check", "ViewModel: ${starData.value.size}")
        starData.value = Data
        names.value = Names
        Log.d("check", "ViewModel: ${starData.value.size}")
    }

    fun getMeanSiderealTime(longitude: Double): Double{
        val JD: Double = (System.currentTimeMillis() * 0.001) / 86400.0 +  2440587.5
        val GMST = 18.697374558 + 24.06570982441908*(JD - 2451545)
        val theta = (GMST * 15.0 + (longitude)) % 360.0
        return theta * PI / 180.0
    }

    fun getAllStars(longitude: Double, latitude: Double, sidereal: Double, starList: Array<DoubleArray>): Array<DoubleArray>{

        val new_latitude = latitude * PI / 180.0

        val sinPhi = sin(new_latitude)
        val cosPhi = cos(new_latitude)

        var starArray = Array(starList.size) {DoubleArray(6)}

        for (i in 0 until starList.size){
            val hourAngle = sidereal - starList[i][0]
            val sinDec = sin(starList[i][1])
            val cosDec = cos(starList[i][1])
            val sina = sinDec * sinPhi + cosDec * cosPhi * cos(hourAngle)
            val cosa = sqrt(1.0 - (sina * sina))
            val sinA = -sin(hourAngle) * cosDec / cosa
            val cosA = (sinDec -(sinPhi * sina)) / (cosPhi * cosa)

            starArray[i][0] = cosa * cosA
            starArray[i][1] = cosa * sinA
            starArray[i][2] = sina
            starArray[i][3] = starList[i][2]
            starArray[i][4] = starList[i][3]
            starArray[i][5] = starList[i][4]
        }
        return starArray
    }

    fun getSight(longitude: Double, latitude: Double, sidereal: Double, _theta: Double, _phi: Double, starArray: Array<DoubleArray>): Array<DoubleArray> {
        val starData = getAllStars(longitude, latitude, sidereal, starArray)
        val theta = _theta * PI / 180.0
        val phi = _phi * PI / 180.0
        val cosTheta = cos(theta)
        val sinTheta = sin(theta)
        val cosPhi = cos(phi)
        val sinPhi = sin(phi)

        val transMatrix = arrayOf(
            doubleArrayOf(cosTheta * cosPhi, -cosTheta * sinPhi, sinTheta),
            doubleArrayOf(sinTheta * cosPhi, -sinTheta * sinPhi, -cosTheta),
            doubleArrayOf(sinPhi, cosPhi, 0.0)
        )
        val resultMatrix = Array(starData.size) {DoubleArray(5)}

        for(i in 0 until starData.size){
            resultMatrix[i][2] = starData[i][3]
            resultMatrix[i][3] = starData[i][4]
            resultMatrix[i][4] = starData[i][5]

            val temp = DoubleArray(3)
            for(j in 0 until 3){
                for(k in 0 until 3){
                    temp[j] += (starData[i][k] * transMatrix[k][j])
                }
            }
            val a = asin(temp[2])
            val cosa = cos(a)
            if(abs(cosa) <1.0E-6){
                resultMatrix[i][0] = 0.0
                resultMatrix[i][1] = 10000.0
                continue
            }
            val _sin = starData[i][1] / cosa
            val _cos = starData[i][0] / cosa

            val new_theta = if(_cos > 0) asin(_sin) else PI - asin(_sin)
            resultMatrix[i][0] = new_theta
            resultMatrix[i][1] = ln(abs((1 + sin(a)) / cosa))
        }
        return resultMatrix
    }


}
