package com.ssafy.campinity.presentation.home

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.ssafy.campinity.ApplicationClass
import com.ssafy.campinity.R
import com.ssafy.campinity.common.util.BindingAdapters.setProfileImgString
import com.ssafy.campinity.common.util.LinearItemDecoration
import com.ssafy.campinity.data.remote.service.FirebaseService
import com.ssafy.campinity.databinding.FragmentHomeBinding
import com.ssafy.campinity.presentation.base.BaseFragment
import com.ssafy.campinity.presentation.mypage.MyPageViewModel
import com.ssafy.campinity.presentation.search.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {

    private lateinit var callback: OnBackPressedCallback

    private val homeViewModel by viewModels<HomeViewModel>()
    private val myPageViewModel by activityViewModels<MyPageViewModel>()
    private val searchViewModel by activityViewModels<SearchViewModel>()

    private val homeBannerAdapter by lazy { HomeBannerAdapter(this::getCurationDetail) }
    private val homeCollectionAdapter by lazy { HomeCollectionAdapter(this::getCollection) }
    private val highestCampingSiteAdapter by lazy { HighestCampingSiteAdapter(this::getCampsite) }
    private val hottestCampingSiteAdapter by lazy { HottestCampingSiteAdapter(this::getCampsite) }
    private val handler = Handler(Looper.getMainLooper()) {
        setPage()
        true
    }
    private var currentPage = 0
    private var waitTime = 0L

    override fun initView() {
        setData()
        homeViewModel.requestCurrentToken()
        getFCMToken()
        initListener()
        initCollection()
        initBanner()
        initRankingCampsite()
        initObserver()
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.getHomeBanners()
        homeViewModel.getHomeCollections()
        homeViewModel.getHighestCampsites()
        homeViewModel.getHottestCampsites()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (System.currentTimeMillis() - waitTime >= 2500) {
                    waitTime = System.currentTimeMillis()
                    showToast("뒤로가기 버튼을\n한번 더 누르면 종료됩니다.")
                } else {
                    requireActivity().finishAffinity()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }

    private fun setData() {
        myPageViewModel.getInfo()
        myPageViewModel.profileImgStr.observe(viewLifecycleOwner) {
            binding.ivMyPage.setProfileImgString(it)
        }
    }

    private fun getFCMToken() {
        lifecycleScope.launch {
            val result = FirebaseService().getCurrentToken()
            ApplicationClass.preferences.fcmToken = result
        }
    }

    private fun initListener() {
        binding.apply {
            tvCollectionMore.setOnClickListener {
                navigate(HomeFragmentDirections.actionHomeFragmentToCollectionFragment())
            }
            clSearch.setOnClickListener {
                navigate(HomeFragmentDirections.actionHomeFragmentToSearchActivity())
            }
            clCommunity.setOnClickListener {
                navigate(HomeFragmentDirections.actionHomeFragmentToCommunityActivity())
            }
            tvBannerMore.setOnClickListener {
                navigate(HomeFragmentDirections.actionHomeFragmentToCurationFragment())
            }
            ivMyPage.setOnClickListener {
                navigate(HomeFragmentDirections.actionHomeFragmentToMyPageFragment())
            }
        }
    }

    private fun initCollection() {
        binding.rvCollectionHome.adapter = homeCollectionAdapter
        homeViewModel.homeCollections.observe(viewLifecycleOwner) { response ->
            response?.let {
                if (response.isEmpty()) {
                    binding.rvCollectionHome.visibility = View.INVISIBLE
                    binding.clEmptyCollection.visibility = View.VISIBLE
                } else {
                    binding.clEmptyCollection.visibility = View.INVISIBLE
                    homeCollectionAdapter.setCollection(response)
                }
            }
        }
        homeViewModel.getHomeCollections()
    }

    private fun initBanner() {
        val child = binding.vpBannerHome.getChildAt(0)
        val thread = Thread(PagerRunnable())

        binding.vpBannerHome.adapter = homeBannerAdapter
        binding.vpBannerHome.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        homeViewModel.homeBanners.observe(viewLifecycleOwner) { response ->
            response?.let { homeBannerAdapter.addHomeBanner(it) }
        }
        homeViewModel.getHomeBanners()

        (child as? RecyclerView)?.overScrollMode = View.OVER_SCROLL_NEVER
        thread.start()
    }

    private fun initRankingCampsite() {
        binding.rvPopularCampingSite.apply {
            adapter = hottestCampingSiteAdapter
            addItemDecoration(LinearItemDecoration(context, RecyclerView.HORIZONTAL, 15))
        }
        homeViewModel.hottestCampsites.observe(viewLifecycleOwner) { response ->
            response?.let {
                hottestCampingSiteAdapter.setCampsite(response)
            }
        }
        homeViewModel.getHottestCampsites()

        binding.rvScoreCampingSite.apply {
            adapter = highestCampingSiteAdapter
            addItemDecoration(LinearItemDecoration(context, RecyclerView.HORIZONTAL, 15))
        }
        homeViewModel.highestCampsites.observe(viewLifecycleOwner) { response ->
            response?.let {
                highestCampingSiteAdapter.setCampsite(response)
            }
        }
        homeViewModel.getHighestCampsites()
    }

    private fun getCurationDetail(curationId: String) {
        navigate(
            HomeFragmentDirections.actionHomeFragmentToCurationDetailFragment(curationId)
        )
    }

    private fun setPage() {
        if (currentPage == 3) currentPage = 0
        binding.vpBannerHome.setCurrentItem(currentPage, true)
        currentPage += 1
    }

    private fun getCollection(collectionId: String) {
        navigate(
            HomeFragmentDirections.actionHomeFragmentToCollectionDetailFragment(
                collectionId
            )
        )
    }

    private fun getCampsite(campsiteId: String) {
        lifecycleScope.launch {
            val sync = searchViewModel.getCampsiteDetailAsync(campsiteId)
            navigate(HomeFragmentDirections.actionHomeFragmentToCampsiteDetailFragment(sync, -1))
        }
    }

    private fun initObserver() {
        myPageViewModel.profileImgStr.observe(viewLifecycleOwner) {
            binding.ivMyPage.setProfileImgString(it)
        }
    }

    inner class PagerRunnable : Runnable {
        override fun run() {
            while (true) {
                try {
                    Thread.sleep(3000)
                    handler.sendEmptyMessage(0)
                } catch (e: InterruptedException) {
                    Log.e("interrupt", e.message.toString())
                }
            }
        }
    }
}