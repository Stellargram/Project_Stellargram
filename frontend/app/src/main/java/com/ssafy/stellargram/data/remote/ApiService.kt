package com.ssafy.stellargram.data.remote

import com.google.gson.annotations.SerializedName
import com.ssafy.stellargram.model.AstronomicalEventResponse
import com.ssafy.stellargram.model.CardsResponse
import com.ssafy.stellargram.model.CursorResponse
import com.ssafy.stellargram.model.MessageListResponse
import com.ssafy.stellargram.model.RoomListResponse
import com.ssafy.stellargram.model.MemberCheckDuplicateRequest
import com.ssafy.stellargram.model.MemberCheckDuplicateResponse
import com.ssafy.stellargram.model.MemberCheckResponse
import com.ssafy.stellargram.model.MemberMeResponse
import com.ssafy.stellargram.model.MemberResponse
import com.ssafy.stellargram.model.MemberSignUpRequest
import com.ssafy.stellargram.model.MemberSignUpResponse
import com.ssafy.stellargram.model.WeatherResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

     @GET("member/check")
     suspend fun getMemberCheck(): Response<MemberCheckResponse>

     @POST("member/check-duplicate/")
     suspend fun getMemberCheckDuplicate(@Body getMemberCheckDuplicateRequest: MemberCheckDuplicateRequest): Response<MemberCheckDuplicateResponse>

     @POST("member/signup")
     suspend fun postMemberSignUP(@Body postMemberSignUpRequest : MemberSignUpRequest) : Response<MemberSignUpResponse>

     // 특정회원 정보조회 (본인도 대상으로 가능)
     @GET("member/others/{userId}")
     suspend fun getMember(
         @Path("userId") userId: Long
     ) : Response<MemberResponse>

    // 내 정보 조회
    @GET("member/me")
    suspend fun getMemberMe() : Response<MemberMeResponse>

    // 닉네임 수정
    @PATCH("member/nickname")
    suspend fun patchNickName(@Body nickname: String): Response<MemberMeResponse>

    // 프로필 이미지 수정 TODO: 파일 어떻게 넣는지 알아보기
    @PATCH("member/profile-image")
    suspend fun patchProfileImage(@Body profileImageFile: String): Response<MemberMeResponse>

    // 회원 탈퇴 -> 추후 구현 예정
    @PATCH("member/withdrawal")
    suspend fun withdrawal(@Body nickname: String): Response<MemberMeResponse>

    // 특정 사용자 팔로우 API
    @GET("follow/{followingId}")
    suspend fun followUser(
        @Path("followingId") followingId: Long
    ): Response<MemberCheckResponse>

    // 특정 사용자 팔로우 취소 API
    @DELETE("follow/{followingId}")
    suspend fun unfollowUser(
        @Path("followingId") followingId: Long
    ): Response<MemberCheckResponse>


}
data class NickNameUpdateRequest(
    @SerializedName("nickname") val nickname: String
)

interface ApiServiceForCards {
    // 내 카드 전체 조회
    @GET("/starcard/{memberId}")
    suspend fun getCards(
        @Path("memberId") memberId: Long
    ): Response<CardsResponse>

    // 내가 좋아하는 카드 전체 조회
    @GET("/starcard/{memberId}")
    suspend fun getLikeCards(
        @Path("memberId") memberId: Long
    ): Response<CardsResponse>

}

interface ApiServiceForWeather{
    @GET("/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst")
    fun getWeatherData(
        @Query("ServiceKey") serviceKey: String,
        @Query("pageNo") pageNo: Int,
        @Query("numOfRows") numOfRows: Int,
        @Query("dataType") dataType: String,
        @Query("base_date") baseDate: String,
        @Query("base_time") baseTime: String,
        @Query("nx") nx: Int,
        @Query("ny") ny: Int
    ): Call<WeatherResponse>
}
interface ApiServiceForAstronomicalEvents {
    @GET("B090041/openapi/service/AstroEventInfoService/getAstroEventInfo")
    suspend fun getAstronomicalEvents(
        @Query("solYear") solYear: String,
        @Query("solMonth") solMonth: String,
        @Query("ServiceKey") serviceKey: String,
        @Query("numOfRows") numOfRows: Int
    ): Response<AstronomicalEventResponse>
}


// 채팅 관련
interface ApiServiceForChat {
    // 내 채팅방 목록 가져오기
    @GET("chat/rooms")
    suspend fun getRoomList(
        @Header("myId") myId: Long
    ): RoomListResponse

    // 특정 채팅방의 이전 메세지 가져오기
    @GET("chat/open/{chatRoomId}/{cursor}")
    suspend fun getPrevChats(
        @Header("myId") myId: Long,
        @Path("chatRoomId") chatRoomId: Int,
        @Path("cursor") cursor: Int,
    ): MessageListResponse

    // 특정 채팅방의 가장 마지막 커서 가져오기
    @GET("chat/recentCurser/{chatRoomId}")
    suspend fun getRecentCursor(
        @Path("chatRoomId") chatRoomId: Int,
    ): CursorResponse
}
