package com.ssafy.campinity.presentation.mypage

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.ssafy.campinity.ApplicationClass
import com.ssafy.campinity.R
import com.ssafy.campinity.common.util.BindingAdapters.setProfileImg
import com.ssafy.campinity.common.util.BindingAdapters.setProfileImgString
import com.ssafy.campinity.databinding.FragmentMyPageBinding
import com.ssafy.campinity.presentation.base.BaseFragment
import com.ssafy.campinity.presentation.collection.CollectionDeleteFileDialog
import com.ssafy.campinity.presentation.collection.CreateCollectionFragment
import com.ssafy.campinity.presentation.collection.FileDeleteDialogListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class MyPageFragment :
    BaseFragment<FragmentMyPageBinding>(R.layout.fragment_my_page),
    LogoutDialogListener, FileDeleteDialogListener {

    private lateinit var callback: OnBackPressedCallback
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

                Glide.with(requireContext())
                    .load(it.data as Uri)
                    .placeholder(R.drawable.ic_profile_image)
                    .error(R.drawable.ic_profile_image)
                    .circleCrop()
                    .into(binding.ivProfileImage)
            }
        }
    }

    override fun initView() {
        setData()
        initListener()
        setTextWatcher()
        observeState()
        initTabLayout()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        myPageViewModel.clearData()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.slMyPage.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    binding.slMyPage.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
                } else {
                    onDetach()
                    myPageViewModel.clearData()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }

    private fun initTabLayout() {
        binding.apply {
            tlMyPage.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {}

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
            vpMyNote.adapter = MyPageAdapter(this@MyPageFragment)
            val tabTitles = listOf("쪽지", "스크랩")
            TabLayoutMediator(
                tlMyPage,
                vpMyNote
            ) { tab, position ->
                tab.text = tabTitles[position]
            }.attach()
        }
    }

    // 파일 삭제 버튼 클릭시
    override fun onConfirmButtonClicked() {
        myPageViewModel.profileImgStr.value = null
        myPageViewModel.profileImgUri.value = null
        myPageViewModel.profileImgMultiPart = null
    }

    private fun observeState() {
        myPageViewModel.isDuplicate.observe(viewLifecycleOwner) {
            if (it == false) {
                binding.btnConfirm.apply {
                    setBackgroundResource(R.drawable.bg_rect_grey_radius10)
                    isEnabled = false
                }
            } else {
                binding.btnConfirm.apply {
                    setBackgroundResource(R.drawable.bg_rect_bilbao_radius10)
                    isEnabled = true
                }
            }
        }

        myPageViewModel.nicknameCheck.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.btnCheckDuplication.apply {
                    setBackgroundResource(R.drawable.bg_rect_white_smoke_radius15)
                    isEnabled = false
                    setTextColor(Color.GRAY)
                }
            } else {
                binding.btnCheckDuplication.apply {
                    setBackgroundResource(R.drawable.bg_rect_bilbao_white_radius15_stroke1)
                    isEnabled = true
                    setTextColor(Color.parseColor("#467F26"))
                }
            }
        }
        myPageViewModel.isDuplicate.observe(viewLifecycleOwner) {
            if (it == true) {
                binding.btnConfirm.apply {
                    setBackgroundResource(R.drawable.bg_rect_grey_radius10)
                    isEnabled = false
                }
            } else {
                binding.btnConfirm.apply {
                    setBackgroundResource(R.drawable.bg_rect_bilbao_radius10)
                    isEnabled = true
                }
            }
        }

        myPageViewModel.detailData.observe(viewLifecycleOwner) { response ->
            response.let {
                if (it != null) {
                    ReviewNoteDialog(requireContext(), it).show()
                }
            }
        }
    }

    private fun setTextWatcher() {
        binding.etNickname.addTextChangedListener {
            myPageViewModel.nicknameCheck.value = binding.etNickname.text.toString()
        }
    }

    private fun setData() {
        // myPage
        myPageViewModel.nickname.observe(viewLifecycleOwner) {
            binding.tvNickname.text = it
        }
        myPageViewModel.profileImgUri.observe(viewLifecycleOwner) {
            binding.ivProfile.setProfileImg(it)
        }
        myPageViewModel.profileImgStr.observe(viewLifecycleOwner) {
            binding.ivProfile.setProfileImgString(it)
        }
        myPageViewModel.getInfo()

        // edit
        myPageViewModel.profileImgUri.observe(viewLifecycleOwner) {
            binding.ivProfileImage.setProfileImg(it)
        }
        myPageViewModel.profileImgStr.observe(viewLifecycleOwner) {
            binding.ivProfileImage.setProfileImgString(it)
        }
        binding.etNickname.setText(myPageViewModel.nickname.value)
        binding.ivProfileImage.setProfileImgString(myPageViewModel.profileImgStr.value)
    }

    private fun initListener() {

        val slidePanel = binding.slMyPage
        slidePanel.addPanelSlideListener(PanelEventListener())
        slidePanel.isTouchEnabled = false

        binding.clEditProfile.setOnClickListener {
            // 닫힌 상태일 경우 열기
            if (slidePanel.panelState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                slidePanel.panelState = SlidingUpPanelLayout.PanelState.ANCHORED
            }
        }

        binding.ivArrowLeft.setOnClickListener {
            popBackStack()
        }

        binding.tvLogOut.setOnClickListener {
            val dialog = LogoutDialog(requireContext(), this)
            dialog.show()
        }

        // edit
        binding.apply {
            ivProfileImage.setOnClickListener { setAlbumView() }
            btnBack.setOnClickListener {
                if (slidePanel.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    slidePanel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
                }
            }
            btnCheckDuplication.setOnClickListener {
                if (myPageViewModel.nickname.value == null) {
                    showToast("닉네임을 입력해주세요.")
                } else {
                    lifecycleScope.launch {
                        val async = myPageViewModel.checkDuplication(etNickname.text.toString())
                        showDuplicateInfo(async)
                    }
                }
            }
            // 수정 확인 버튼을 눌렀을 때
            btnConfirm.setOnClickListener {
                myPageViewModel.isSuccess.value = false
                ApplicationClass.preferences.nickname = etNickname.text.toString()
                if (myPageViewModel.profileImgUri.value != null) {
                    myPageViewModel.updateProfile(etNickname.text.toString())
                } else {
                    if (myPageViewModel.profileImgStr.value == null) {
                        myPageViewModel.updateProfileWithoutImg(etNickname.text.toString())
                    } else {
                        myPageViewModel.updateProfileWithExistingImg(etNickname.text.toString())
                    }
                }
                if (slidePanel.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    slidePanel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
                }
                showToast("프로필이 수정되었습니다.")
            }
        }
    }

    private fun showDuplicateInfo(async: Int) {
        if (myPageViewModel.isDuplicate.value == true) {
            showToast("중복된 닉네임입니다.")
        } else {
            showToast("사용 가능한 닉네임입니다.")
        }
    }

    // 로그아웃
    override fun onSubmitButtonClicked() {
        myPageViewModel.requestLogout()
        myPageViewModel.isLoggedOut.observe(viewLifecycleOwner) {
            if (it == true) {
                ApplicationClass.preferences.apply {
                    accessToken = null
                    refreshToken = null
                    fcmToken = null
                    isLoggedIn = false
                }
                navigate(MyPageFragmentDirections.actionMyPageFragmentToOnBoardingFragment())
                showToast("로그아웃 되었습니다.")
            } else {
                showToast("다시 시도해주세요.")
            }
        }
    }

    private fun setAlbumView() {
        if (myPageViewModel.profileImgUri.value == null && myPageViewModel.profileImgStr.value == null) {
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
                        CreateCollectionFragment.REQUEST_READ_STORAGE_PERMISSION
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

    // 이벤트 리스너
    inner class PanelEventListener : SlidingUpPanelLayout.PanelSlideListener {
        // 패널이 슬라이드 중일 때
        override fun onPanelSlide(panel: View?, slideOffset: Float) {}

        // 패널의 상태가 변했을 때
        override fun onPanelStateChanged(
            panel: View?,
            previousState: SlidingUpPanelLayout.PanelState?,
            newState: SlidingUpPanelLayout.PanelState?
        ) {
        }
    }
}