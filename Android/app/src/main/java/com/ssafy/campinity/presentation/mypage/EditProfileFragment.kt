package com.ssafy.campinity.presentation.mypage

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.ssafy.campinity.ApplicationClass
import com.ssafy.campinity.R
import com.ssafy.campinity.databinding.FragmentEditProfileBinding
import com.ssafy.campinity.presentation.base.BaseFragment
import com.ssafy.campinity.presentation.collection.CollectionDeleteFileDialog
import com.ssafy.campinity.presentation.collection.CreateFileFragment
import com.ssafy.campinity.presentation.collection.FileDeleteDialogListener
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class EditProfileFragment : BaseFragment<FragmentEditProfileBinding>(R.layout.fragment_edit_profile),
    FileDeleteDialogListener {

    private val myPageViewModel by activityViewModels<MyPageViewModel>()
    private val fromAlbumActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        result.data?.let {
            if (it.data != null) {
                myPageViewModel.setProfileImg(
                    it.data as Uri,
                    File(absolutelyPath(it.data, requireContext()))
                )
            }
        }
    }

    override fun initView() {
        setData()
        initListener()
        setTextWatcher()
        myPageViewModel.checkSame()
    }

    private fun setData() {
        myPageViewModel.userInfo.observe(viewLifecycleOwner){ response->
            binding.etNickname.setText(response?.name.toString())
            Glide.with(requireContext())
                .load("http://i8d101.p.ssafy.io:8003/images" + response?.imagePath.toString())
                .placeholder(R.drawable.ic_profile_default)
                .error(R.drawable.ic_profile_default)
                .circleCrop()
                .into(binding.ivProfileImage)
        }
        myPageViewModel.getInfo()
    }

    private fun initListener() {
        binding.apply {
            ivProfileImage.setOnClickListener { setAlbumView() }
            etNickname.addTextChangedListener { myPageViewModel.setNickname(it.toString()) }
            btnBack.setOnClickListener { popBackStack() }
            btnConfirm.setOnClickListener {
                if (myPageViewModel.profileImgUri.value == null) {
                    myPageViewModel.updateProfileWithoutImg(ApplicationClass.preferences.fcmToken.toString())
                    myPageViewModel.getInfo()
                } else {
                    myPageViewModel.updateProfile(ApplicationClass.preferences.fcmToken.toString())
                    myPageViewModel.getInfo()
                }
                myPageViewModel.isSuccess.observe(viewLifecycleOwner) {
                    Log.d("myPageViewModel.isSuccess", "initListener: ${myPageViewModel.isSuccess.value}")
                    if (it == true) {
                        popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            btnCheckDuplication.setOnClickListener {
                if (myPageViewModel.nickname.value == null) {
                    Toast.makeText(requireContext(), "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
                } else {
                    myPageViewModel.checkDuplication()
                    if (myPageViewModel.isDuplicate.value == true) {
                        Toast.makeText(requireContext(), "중복된 닉네임입니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "사용할 수 있는 닉네임입니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setTextWatcher() {
        binding.etNickname.addTextChangedListener {
            myPageViewModel.nickname.value = binding.etNickname.text.toString()
        }
    }

    private fun setAlbumView() {
        if (myPageViewModel.profileImgUri.value == null) {
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) -> {
                    fromAlbumActivityLauncher.launch(
                        Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )
                    )
                }
                else -> {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        CreateFileFragment.REQUEST_READ_STORAGE_PERMISSION
                    )
                }
            }
        } else {
            val dialog = CollectionDeleteFileDialog(requireContext(), this)
            dialog.show()
        }
    }

    private fun absolutelyPath(path: Uri?, context: Context): String {
        val proj: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        val c: Cursor? = context.contentResolver.query(path!!, proj, null, null, null)
        val index = c?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        c?.moveToFirst()
        val result = c?.getString(index!!)
        c?.close()
        return result!!
    }

    override fun onButtonClicked() {
        myPageViewModel.profileImgUri.value = null
    }
}