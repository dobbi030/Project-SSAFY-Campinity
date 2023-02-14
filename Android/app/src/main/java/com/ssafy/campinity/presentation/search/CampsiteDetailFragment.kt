package com.ssafy.campinity.presentation.search

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.ssafy.campinity.R
import com.ssafy.campinity.common.util.LinearItemDecoration
import com.ssafy.campinity.common.util.getDeviceWidthPx
import com.ssafy.campinity.common.util.px
import com.ssafy.campinity.common.util.toString
import com.ssafy.campinity.data.remote.datasource.search.SearchReviewRequest
import com.ssafy.campinity.databinding.FragmentCampsiteDetailBinding
import com.ssafy.campinity.domain.entity.search.CampsiteDetailInfo
import com.ssafy.campinity.domain.entity.search.FacilityAndLeisureItem
import com.ssafy.campinity.domain.entity.search.Review
import com.ssafy.campinity.presentation.base.BaseFragment
import kotlinx.coroutines.launch

class CampsiteDetailFragment :
    BaseFragment<FragmentCampsiteDetailBinding>(R.layout.fragment_campsite_detail),
    CampsiteReviewDialogInterface {

    private lateinit var contentTheme: Array<String>
    private lateinit var contentFacility: Array<String>
    private lateinit var contentAmenity: Array<String>
    private lateinit var reviews: List<Review>

    private val searchViewModel by activityViewModels<SearchViewModel>()
    private val args by navArgs<CampsiteDetailFragmentArgs>()
    private val campsiteReviewAdapter by lazy {
        CampsiteReviewAdapter(
            requireContext(), reviews, this::deleteReview
        )
    }

    override fun initView() {
        initStringArray()
        initFragment()
        initListener()
    }

    private fun initStringArray() {
        contentTheme = resources.getStringArray(R.array.content_campsite_theme)
        contentFacility = resources.getStringArray(R.array.content_campsite_facility)
        contentAmenity = resources.getStringArray(R.array.content_campsite_amenity)
    }

    private fun initFragment() {
        binding.apply {
            btnGoBack.setOnClickListener {
                popBackStack()
            }

            searchViewModel.campsiteData.value.let { campsiteDetailInfo ->
                initViewPager()

                if (campsiteDetailInfo != null && campsiteDetailInfo.thumbnails.isEmpty()) {
                    binding.vpCampsiteImage.visibility = View.GONE
                    binding.ivIndicator.visibility = View.GONE
                }

                tvCampsiteIndustry.text =
                    if (campsiteDetailInfo != null && campsiteDetailInfo.industries.isNotEmpty()) {
                        campsiteDetailInfo.industries.toString(" | ")
                    } else {
                        tvCampsiteIndustry.visibility = View.GONE
                        ""
                    }
                tvCampsiteName.text =
                    if (campsiteDetailInfo != null && campsiteDetailInfo.campsiteName.isNotEmpty()) campsiteDetailInfo.campsiteName
                    else "이름 미등록 캠핑장"
                tvCampsiteShortContent.text = campsiteDetailInfo?.lineIntro
                tvCampsiteLocation.text =
                    if (campsiteDetailInfo != null && campsiteDetailInfo.address.isNotEmpty()) campsiteDetailInfo.address
                    else "미등록"
                tvCampsiteCall.text =
                    if (campsiteDetailInfo != null && campsiteDetailInfo.phoneNumber.isNotEmpty()) campsiteDetailInfo.phoneNumber
                    else "미등록"
                tvContentCampsiteOpenSeason.text =
                    if (campsiteDetailInfo != null && campsiteDetailInfo.openSeasons.isNotEmpty()) {
                        campsiteDetailInfo.openSeasons.toString(" | ")
                    } else {
                        tvTitleCampsiteOpenSeason.visibility = View.GONE
                        ""
                    }
                tvContentCampsitePet.text =
                    if (campsiteDetailInfo != null && campsiteDetailInfo.allowAnimal.isNotEmpty()) {
                        campsiteDetailInfo.allowAnimal
                    } else {
                        tvTitleCampsitePet.visibility = View.GONE
                        ""
                    }
                tvContentCampsiteHowToReserve.text =
                    if (campsiteDetailInfo != null && campsiteDetailInfo.reserveType.isNotEmpty()) {
                        campsiteDetailInfo.reserveType
                    } else {
                        tvTitleCampsiteHowToReserve.visibility = View.GONE
                        ""
                    }
                tvContentCampsiteHomepageUrl.text =
                    if (campsiteDetailInfo != null && campsiteDetailInfo.homepage.isNotEmpty()) {
                        campsiteDetailInfo.homepage
                    } else {
                        tvTitleCampsiteHomepageUrl.visibility = View.GONE
                        ""
                    }
                if (campsiteDetailInfo != null) {
                    if (campsiteDetailInfo.isScraped)
                        btnBookmark.setBackgroundResource(R.drawable.ic_bookmark_on)
                    else
                        btnBookmark.setBackgroundResource(R.drawable.ic_bookmark_off)
                }

                val viewTreeObserver = tvCampsiteLongContent.viewTreeObserver
                viewTreeObserver.addOnGlobalLayoutListener {
                    if (campsiteDetailInfo == null || campsiteDetailInfo.intro.isEmpty()) {
                        clCampsiteLongContent.visibility = View.GONE
                        tvCampsiteLongContent.text = ""
                    } else {
                        tvCampsiteLongContent.apply {
                            text = campsiteDetailInfo.intro
                            if (this.layout.getEllipsisCount(lineCount - 1) > 0) {
                                tvShowMore.setOnClickListener {
                                    it.visibility = View.GONE
                                    it.isClickable = false
                                    ellipsize = null
                                    maxLines = Int.MAX_VALUE
                                }
                            } else {
                                tvShowMore.visibility = View.GONE
                                tvShowMore.isClickable = false
                            }
                        }
                    }
                }

                mapFacilityAndLeisureList(campsiteDetailInfo!!)
            }
        }
    }

    private fun mapFacilityAndLeisureList(campsiteDetailInfo: CampsiteDetailInfo) {
        val facilityAndLeisureList: ArrayList<FacilityAndLeisureItem> = arrayListOf()

        campsiteDetailInfo.caravanFacilities.forEach {
            facilityAndLeisureList.add(
                FacilityAndLeisureItem(
                    resources.getIdentifier(
                        "ic_campsite_facility_$it", "drawable", requireContext().packageName
                    ), contentFacility[it - 1]
                )
            )
        }

        campsiteDetailInfo.glampingFacilities.forEach {
            if (!facilityAndLeisureList.contains(
                    FacilityAndLeisureItem(
                        resources.getIdentifier(
                            "ic_campsite_facility_$it", "drawable", requireContext().packageName
                        ), contentFacility[it - 1]
                    )
                )
            ) facilityAndLeisureList.add(
                FacilityAndLeisureItem(
                    resources.getIdentifier(
                        "ic_campsite_facility_$it", "drawable", requireContext().packageName
                    ), contentFacility[it - 1]
                )
            )
        }

        campsiteDetailInfo.amenities.forEach {
            facilityAndLeisureList.add(
                FacilityAndLeisureItem(
                    resources.getIdentifier(
                        "ic_campsite_amenity_$it", "drawable", requireContext().packageName
                    ), contentAmenity[it - 1]
                )
            )
        }

        campsiteDetailInfo.themes.forEach {
            facilityAndLeisureList.add(
                FacilityAndLeisureItem(
                    resources.getIdentifier(
                        "ic_campsite_theme_$it", "drawable", requireContext().packageName
                    ), contentTheme[it - 1]
                )
            )
        }

        if (facilityAndLeisureList.contains(
                FacilityAndLeisureItem(
                    resources.getIdentifier(
                        "ic_campsite_facility_5", "drawable", requireContext().packageName
                    ), contentFacility[4]
                )
            )
        ) facilityAndLeisureList.remove(
            FacilityAndLeisureItem(
                resources.getIdentifier(
                    "ic_campsite_amenity_8", "drawable", requireContext().packageName
                ), contentAmenity[7]
            )
        )

        initRecyclerView(facilityAndLeisureList)
    }

    private fun initViewPager() {
        binding.vpCampsiteImage.apply {
            adapter = CampsiteDetailImageAdapter(
                requireContext(), searchViewModel.campsiteData.value?.thumbnails ?: listOf()
            )
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
        }

        binding.ivIndicator.apply {
            val count =
                if (searchViewModel.campsiteData.value?.thumbnails != null) searchViewModel.campsiteData.value?.thumbnails!!.size else 1
            setSliderHeight(5.px(requireContext()).toFloat())
            setSliderGap(0F)
            setupWithViewPager(binding.vpCampsiteImage)
            setSliderWidth(getDeviceWidthPx(requireContext()).toFloat() / count)
        }
    }

    private fun initRecyclerView(facilityAndLeisure: List<FacilityAndLeisureItem>) {
        if (facilityAndLeisure.isNotEmpty()) binding.rvCampsiteFacilityAndLeisure.apply {
            layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false
            )
            adapter = CampsiteFacilityAndLeisureAdapter(facilityAndLeisure)

            addItemDecoration(LinearItemDecoration(context, RecyclerView.HORIZONTAL, 20))
        }
        else binding.clCampsiteAmenity.visibility = View.GONE

        if (searchViewModel.campsiteData.value != null) {
            binding.rvCampsiteReview.apply {
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

                reviews = if (searchViewModel.campsiteData.value!!.reviews.size > 3) {
                    searchViewModel.campsiteData.value!!.reviews.subList(0, 3)
                } else {
                    binding.tvShowListReview.visibility = View.GONE
                    searchViewModel.campsiteData.value!!.reviews
                }

                adapter = campsiteReviewAdapter

                addItemDecoration(
                    LinearItemDecoration(requireContext(), LinearLayoutManager.VERTICAL, 20)
                )
            }
        }

        if (searchViewModel.campsiteData.value!!.reviews.isEmpty()) {
            binding.rvCampsiteReview.visibility = View.GONE
            binding.tvShowListReview.visibility = View.GONE
            binding.clEmptyCollection.visibility = View.VISIBLE
        }
        setAverageRate(0)
    }

    private fun initListener() {
        binding.apply {
            btnPostbox.setOnClickListener {
                navigate(
                    CampsiteDetailFragmentDirections.actionCampsiteDetailFragmentToSearchPostboxFragment(
                        searchViewModel.campsiteData.value!!.campsiteId
                    )
                )
            }

            btnCampsiteWriteReview.setOnClickListener {
                CampsiteReviewDialog(
                    requireContext(),
                    searchViewModel.campsiteData.value!!.campsiteId,
                    this@CampsiteDetailFragment
                ).show()
            }

            btnBookmark.setOnClickListener {
                lifecycleScope.launch {
                    val isScraped =
                        searchViewModel.scrapCampsite(
                            args.position,
                            searchViewModel.campsiteData.value!!.campsiteId
                        )
                    if (isScraped == "true")
                        btnBookmark.setBackgroundResource(R.drawable.ic_bookmark_on)
                    else if (isScraped == "false")
                        btnBookmark.setBackgroundResource(R.drawable.ic_bookmark_off)
                }
            }

            tvShowListReview.setOnClickListener {
                it.visibility = View.GONE
                campsiteReviewAdapter.setData(0, searchViewModel.campsiteData.value!!.reviews)
                campsiteReviewAdapter.notifyItemRangeInserted(
                    reviews.size,
                    searchViewModel.campsiteData.value!!.reviews.size - reviews.size
                )
            }
        }
    }

    override fun postReview(campsiteId: String, content: String, rate: Int) {
        lifecycleScope.launch {
            val result = searchViewModel.writeReview(SearchReviewRequest(campsiteId, content, rate))
            if (result) {
                showToast("리뷰가 작성되었습니다.")
                val sync = searchViewModel.getCampsiteDetailAsync(campsiteId)
                setAverageRate(sync)
                setReviewData(sync, true, 0)
            } else {
                showToast("리뷰 작성을 실패했습니다.")
            }
        }
    }

    private fun deleteReview(reviewId: String, position: Int) {
        lifecycleScope.launch {
            val result = searchViewModel.deleteReview(reviewId)
            if (result) {
                showToast("리뷰가 삭제되었습니다.")
                val sync =
                    searchViewModel.getCampsiteDetailAsync(searchViewModel.campsiteData.value!!.campsiteId)
                setAverageRate(sync)
                setReviewData(sync, false, position)
            } else {
                showToast("리뷰 삭제를 실패했습니다.")
            }
        }
    }

    private fun setAverageRate(sync: Int) {
        binding.apply {
            ivCampsiteScore1.setBackgroundResource(R.drawable.ic_star_off)
            ivCampsiteScore2.setBackgroundResource(R.drawable.ic_star_off)
            ivCampsiteScore3.setBackgroundResource(R.drawable.ic_star_off)
            ivCampsiteScore4.setBackgroundResource(R.drawable.ic_star_off)
            ivCampsiteScore5.setBackgroundResource(R.drawable.ic_star_off)

            var aver = 0.0

            searchViewModel.campsiteData.value!!.reviews.apply {
                if (this.isNotEmpty()) {
                    forEach { aver += it.rate }
                    aver /= size.toDouble()
                }
            }
            tvContentCampsiteReviewScore.text =
                resources.getString(R.string.content_average_rate, aver)

            if (aver >= 1.0) ivCampsiteScore1.setBackgroundResource(R.drawable.ic_star_on)
            if (aver >= 2.0) ivCampsiteScore2.setBackgroundResource(R.drawable.ic_star_on)
            if (aver >= 3.0) ivCampsiteScore3.setBackgroundResource(R.drawable.ic_star_on)
            if (aver >= 4.0) ivCampsiteScore4.setBackgroundResource(R.drawable.ic_star_on)
            if (aver == 5.0) ivCampsiteScore5.setBackgroundResource(R.drawable.ic_star_on)
        }
    }

    private fun setReviewData(sync: Int, isInserted: Boolean, position: Int) {
        searchViewModel.campsiteData.value!!.reviews.apply {
            if (this.size > 3) {
                reviews = this.subList(0, 3)
                campsiteReviewAdapter.setData(sync, reviews)
                campsiteReviewAdapter.notifyDataSetChanged()
                binding.rvCampsiteReview.visibility = View.VISIBLE
                binding.tvShowListReview.visibility = View.VISIBLE
                binding.clEmptyCollection.visibility = View.GONE
            } else if (this.isNotEmpty()) {
                campsiteReviewAdapter.setData(sync, this)
                if (isInserted)
                    campsiteReviewAdapter.notifyItemInserted(position)
                else
                    campsiteReviewAdapter.notifyItemRemoved(position)
                binding.rvCampsiteReview.visibility = View.VISIBLE
                binding.tvShowListReview.visibility = View.GONE
                binding.clEmptyCollection.visibility = View.GONE
            } else {
                binding.rvCampsiteReview.visibility = View.GONE
                binding.tvShowListReview.visibility = View.GONE
                binding.clEmptyCollection.visibility = View.VISIBLE
                campsiteReviewAdapter.setData(sync, listOf())
                campsiteReviewAdapter.notifyDataSetChanged()
            }
        }
    }
}