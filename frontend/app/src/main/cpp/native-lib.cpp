#include <jni.h>
#include <string>
#include <vector>
#include <thread>
#include <android/log.h>

double starList1[120000][5];
double constList1[20000][2];

int starList_size = 0;
int constList_size = 0;

double starTrans1[120000][6];
double constTrans1[20000][3];


double starResult1[120000][5];
double constResult1[20000][2];

std::vector<double> temp(3, 0.0);
//---------------------------------------------------------------
/*
 * starList, constellationLineList 최초 등록
*/

extern "C"{
    JNIEXPORT void JNICALL
    Java_com_ssafy_stellargram_ui_MainActivity_setStarList(
            JNIEnv* env,
            jobject,
            jobjectArray array
    ){
        jsize rowSize = env -> GetArrayLength(array);
        starList_size = rowSize;

        for(jsize i = 0; i < rowSize; i++) {
            jdoubleArray oneRow = (jdoubleArray)env ->GetObjectArrayElement(array, i);
            jsize colSize = env -> GetArrayLength(oneRow);
            std::vector<double> row(colSize);
            env ->GetDoubleArrayRegion(oneRow, 0, colSize, &row[0]);
            env ->DeleteLocalRef(oneRow);

            for(int j = 0; j < 5; j++){
                starList1[i][j] = row[j];
            }

        }
        std::vector<std::thread> threads;
        int threadCount = std::thread::hardware_concurrency();

        __android_log_print(ANDROID_LOG_DEBUG, "test", "thread count: %d", threadCount);
    };

    JNIEXPORT void JNICALL
    Java_com_ssafy_stellargram_ui_MainActivity_setLineList(
            JNIEnv* env,
            jobject,
            jobjectArray array
    ){
        jsize rowSize = env -> GetArrayLength(array);
        constList_size = rowSize;

        for(jsize i = 0; i < rowSize; i++) {
            jdoubleArray oneRow = (jdoubleArray)env ->GetObjectArrayElement(array, i);
            jsize colSize = env -> GetArrayLength(oneRow);
            std::vector<double> row(colSize);
            env ->GetDoubleArrayRegion(oneRow, 0, colSize, &row[0]);
            env ->DeleteLocalRef(oneRow);

            for(int j = 0; j < 2; j++){
                constList1[i][j] = row[j];
            }
        }
        __android_log_print(ANDROID_LOG_DEBUG, "test", "const Length: %d", constList_size);
    };

}

//---------------------------------------------------------------
/*
 * util 함수들 등록
*/

//현재 경도의 LST를 구하는 함수.
double getLocalSiderealTime(double longitude) {
    auto now = std::chrono::system_clock::now();
    auto duration = now.time_since_epoch();
    auto millis = std::chrono::duration_cast<std::chrono::milliseconds>(duration).count();
    double JD = (millis / 1000.0) / 86400.0 + 2440587.5;
    double GMST = 18.697374558 + 24.06570982441908 * (JD - 2451545);
    double theta = fmod(GMST * 15.0 + longitude, 360.0);
    return theta * M_PI / 180.0;
}

void starTransCoor(double longitude, double latitude){
    double sidereal = getLocalSiderealTime(longitude);
    double newLatitude = latitude * M_PI / 180.0;
    double sinPhi = sin(newLatitude);
    double cosPhi = cos(newLatitude);

    for(size_t i = 0; i < starList_size; i++){
        double hourAngle = sidereal - starList1[i][0];
        double sinDec = sin(starList1[i][1]);
        double cosDec = cos(starList1[i][1]);
        double sina = sinDec * sinPhi + cosDec * cosPhi * cos(hourAngle);
        double cosa = sqrt(1.0 - (sina * sina));
        double sinA = -sin(hourAngle) * cosDec / cosa;
        double cosA = (sinDec - (sinPhi * sina)) / (cosPhi * cosa);

        starTrans1[i][0] = cosa * cosA;
        starTrans1[i][1] = cosa * sinA;
        starTrans1[i][2] = sina;
        starTrans1[i][3] = starList1[i][2];
        starTrans1[i][4] = starList1[i][3];
        starTrans1[i][5] = starList1[i][4];
    }
}

void constTransCoor(double longitude, double latitude){
    double sidereal = getLocalSiderealTime(longitude);
    double newLatitude = latitude * M_PI / 180.0;
    double sinPhi = sin(newLatitude);
    double cosPhi = cos(newLatitude);

    for(size_t i = 0; i < constList_size; i++){
        double hourAngle = sidereal - constList1[i][0];
        double sinDec = sin(constList1[i][1]);
        double cosDec = cos(constList1[i][1]);
        double sina = sinDec * sinPhi + cosDec * cosPhi * cos(hourAngle);
        double cosa = sqrt(1.0 - (sina * sina));
        double sinA = -sin(hourAngle) * cosDec / cosa;
        double cosA = (sinDec - (sinPhi * sina)) / (cosPhi * cosa);

        constTrans1[i][0] = cosa * cosA;
        constTrans1[i][1] = cosa * sinA;
        constTrans1[i][2] = sina;
    }
}

void starSight(double _theta, double _phi, float _zoom){
    double theta = _theta * M_PI / 180.0;
    double phi = _phi * M_PI / 180.0;
    double cosTheta = cos(theta);
    double sinTheta = sin(theta);
    double cosPhi = cos(phi);
    double sinPhi = sin(phi);

    double transMatrix[3][3] = {
    {cosTheta * cosPhi, -cosTheta * sinPhi, sinTheta},
    {sinTheta * cosPhi, -sinTheta * sinPhi, -cosTheta},
    {sinPhi, cosPhi, 0.0}
    };

    for(size_t i = 0; i < starList_size; i++){
        starResult1[i][2] = starTrans1[i][3];
        starResult1[i][3] = starTrans1[i][4];
        starResult1[i][4] = starTrans1[i][5];

        for(size_t j = 0; j < 3; ++j) {
            for(size_t k = 0; k < 3; ++k) {
                temp[j] += starTrans1[i][k] * transMatrix[k][j];
            }
        }

        double a = asin(temp[2]);
        double cosa = cos(a);
        if(std::abs(cosa) < 1.0E-6) {
            starResult1[i][0] = 0.0;
            starResult1[i][1] = 1000000.0;
            continue;
        }
        double _sin = temp[1] / cosa;
        double _cos = temp[0] / cosa;

        double new_theta = _cos > 0 ? asin(_sin) : M_PI - asin(_sin);
        starResult1[i][0] = -1500.0 * new_theta * pow(10.0, _zoom);
        starResult1[i][1] = -1500.0 * log(std::abs((1 + sin(a)) / cosa)) * pow(10.0, _zoom);
        temp[0] = 0.0; temp[1] = 0.0; temp[2] = 0.0;
    }
}

void constSight(double _theta, double _phi, float _zoom){
    double theta = _theta * M_PI / 180.0;
    double phi = _phi * M_PI / 180.0;
    double cosTheta = cos(theta);
    double sinTheta = sin(theta);
    double cosPhi = cos(phi);
    double sinPhi = sin(phi);

    double transMatrix[3][3] = {
            {cosTheta * cosPhi, -cosTheta * sinPhi, sinTheta},
            {sinTheta * cosPhi, -sinTheta * sinPhi, -cosTheta},
            {sinPhi, cosPhi, 0.0}
    };

    for(size_t i = 0; i < constList_size; i++){
        for(size_t j = 0; j < 3; ++j) {
            for(size_t k = 0; k < 3; ++k) {
                temp[j] += constTrans1[i][k] * transMatrix[k][j];
            }
        }

        double a = asin(temp[2]);
        double cosa = cos(a);
        if(std::abs(cosa) < 1.0E-6) {
            constResult1[i][0] = 0.0;
            constResult1[i][1] = 1000000.0;
            continue;
        }
        double _sin = temp[1] / cosa;
        double _cos = temp[0] / cosa;

        double new_theta = _cos > 0 ? asin(_sin) : M_PI - asin(_sin);
        constResult1[i][0] = -1500.0 * new_theta * pow(10.0, _zoom);
        constResult1[i][1] = -1500.0 * log(std::abs((1 + sin(a)) / cosa)) * pow(10.0, _zoom);
        temp[0] = 0.0; temp[1] = 0.0; temp[2] = 0.0;
    }
}

//---------------------------------------------------------------
/*
 * 계산 로직 함수
*/

void getAllStars(double longitude, double latitude){
    starTransCoor(longitude, latitude);
}

void getAllConstellationLines(double longitude, double latitude){
    constTransCoor(longitude, latitude);
}

std::vector<std::vector<double>> getVisibleStars(double _limit, double screenHeight, double screenWidth) {
    std::vector<std::vector<double>> visible;
    for (auto star : starResult1) {
        if (star[3] > _limit ||
            std::abs(star[0]) > screenHeight / 2.0 ||
            std::abs(star[1]) > screenWidth / 2.0) {
            continue;
        }
        visible.push_back(std::vector<double>{star[0], star[1], star[2], star[3], star[4]});
    }
    return visible;
}

std::vector<std::vector<double>> getVisibleConstellationLines(double screenHeight, double screenWidth){
    std::vector<std::vector<double>> visible;
    for (int i = 0; i < (constList_size >> 1); i++){
        auto st = constList1[i << 1];
        auto fi = constList1[i << 1 | 1];
        if(std::abs(st[0]) > screenHeight / 2.0 ||
            std::abs(st[1]) > screenWidth / 2.0 ||
            std::abs(fi[0]) > screenHeight / 2.0 ||
            std::abs(fi[1]) > screenWidth / 2.0
        ){
            continue;
        }
        visible.push_back(std::vector<double>{st[0], st[1], fi[0], fi[1]});
    }
    return visible;
}
//---------------------------------------------------------------
/*
 * 쓰레드 관련 함수
*/

void starThread(double longitude, double latitude, double _theta, double _phi, float _zoom){
    getAllStars(longitude, latitude);
    starSight(_theta, _phi, _zoom);
}

void constThread(double longitude, double latitude, double _theta, double _phi, float _zoom){
    getAllConstellationLines(longitude, latitude);
    constSight(_theta, _phi, _zoom);
}

//---------------------------------------------------------------
/*
 * JNI 통신 함수
*/

extern "C"{

    JNIEXPORT jobjectArray JNICALL
    Java_com_ssafy_stellargram_ui_screen_skymap_SkyMapViewModel_getAllStars(
            JNIEnv* env,
            jobject,
            jdouble longitude,
            jdouble latitude,
            jdouble zoom,
            jdouble theta,
            jdouble phi,
            jdouble limit,
            jdouble screenHeight,
            jdouble screenWidth
    )
    {
        auto now = std::chrono::system_clock::now();
        auto duration = now.time_since_epoch();
        auto millis = std::chrono::duration_cast<std::chrono::milliseconds>(duration).count();

        jclass doubleArrayClass = env->FindClass("[D");

        starThread(longitude, latitude, theta, phi, zoom);

        auto visibleStars = getVisibleStars(limit, screenHeight, screenWidth);

        now = std::chrono::system_clock::now();
        duration = now.time_since_epoch();
        auto millis1 = std::chrono::duration_cast<std::chrono::milliseconds>(duration).count();
        __android_log_print(ANDROID_LOG_DEBUG, "calc", "elapsed Time: %d ms", millis1 - millis);

        int numRows = visibleStars.size();

        // 결과 배열 생성 및 반환
        jobjectArray resultArray = env->NewObjectArray(numRows, doubleArrayClass, NULL);
        for (int i = 0; i < numRows; i++) {
            jdoubleArray rowArray = env->NewDoubleArray(5);
            env->SetDoubleArrayRegion(rowArray, 0, 5, visibleStars[i].data());
            env->SetObjectArrayElement(resultArray, i, rowArray);
            env->DeleteLocalRef(rowArray);
        }
        return resultArray;
    }

    JNIEXPORT jobjectArray JNICALL
    Java_com_ssafy_stellargram_ui_screen_skymap_SkyMapViewModel_getAllConstellationLines(
            JNIEnv* env,
            jobject,
            jdouble longitude,
            jdouble latitude,
            jdouble zoom,
            jdouble theta,
            jdouble phi,
            jdouble screenHeight,
            jdouble screenWidth
    ){
        jclass doubleArrayClass = env->FindClass("[D");
        constThread(longitude, latitude, theta, phi, zoom);
        auto visibleLines = getVisibleConstellationLines(screenHeight, screenWidth);

        __android_log_print(ANDROID_LOG_DEBUG, "calc", "check");

        int numRows = visibleLines.size();

        // 결과 배열 생성 및 반환
        jobjectArray resultArray = env->NewObjectArray(numRows, doubleArrayClass, NULL);
        for (int i = 0; i < numRows; i++) {
            jdoubleArray rowArray = env->NewDoubleArray(4);
            env->SetDoubleArrayRegion(rowArray, 0, 4, visibleLines[i].data());
            env->SetObjectArrayElement(resultArray, i, rowArray);
            env->DeleteLocalRef(rowArray);
        }
        return resultArray;
    }
}