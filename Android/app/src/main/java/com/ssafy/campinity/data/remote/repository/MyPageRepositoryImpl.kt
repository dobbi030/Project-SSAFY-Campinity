package com.ssafy.campinity.data.remote.repository

import com.ssafy.campinity.common.util.wrapToResource
import com.ssafy.campinity.data.remote.Resource
import com.ssafy.campinity.data.remote.datasource.mypage.LogoutRequest
import com.ssafy.campinity.data.remote.datasource.mypage.MyPageRemoteDataSource
import com.ssafy.campinity.domain.entity.mypage.MyPageNote
import com.ssafy.campinity.domain.entity.mypage.MyPageUser
import com.ssafy.campinity.domain.entity.mypage.ScrapCampsite
import com.ssafy.campinity.domain.entity.user.User
import com.ssafy.campinity.domain.repository.MyPageRepository
import kotlinx.coroutines.Dispatchers
import okhttp3.MultipartBody
import javax.inject.Inject

class MyPageRepositoryImpl @Inject constructor(
    private val myPageRemoteDataSource: MyPageRemoteDataSource
) : MyPageRepository {

    override suspend fun getNotes(): Resource<MyPageNote> =
        wrapToResource(Dispatchers.IO) {
            myPageRemoteDataSource.getNotes().toDomainModel()
        }

    override suspend fun getUserInfo(): Resource<MyPageUser> =
        wrapToResource(Dispatchers.IO) {
            myPageRemoteDataSource.getUserInfo().toDomainModel()
        }

    override suspend fun requestLogout(body: LogoutRequest): Resource<Boolean> =
        wrapToResource(Dispatchers.IO) {
            myPageRemoteDataSource.requestLogout(body)
        }

    override suspend fun editUserInfo(
        nickName: String,
        isChanged: Boolean,
        profileImg: MultipartBody.Part?
    ): Resource<User> =
        wrapToResource(Dispatchers.IO) {
            myPageRemoteDataSource.editUserInfo(nickName, isChanged, profileImg).toDomainModel()
        }

    override suspend fun getScrapCampsites(): Resource<List<ScrapCampsite>> =
        wrapToResource(Dispatchers.IO) {
            myPageRemoteDataSource.getScrapCampsites().map { it.toDomainModel() }
        }
}